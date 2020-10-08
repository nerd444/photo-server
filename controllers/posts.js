const path = require("path");
const connection = require("../db/mysql_connection");

// aws S3
var AWS = require("aws-sdk");

// @desc        사진1장과 내용을 업로드 하는 API
// @route       POST /api/v1/posts
// @request     photo, content, user_id(auth)
// @response    success
exports.uploadPhoto = async (req, res, next) => {
  let user_id = req.user.id;
  let photo = req.files.photo;
  let content = req.body.content;

  if (photo.mimetype.startsWith("image") == false) {
    res.status(400).json({ message: "사진 파일 아닙니다." });
    return;
  }

  if (photo.size > process.env.MAX_FILE_SIZE) {
    res.status(400).json({ message: "파일 크기가 너무 큽니다." });
    return;
  }

  photo.name = `photo_${user_id}_${Date.now()}${path.parse(photo.name).ext}`;

  // aws S3에 억세스 할 권한 설정.
  let file = photo.data;
  const S3_BUCKET = "textedu";
  const AWS_ACCESS_KEY_ID = "AKIA3JBQU4QQ6ZLAPYNV";
  const AWS_SECRET_ACCESS_KEY = "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";

  AWS.config.update({
    accessKeyId: AWS_ACCESS_KEY_ID,
    secretAccessKey: AWS_SECRET_ACCESS_KEY,
  });

  // aws S3에 파일 업로드(대소문자 중요, aws 공식문서에 있음)
  const s3 = new AWS.S3();

  let params = {
    Bucket: S3_BUCKET,
    key: photo.name,
    body: file,
    ContentType: path.parse(photo.name).ext.split(".")[1],
    ACL: "public-read",
  };

  s3.upload(params, async function (err, s3Data) {
    console.log("err : ", err, "s3Data : ", s3Data);

    let query =
      "insert into photo_post (user_id, photo_url, content) \
                values (?,?,?)";
    let data = [user_id, photo.name, content];

    try {
      [result] = await connection.query(query, data);
      res.status(200).json({ success: true });
      return;
    } catch (e) {
      res.status(500).json({ error: e });
      return;
    }
  });
};

// @desc    내가 쓴 포스트 정보 가져오기 (25개씩)
// @route   GET /api/v1/posts/me?offset=0&limit=25
// @request user_id(auth), offset, limit
// @response  success, items[], cnt
exports.getMyPosts = async (req, res, next) => {
  let user_id = req.user.id;
  let offset = req.query.offset;
  let limit = req.query.limit;

  if (!user_id || !offset || !limit) {
    res.status(400).json({ message: "파라미터가 잘 못 되었습니다." });
    return;
  }

  let query = "select * from photo_post where user_id = ? limit ? , ? ;";
  let data = [user_id, Number(offset), Number(limit)];

  try {
    [rows] = await connection.query(query, data);
    res.status(200).json({ success: true, items: rows, cnt: rows.length });
    return;
  } catch (e) {
    res.status(500).json();
    return;
  }
};

// @desc    포스팅 수정하기
// @route   PUT /api/v1/posts/:post_id
// @request user_id(auth), photo, content
// @response  success

exports.updatePost = async (req, res, next) => {
  let post_id = req.params.post_id;
  let user_id = req.user.id;
  let photo = req.files.photo;
  let content = req.body.content;

  // 이 사람의 포스팅을 변경하는것인지, 확인한다.
  let query = "select * from photo_post where id = ? ";
  let data = [post_id];

  try {
    [rows] = await connection.query(query, data);
    // 다른사람이 쓴 글을, 이 사람이 바꾸려고 하면, 401로 보낸다.
    if (rows[0].user_id != user_id) {
      req.status(401).json();
      return;
    }
  } catch (e) {
    res.status(500).json();
    return;
  }

  if (photo.mimetype.startsWith("image") == false) {
    res.stats(400).json({ message: "사진 파일 아닙니다." });
    return;
  }

  if (photo.size > process.env.MAX_FILE_SIZE) {
    res.stats(400).json({ message: "파일 크기가 너무 큽니다." });
    return;
  }

  photo.name = `photo_${user_id}_${Date.now()}${path.parse(photo.name).ext}`;

  let fileUploadPath = `${process.env.FILE_UPLOAD_PATH}/${photo.name}`;

  photo.mv(fileUploadPath, async (err) => {
    if (err) {
      console.log(err);
      return;
    }
  });

  query = "update photo_post set photo_url = ? , content = ? where id = ? ";
  data = [photo.name, content, post_id];

  try {
    [result] = await connection.query(query, data);
    res.status(200).json({ success: true });
    return;
  } catch (e) {
    res.status(500).json();
    return;
  }
};

// @desc    내 포스팅 삭제하기 (1개)
// @route   DELETE /api/v1/posts/:post_id
// @request post_id, user_id(auth)
// @response  success

exports.deletePost = async (req, res, next) => {
  let post_id = req.params.post_id;
  let user_id = req.user.id;

  if (!post_id || !user_id) {
    res.status(400).json({ message: "파라미터가 잘못 되었습니다." });
    return;
  }

  // 이 사람의 포스팅이 맞는지 확인하는 코드 // 시작
  let query = "select * from photo_post where id = ? ";
  let data = [post_id];

  let photo_url;
  try {
    [rows] = await connection.query(query, data);
    // 다른사람 포스팅이면, 401로 보낸다.
    if (rows[0].user_id != user_id) {
      req.status(401).json();
      return;
    }
    photo_url = rows[0].photo_url;
  } catch (e) {
    res.status(500).json();
    return;
  }
  // 이 사람의 포스팅이 맞는지 확인하는 코드 // 끝.

  query = "delete from photo_post where id = ? ";
  data = [post_id];

  try {
    [result] = await connection.query(query, data);
    res.status(200).json({ success: true });
    return;
  } catch (e) {
    res.status(500).json();
    return;
  }
};

// @desc    내 친구들의 포스팅 불러오기 (25개씩)
// @route   GET /api/v1/posts?offset=0&limit=25
// @request user_id(auth)
// @response  success, items[], cnt

exports.getFriendsPost = async (req, res, next) => {
  let user_id = req.user.id;
  let offset = req.query.offset;
  let limit = req.query.limit;

  if (!user_id || !offset || !limit) {
    res.status(400).json();
    return;
  }

  let query =
    "select p.* \
  from photo_follow as f \
  join photo_post as p \
  on f.friend_user_id = p.user_id \
  where f.user_id = ? \
  order by p.created_at desc \
  limit ?, ? ";

  let data = [user_id, Number(offset), Number(limit)];

  try {
    [rows] = await connection.query(query, data);
    res.status(200).json({ success: true, items: rows, cnt: rows.length });
    return;
  } catch (e) {
    res.status(500).json();
    return;
  }
};

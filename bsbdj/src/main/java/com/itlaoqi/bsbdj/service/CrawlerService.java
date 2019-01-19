package com.itlaoqi.bsbdj.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.itlaoqi.bsbdj.common.OgnlUtil;
import com.itlaoqi.bsbdj.entity.Comment;
import com.itlaoqi.bsbdj.entity.Content;
import com.itlaoqi.bsbdj.entity.Forum;
import com.itlaoqi.bsbdj.entity.Image;
import com.itlaoqi.bsbdj.entity.Source;
import com.itlaoqi.bsbdj.entity.User;
import com.itlaoqi.bsbdj.entity.Video;
import com.itlaoqi.bsbdj.mapper.CommentMapper;
import com.itlaoqi.bsbdj.mapper.ContentMapper;
import com.itlaoqi.bsbdj.mapper.ForumMapper;
import com.itlaoqi.bsbdj.mapper.ImageMapper;
import com.itlaoqi.bsbdj.mapper.SourceMapper;
import com.itlaoqi.bsbdj.mapper.UserMapper;
import com.itlaoqi.bsbdj.mapper.VideoMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;

@Service
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class CrawlerService {

	private Logger logger = LoggerFactory.getLogger(CrawlerService.class);

	@Autowired
	private SourceMapper sourceMapper;

	@Autowired
	private ContentMapper contentMapper;

	@Autowired
	private ImageMapper imageMapper;

	@Autowired
	private VideoMapper videoMapper;

	@Autowired
	private ForumMapper forumMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private CommentMapper commentMapper;

	/*
	 * 抓取“百思不得姐”的内容
	 */
	@SuppressWarnings("rawtypes")
	public void crawl(String template, String np, int count, int channelId) {

		String url = StringUtils.replace(template, "{np}", np);

		// 开始抓取
		OkHttpClient client = new OkHttpClient();
		Request.Builder builder = new Request.Builder().url(url);
		builder.addHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
		Request request = builder.build();
		String retText = null;

		for (int i = 1; i <= 10; i++) {

			try {
				Response response = client.newCall(request).execute();
				retText = response.body().string();
				break;
			} catch (IOException e) {
				logger.error("第 {} 次数据抓取尝试失败. Url：{}", i, url);
				logger.error("错误信息：", e);
			}

		}

		if (StringUtils.isEmpty(retText)) {

			logger.error("数据抓取失败. url：{}，频道：{}，np：{}", url, channelId, np);
			return;

		}

		Source source = new Source();
		source.setChannelId(channelId);
		source.setCreateTime(new Date());
		source.setResponseText(retText);
		source.setState("WAITING");
		source.setUrl(url);
		sourceMapper.insert(source);
		logger.info("原始数据保存成功. source: {}", source);

		// 解析数据
		Gson gson = new Gson();
		Map ret = gson.fromJson(retText, new TypeToken<Map>() {
		}.getType());
		double dnp = (double) ((Map) ret.get("info")).get("np");
		String npStr = new DecimalFormat("################").format(dnp);

		count++;
		logger.info("第 {} 次数据抓取成功. Url: {}, channelId: {}, npStr: {}", count, url, channelId, npStr);
		if (count == 5) {
			logger.info("第 {} 个模块数据抓取完毕. Url: {} ", count, url);
			return;
		}

		crawl(template, npStr, count, channelId);
	}

	/*
	 * 对抓取的内容进行解析保存
	 */
	public void etl() {

		logger.info("CrawlerService.etl is running now.");

		List<Source> sourceList = sourceMapper.selectByState("WAITING");

		if (sourceList != null) {
			for (Source source : sourceList) {

				String jsonStr = source.getResponseText();
				Map<String, Object> map = new Gson().fromJson(jsonStr, new TypeToken<Map<String, Object>>() {
				}.getType());
				List<Map<String, Object>> list = OgnlUtil.getListMap("list", map);

				for (Map<String, Object> contentText : list) {
					Content content = getContent(contentText);
					if (content != null) {
						content.setChannelId(source.getChannelId().longValue());
						content.setSourceId(source.getSourceId());
						contentMapper.insert(content);
					}
				}
				source.setState("PROCESSED");
				sourceMapper.updateByPrimaryKey(source);

				logger.info("CrawlerService.etl 资源解析完成, sourceId: {}.", source.getSourceId());
			}
		}

	}

	/*
	 * 解析资源获取内容
	 */
	private Content getContent(Map<String, Object> map) {

		Long cId = OgnlUtil.getNumber("id", map).longValue();
		if (contentMapper.selectByPrimaryKey(cId) != null) {
			logger.info("CrawlerService.getContent 内容已存在, contentId: {}.", cId);
			return null;
		}

		Content content = new Content();
		content.setContentId(cId);
		content.setBookmarkCount(OgnlUtil.getNumber("bookmark", map).intValue());
		content.setCommentCount(OgnlUtil.getNumber("comment", map).intValue());
		content.setContentText(OgnlUtil.getString("text", map));
		content.setContentType(OgnlUtil.getString("type", map));
		content.setHateCount(OgnlUtil.getNumber("down", map).intValue());
		content.setLikeCount(OgnlUtil.getNumber("up", map).intValue());
		content.setPasstime(OgnlUtil.getString("passtime", map));
		content.setShareCount(OgnlUtil.getNumber("forward", map).intValue());
		content.setShareUrl(OgnlUtil.getString("share_url", map));
		content.setStatus(OgnlUtil.getNumber("status", map).intValue());
		content.setCreateTime(new Date());

		String contentType = content.getContentType();
		if ("image".equals(contentType)) {
			Image image = getImage(map);
			image.setContentId(content.getContentId());
			imageMapper.insert(image);
		} else if ("gif".equals(contentType)) {
			Image gif = getGif(map);
			gif.setContentId(content.getContentId());
			imageMapper.insert(gif);
		} else if ("video".equals(contentType)) {
			Video video = getVideo(map);
			video.setContentId(content.getContentId());
			videoMapper.insert(video);
		}

		Forum forum = getForum(map);
		if (forum != null) {
			content.setForumId(forum.getForumId());
		}

		Comment comment = getComment(map);
		if (comment != null) {
			comment.setContentId(content.getContentId());
			commentMapper.insert(comment);
		}

		Long uid = OgnlUtil.getNumber("u.uid", map).longValue();
		if (uid != null) {
			User user = userMapper.selectByPrimaryKey(uid);
			if (user == null) {

				user = new User();
				user.setUid(uid);
				setUser(map, user);
				userMapper.insert(user);

			}
			content.setUid(uid);
		}

		logger.info("CrawlerService.getContent 内容解析完成, contentId: {}.", cId);
		return content;

	}

	private Image getImage(Map<String, Object> map) {

		Image image = new Image();

		List<String> bigUrlList = OgnlUtil.getListString("image.big", map);
		if (bigUrlList != null && bigUrlList.size() > 0) {
			image.setBigUrl(bigUrlList.get(0));
		}

		Number number = OgnlUtil.getNumber("image.id", map);
		if (number != null) {
			image.setImageId(number.longValue());
		}
		image.setRawHeight(OgnlUtil.getNumber("image.height", map).intValue());
		image.setRawWidth(OgnlUtil.getNumber("image.width", map).intValue());

		List<String> thumbUrlList = OgnlUtil.getListString("image.thumbnail_small", map);
		if (thumbUrlList != null && thumbUrlList.size() > 0) {
			image.setThumbUrl(thumbUrlList.get(0));
		}

		logger.info("CrawlerService.getImage 图片保存成功.");
		return image;

	}

	private Image getGif(Map<String, Object> map) {

		Image gif = new Image();

		List<String> imageList = OgnlUtil.getListString("gif.images", map);
		if (imageList != null && imageList.size() > 0) {
			gif.setBigUrl(imageList.get(0));
		}

//		git.setImageId(OgnlUtil.getNumber("image.id", map).longValue());
		Number number = OgnlUtil.getNumber("gif.id", map);
		if (number != null) {
			gif.setImageId(number.longValue());
		}
		gif.setRawHeight(OgnlUtil.getNumber("gif.height", map).intValue());
		gif.setRawWidth(OgnlUtil.getNumber("gif.width", map).intValue());

		List<String> thumbUrlList = OgnlUtil.getListString("gif.gif_thumbnail", map);
		if (thumbUrlList != null && thumbUrlList.size() > 0) {
			gif.setThumbUrl(thumbUrlList.get(0));
		}

		logger.info("CrawlerService.getGif gif 保存成功.");
		return gif;

	}

	private Video getVideo(Map<String, Object> map) {

		Video video = new Video();

		List<String> downloadUrlList = OgnlUtil.getListString("video.download", map);
		if (downloadUrlList != null && downloadUrlList.size() > 0) {
			video.setDownloadUrl(downloadUrlList.get(0));
		}

		video.setDuration(OgnlUtil.getNumber("video.duration", map).intValue());
		video.setHeight(OgnlUtil.getNumber("video.height", map).intValue());
		video.setWidth(OgnlUtil.getNumber("video.width", map).intValue());
		video.setPlayfcount(OgnlUtil.getNumber("video.playfcount", map).intValue());
		video.setPlaycount(OgnlUtil.getNumber("video.playcount", map).intValue());

		List<String> thumbList = OgnlUtil.getListString("video.thumbnail", map);
		if (thumbList != null && thumbList.size() > 0) {
			video.setThumb(thumbList.get(0));
		}

		List<String> thumbSmallList = OgnlUtil.getListString("video.thumbnail_small", map);
		if (thumbSmallList != null && thumbSmallList.size() > 0) {
			video.setThumbSmall(thumbSmallList.get(0));
		}

		List<String> videoUrlList = OgnlUtil.getListString("video.video", map);
		if (videoUrlList != null && videoUrlList.size() > 0) {
			video.setVideoUrl(videoUrlList.get(0));
		}

		logger.info("CrawlerService.getVideo 视频保存成功.");
		return video;

	}

	private Forum getForum(Map<String, Object> map) {

		List<Map<String, Object>> forumList = OgnlUtil.getListMap("tags", map);
		Forum forum = null;
		if (forumList != null && forumList.size() > 0) {

			Map<String, Object> forumMap = forumList.get(0);
			Long fid = OgnlUtil.getNumber("id", forumMap).longValue();
			forum = forumMapper.selectByPrimaryKey(fid);

			if (forum == null) {
				forum = new Forum();
				forum.setForumId(fid);
				forum.setForumSort(OgnlUtil.getNumber("forum_sort", forumMap).intValue());
				forum.setForumStatus(OgnlUtil.getNumber("forum_status", forumMap).intValue());
				forum.setInfo(OgnlUtil.getString("info", forumMap));
				forum.setName(OgnlUtil.getString("name", forumMap));
				forum.setPostCount(OgnlUtil.getNumber("post_number", forumMap).intValue());
				forum.setUserCount(OgnlUtil.getNumber("sub_number", forumMap).intValue());
				forumMapper.insert(forum);
			}

		}

		logger.info("CrawlerService.getForum 论坛保存成功.");
		return forum;
	}

	private Comment getComment(Map<String, Object> map) {

		List<Map<String, Object>> commentList = OgnlUtil.getListMap("top_comments", map);
		Comment comment = null;
		if (commentList != null && commentList.size() > 0) {

			Map<String, Object> commentMap = commentList.get(0);
			comment = new Comment();

			comment.setCommentId(OgnlUtil.getNumber("id", commentMap).longValue());
			comment.setCommentText(OgnlUtil.getString("content", commentMap));
			comment.setPasstime(OgnlUtil.getString("passtime", commentMap));

			Long uid = OgnlUtil.getNumber("u.uid", commentMap).longValue();
			if (uid != null) {

				User user = userMapper.selectByPrimaryKey(uid);
				if (user == null) {

					user = new User();
					user.setUid(uid);
					setUser(commentMap, user);
					userMapper.insert(user);

				}
				comment.setUid(uid);
			}
		}

		logger.info("CrawlerService.getComment 评论保存成功.");
		return comment;
	}

	private void setUser(Map<String, Object> commentMap, User user) {

		List<String> headers = OgnlUtil.getListString("u.header", commentMap);
		if (headers != null && headers.size() > 0) {
			user.setHeader(headers.get(0));
		}
		user.setIsVip(OgnlUtil.getBoolean("u.is_vip", commentMap) ? 1 : 0);
		user.setNickname(OgnlUtil.getString("u.name", commentMap));
		user.setRoomIcon(OgnlUtil.getString("u.room_icon", commentMap));
		user.setRoomName(OgnlUtil.getString("u.room_name", commentMap));
		user.setRoomRole(OgnlUtil.getString("u.room_role", commentMap));
		user.setRoomUrl(OgnlUtil.getString("u.room_url", commentMap));
		if (OgnlUtil.getBoolean("u.is_v", commentMap) != null) {
			user.setIsV(OgnlUtil.getBoolean("u.is_v", commentMap) ? 1 : 0);
		}

		logger.info("CrawlerService.setUser 用户保存成功.");
	}

	public PageInfo<Map<String, Object>> findByParams(Integer page, Integer limit, Integer channelId, String contentType, String keyword) {
		
		Map<String, Object> params = new HashMap<>();
		if(channelId != null && channelId != -1) {
			params.put("channelId", channelId);
		}
		if(contentType != null && !"-1".equals(contentType)) {
			params.put("contentType", contentType);
		}
		if(keyword != null && !keyword.trim().equals("")) {
			params.put("keyword", keyword);
		}
		
		PageHelper.startPage(page, limit);
		List<Map<String, Object>> contentList = contentMapper.findByParams(params);
		PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(contentList);
		
		return pageInfo;
		
	}

}

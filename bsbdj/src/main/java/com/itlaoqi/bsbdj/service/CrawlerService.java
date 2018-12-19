package com.itlaoqi.bsbdj.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.itlaoqi.bsbdj.entity.Source;
import com.itlaoqi.bsbdj.mapper.SourceMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.text.DecimalFormat;

@Service
public class CrawlerService {

	private Logger logger = LoggerFactory.getLogger(CrawlerService.class);
	
	@Autowired
	private SourceMapper sourceMapper;
	
	public void crawlerRunner() {
		
		String[] templates = new String[] {
			"http://s.budejie.com/topic/list/jingxuan/1/bs02-iphone-4.6/{np}-20.json"		// 全部
			, "http://s.budejie.com/topic/list/jingxuan/41/bs02-iphone-4.6/{np}-20.json"	// 视频
			, "http://s.budejie.com/topic/list/jingxuan/10/bs02-iphone-4.6/{np}-20.json"	// 图片
			, "http://s.budejie.com/topic/list/jingxuan/29/bs02-iphone-4.6/{np}-20.json"	// 段子
			, "http://s.budejie.com/topic/list/remen/1/bs02-iphone-4.6/{np}-20.json"		// 排行
		};
		
		for(int i = 0; i < templates.length; i++) {
			
			String template = templates[i];
			int count = 0;
			
			logger.info("正在抓取第 {} 个模块，urlTemplate: {}", i+1, template);
			
			crawl(template, "0", count, i+1);
			
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private void crawl(String template, String np, int count, int channelId) {
		
		String url = StringUtils.replace(template, "{np}", np);
		
		// 开始抓取
		OkHttpClient client = new OkHttpClient();
		Request.Builder builder = new Request.Builder().url(url);
		builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
		Request request = builder.build();
		String retText = null;
		
		for(int i = 1; i <= 10; i++) {
			
			try {
				Response response = client.newCall(request).execute();
				retText = response.body().string();
				break;
			} catch(IOException e) {
				logger.error("第 {} 次数据抓取尝试失败. Url：{}", i, url);
				logger.error("错误信息：", e);
			}
			
		}
		
		if(StringUtils.isEmpty(retText)) {
			
			logger.error("数据抓取失败. url：{}，频道：{}，np：{}", url, channelId, np);
			return;
			
		}
		
		Source source = new Source();
		source.setChannelId(channelId);
		source.setCreateTime(new Date());
		source.setResponseText(retText);
		source.setState("Waiting");
		source.setUrl(url);
		sourceMapper.insert(source);
		logger.info("原始数据保存成功. source: {}", source);
		
		// 解析数据
		Gson gson = new Gson();
		Map ret = gson.fromJson(retText, new TypeToken<Map>(){}.getType());
		double dnp = (double)((Map)ret.get("info")).get("np");
		String npStr = new DecimalFormat("################").format(dnp);
		
		count++;
		logger.info("第 {} 次数据抓取成功. Url: {}, channelId: {}, npStr: {}", count, url, channelId, npStr);
		if(count == 5) {
			logger.info("第 {} 个模块数据抓取完毕. Url: {} ", count, url);
			return;
		}
		
		crawl(template, npStr, count, channelId);
	}
	
	public void etl() {
		
		
	}
	
}

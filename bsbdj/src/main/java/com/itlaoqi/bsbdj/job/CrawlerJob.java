package com.itlaoqi.bsbdj.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.itlaoqi.bsbdj.service.CrawlerService;

@Component
public class CrawlerJob {
	
	private Logger logger = LoggerFactory.getLogger(CrawlerJob.class);
	@Autowired
	private CrawlerService service = null;

	@Scheduled(cron = "0 0 */6 * * ?")	// cron = 秒 分 时 日 月 星期
	public void crawlerRunner() {

		String[] templates = new String[] { "http://s.budejie.com/topic/list/jingxuan/1/bs02-iphone-4.6/{np}-20.json" // 全部
				, "http://s.budejie.com/topic/list/jingxuan/41/bs02-iphone-4.6/{np}-20.json" // 视频
				, "http://s.budejie.com/topic/list/jingxuan/10/bs02-iphone-4.6/{np}-20.json" // 图片
				, "http://s.budejie.com/topic/list/jingxuan/29/bs02-iphone-4.6/{np}-20.json" // 段子
				, "http://s.budejie.com/topic/list/remen/1/bs02-iphone-4.6/{np}-20.json" // 排行
		};

		for (int i = 0; i < templates.length; i++) {

			String template = templates[i];
			int count = 0;

			logger.info("正在抓取第 {} 个模块，urlTemplate: {}", i + 1, template);

			service.crawl(template, "0", count, i + 1);

		}
		
		service.etl();

	}
	
}

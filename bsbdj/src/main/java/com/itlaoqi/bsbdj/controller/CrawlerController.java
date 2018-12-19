package com.itlaoqi.bsbdj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itlaoqi.bsbdj.service.CrawlerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@Controller
public class CrawlerController {

	private Logger logger = LoggerFactory.getLogger(CrawlerController.class);
	
	@Resource
	private CrawlerService crawlerService;
	
	@RequestMapping("/c")
	@ResponseBody
	public String crawl() {

		logger.info("CrawlerController.crawl is Running.");
		
		crawlerService.crawlerRunner();
		
		return "success";

	}

}

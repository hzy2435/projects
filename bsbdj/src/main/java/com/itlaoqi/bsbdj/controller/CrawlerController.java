package com.itlaoqi.bsbdj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.itlaoqi.bsbdj.service.CrawlerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

@Controller
public class CrawlerController {

	private Logger logger = LoggerFactory.getLogger(CrawlerController.class);
	
	@Resource
	private CrawlerService crawlerService;
	
	@RequestMapping("/") 
	public String index(){
		return "manager";
	}
	
	@RequestMapping("/list")
	@ResponseBody
	public Map<String, Object> getContents(Integer page, Integer limit, Integer channelId, String contentType, String keyword) {
		
		Map<String, Object> response = new HashMap<>();
		
		response.put("code", 0);
		response.put("msg", "");
		
		PageInfo<Map<String, Object>> pageInfo = crawlerService.findByParams(page, limit, channelId, contentType, keyword);
		response.put("data", pageInfo.getList());
		response.put("count", pageInfo.getTotal());
		
		return response;
	}
}

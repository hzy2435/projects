package com.zshop.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zshop.common.constant.PaginationConstant;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zshop.pojo.ProductType;
import com.zshop.service.ProductTypeService;

@Controller
@RequestMapping("/backend/productType")
public class ProductTypeController {

	@Autowired
	private ProductTypeService productTypeService;
	
	@RequestMapping("/getAll")
	public String getAll(Integer pageNum, Model model) {
		
		if(ObjectUtils.isEmpty(pageNum)) {
			pageNum = PaginationConstant.PAGE_NUM;
		}
		
		PageHelper.startPage(pageNum, PaginationConstant.PAGE_SIZE);
		
		List<ProductType> productTypeList = productTypeService.getAll();
		
		PageInfo<ProductType> pageInfo = new PageInfo<>(productTypeList);
		model.addAttribute("pageInfo", pageInfo);
		return "productTypeManager";
	}
	
}

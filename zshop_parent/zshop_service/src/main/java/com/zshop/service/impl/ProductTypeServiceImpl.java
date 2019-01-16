package com.zshop.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zshop.dao.ProductTypeDao;

import com.zshop.pojo.ProductType;
import com.zshop.service.ProductTypeService;

@Service("productTypeService")
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class, isolation=Isolation.READ_COMMITTED)
public class ProductTypeServiceImpl implements ProductTypeService {

	@Autowired
	private ProductTypeDao productTypeDao;
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	@Override
	public List<ProductType> getAll() {
		return productTypeDao.findAll();
	}

}

package com.itlaoqi.bsbdj.mapper;

import java.util.List;
import java.util.Map;

import com.itlaoqi.bsbdj.entity.Content;

public interface ContentMapper {
    int deleteByPrimaryKey(Long contentId);

    int insert(Content record);

    int insertSelective(Content record);

    Content selectByPrimaryKey(Long contentId);

    int updateByPrimaryKeySelective(Content record);

    int updateByPrimaryKey(Content record);

	List<Map<String, Object>> findByParams(Map<String, Object> params);
}
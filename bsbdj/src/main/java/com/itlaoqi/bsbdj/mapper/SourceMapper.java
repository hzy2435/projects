package com.itlaoqi.bsbdj.mapper;

import java.util.List;

import com.itlaoqi.bsbdj.entity.Source;

public interface SourceMapper {
    int deleteByPrimaryKey(Long sourceId);

    int insert(Source record);

    int insertSelective(Source record);

    Source selectByPrimaryKey(Long sourceId);

    int updateByPrimaryKeySelective(Source record);

    int updateByPrimaryKey(Source record);

	List<Source> selectByState(String state);
}
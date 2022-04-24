package com.ink.bigdata.mapper;

import com.ink.bigdata.bean.Test;

import java.util.List;

public interface TestMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Test record);

    int insertSelective(Test record);

    Test selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Test record);

    int updateByPrimaryKey(Test record);

    List<Test> getAll();

    int batchUpdateV1(List<Test> list);

    int batchUpdateV2(List<Test> list);


}

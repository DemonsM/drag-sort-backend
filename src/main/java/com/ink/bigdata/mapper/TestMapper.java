package com.ink.bigdata.mapper;

import com.ink.bigdata.bean.Test;
import org.apache.ibatis.annotations.Param;

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

    Test selectFirstNode(@Param("projectId") Long projectId);

    Test selectLastNode(@Param("projectId") Long projectId);

    List<Test> selectByProjectId(@Param("projectId") Long projectId);

    Long selectNodeById(@Param("id") Long id);

    int reset_node(@Param("projectId") Long projectId);
}

package com.ink.bigdata.controller;

import com.ink.bigdata.bean.Test;
import com.ink.bigdata.bean.vo.AcrossReqVO;
import com.ink.bigdata.mapper.TestMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
public class TestController {
    private static final BigDecimal GAP = new BigDecimal(65536);
    private TestMapper testMapper;

    @GetMapping("/get-all")
    public List<Test> getAll() {
        return testMapper.getAll();
    }

    @GetMapping("/set")
    public String set() {
        List<Test> list = testMapper.getAll();
        BigDecimal temp = GAP;
        for (Test test : list) {
            test.setNode(temp.toPlainString());
            temp = temp.add(GAP);
        }
        long l = System.currentTimeMillis();
        testMapper.batchUpdateV1(list);
        log.info(String.valueOf((System.currentTimeMillis() - l) / 1000d));
        return "ok";
    }

    @GetMapping("/set2")
    public String set2() {
        List<Test> list = testMapper.getAll();
        BigDecimal temp = GAP;
        for (Test test : list) {
            test.setNode(temp.toPlainString());
            temp = temp.add(GAP);
        }
        long l = System.currentTimeMillis();
        for (Test test : list) {
            testMapper.updateByPrimaryKeySelective(test);
        }
        log.info(String.valueOf((System.currentTimeMillis() - l) / 1000d));
        return "ok";
    }

    @GetMapping("/set3")
    public String set3() {
        List<Test> list = testMapper.getAll();
        Map<Long, List<Test>> projectMap = list.stream().collect(Collectors.groupingBy(Test::getProjectId));
        for (List<Test> testList : projectMap.values()) {
            BigDecimal temp = GAP;
            for (Test test : testList) {
                test.setNode(temp.toPlainString());
                temp = temp.add(GAP);
            }
        }
        long l = System.currentTimeMillis();
        testMapper.batchUpdateV2(list);
        log.info(String.valueOf((System.currentTimeMillis() - l) / 1000d));
        return "ok";
    }

    @PostMapping("/across")
    public String across(@RequestBody AcrossReqVO request) {
        Long curId = request.getCurId();
        Test curTest = testMapper.selectByPrimaryKey(curId);
        if (request.getIsFirst()) {

        }
        if (request.getIsLast()) {

        }
        return "ok";
    }
}

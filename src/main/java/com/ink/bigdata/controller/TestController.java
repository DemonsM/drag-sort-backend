package com.ink.bigdata.controller;

import cn.hutool.core.collection.ListUtil;
import com.ink.bigdata.bean.Test;
import com.ink.bigdata.bean.vo.AcrossReqVO;
import com.ink.bigdata.mapper.TestMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
public class TestController {
    private static final Long GAP = 65536L;
    /**
     * 超级小数
     */
    private static final Double EPS = 1E-10;
    private TestMapper testMapper;

    @GetMapping("/get-all")
    public List<Test> getAll() {
        return testMapper.getAll();
    }

    @GetMapping("/set")
    @Transactional(rollbackFor = Exception.class)
    public String set3() {
        List<Test> list = testMapper.getAll();
        Map<Long, List<Test>> projectMap = list.stream().collect(Collectors.groupingBy(Test::getProjectId));
        for (List<Test> testList : projectMap.values()) {
            long temp = GAP;
            for (Test test : testList) {
                test.setNode(temp);
                temp += GAP;
            }
        }
        long l = System.currentTimeMillis();
        for (List<Test> tests : ListUtil.split(list, 1000)) {
            testMapper.batchUpdateV2(tests);
        }
        log.info(String.valueOf((System.currentTimeMillis() - l) / 1000d));
        return "ok";
    }


    private long reset(Long projectId) {
        List<Test> list = testMapper.selectByProjectId(projectId);
        long temp = GAP;
        for (Test test : list) {
            test.setNode(temp);
            temp += GAP;
        }
        testMapper.batchUpdateV2(list);
        return temp;
    }

    @PostMapping("/across")
    @Transactional(rollbackFor = Exception.class)
    public String across(@RequestBody AcrossReqVO request) {
        Long projectId = request.getProjectId();

        Test updated = new Test();
        updated.setId(request.getCurId());
        updated.setProjectId(projectId);

        //移到顶端
        if (request.getToFirst()) {
            Test realFirstNode = testMapper.selectFirstNode(projectId);
            double newNode = realFirstNode.getNode() / 2D;
            //flag = true -> newNode为小数
            boolean flag = newNode - Math.floor(newNode) > EPS;
            if (flag) {
                log.info("空间不够，重置列表");
                reset(projectId);
                updated.setNode(GAP / 2);
                testMapper.updateByPrimaryKeySelective(updated);
                return "ok";
            }
            updated.setNode((long) newNode);
            testMapper.updateByPrimaryKeySelective(updated);
            return "ok";
        }

        //移到末端
        if (request.getToLast()) {
            Test realLastNode = testMapper.selectLastNode(projectId);
            long newNode;
            try {
                newNode = Math.multiplyExact(realLastNode.getNode(), 2);
            } catch (ArithmeticException e) {
                log.info("末端越界，重置列表");
                long lastNode = reset(projectId);
                updated.setNode(lastNode);
                testMapper.updateByPrimaryKeySelective(updated);
                return "ok";
            }
            updated.setNode(newNode);
            testMapper.updateByPrimaryKeySelective(updated);
            return "ok";
        }

        //移到中间
        Long preNode = testMapper.selectNodeById(request.getPreId());
        Long nextNode = testMapper.selectNodeById(request.getNextId());
        if (Objects.equals(preNode % 2, 1L) || Objects.equals(nextNode % 2, 1L)) {
            log.info("空间不够，重置列表");
            reset(projectId);
            Long newPreNode = testMapper.selectNodeById(request.getPreId());
            Long newNextNode = testMapper.selectNodeById(request.getNextId());
            updated.setNode((newPreNode + newNextNode) / 2);
        } else {
            updated.setNode((preNode + nextNode) / 2);
        }
        testMapper.updateByPrimaryKeySelective(updated);
        return "ok";
    }
}

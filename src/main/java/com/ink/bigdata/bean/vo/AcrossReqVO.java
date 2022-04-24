package com.ink.bigdata.bean.vo;

import lombok.Data;

@Data
public class AcrossReqVO {
    private Long curId;

    private String preNode;

    private String nextNode;

    private Boolean isFirst;

    private Boolean isLast;
}

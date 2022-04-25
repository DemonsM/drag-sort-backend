package com.ink.bigdata.bean.vo;

import lombok.Data;

@Data
public class AcrossReqVO {
    private Long curId;

    private Long preId;

    private Long nextId;

    private Boolean toFirst;

    private Boolean toLast;

    private Long projectId;
}

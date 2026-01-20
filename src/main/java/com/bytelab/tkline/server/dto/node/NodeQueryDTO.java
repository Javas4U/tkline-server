package com.bytelab.tkline.server.dto.node;

import com.bytelab.tkline.server.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NodeQueryDTO extends PageQueryDTO {
    private String name;
    private String domain;
    private String ipAddress;
    private String region;
    private Integer status;
}

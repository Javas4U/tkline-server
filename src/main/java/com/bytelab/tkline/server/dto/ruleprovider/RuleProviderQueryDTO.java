package com.bytelab.tkline.server.dto.ruleprovider;

import com.bytelab.tkline.server.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Rule Provider 查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RuleProviderQueryDTO extends PageQueryDTO {

    private String name;

    private String type;

    private String behavior;

    private Integer status;
}

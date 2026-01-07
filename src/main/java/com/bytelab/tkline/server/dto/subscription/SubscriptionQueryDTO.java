package com.bytelab.tkline.server.dto.subscription;

import com.bytelab.tkline.server.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubscriptionQueryDTO extends PageQueryDTO {
    private String name;
    private String type;
    private Integer status;
}

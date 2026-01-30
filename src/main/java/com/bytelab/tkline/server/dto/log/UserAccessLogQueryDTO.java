package com.bytelab.tkline.server.dto.log;

import com.bytelab.tkline.server.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAccessLogQueryDTO extends PageQueryDTO {

    /**
     * Node ID
     */
    private Long nodeId;

    /**
     * User ID (Subscription ID)
     */
    private Long userId;

    /**
     * Target Address (Domain or IP)
     */
    private String targetAddress;

    /**
     * Start Time
     */
    private LocalDateTime startTime;

    /**
     * End Time
     */
    private LocalDateTime endTime;
}

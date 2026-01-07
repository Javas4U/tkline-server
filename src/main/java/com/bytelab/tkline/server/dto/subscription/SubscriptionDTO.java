package com.bytelab.tkline.server.dto.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubscriptionDTO {
    private Long id;
    private String name;
    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validTo;

    private Integer status;
    private String statusLabel;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String createBy;
    private String updateBy;
    private Long trafficLimit;
    private Long trafficUsed;
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isTrafficExhausted;
    private Integer nodeCount;
    private Integer version;
}

package com.bytelab.tkline.server.util;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订阅订单号生成器
 * 使用雪花算法保证全局唯一性
 */
public class SubscriptionOrderGenerator {

    private static final String ORDER_PREFIX = "SUB";

    /**
     * 生成订单号
     * 格式: SUBYYYYMMDD{12位数字}
     * 示例: SUB20260109123456789012
     */
    public static String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long snowflakeId = IdWorker.getId();
        // 取雪花ID的后12位数字
        String digits = String.format("%012d", Math.abs(snowflakeId % 1000000000000L));
        return ORDER_PREFIX + dateStr + digits;
    }

    /**
     * 生成子订单号(独立格式,与父订单号相同格式)
     * 格式: SUBYYYYMMDD{12位数字}
     * 示例: SUB20260109987654321098
     *
     * 注意: 子订单号与父订单号通过数据库表字段 subscription_id 关联
     */
    public static String generateChildOrderNo() {
        return generateOrderNo();
    }
}

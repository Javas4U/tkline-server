package com.bytelab.tkline.server.util;

import java.util.UUID;

/**
 * 订阅订单号生成器
 * 使用UUID格式保证全局唯一性
 */
public class SubscriptionOrderGenerator {

    /**
     * 生成订单号
     * 格式: UUID (标准格式，如: 550e8400-e29b-41d4-a716-446655440000)
     * 示例: 123e4567-e89b-12d3-a456-426614174000
     */
    public static String generateOrderNo() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成子订单号(独立格式,与父订单号相同格式)
     * 格式: UUID (标准格式，如: 550e8400-e29b-41d4-a716-446655440000)
     * 示例: 987f6543-e21d-43e2-b123-987654321000
     *
     * 注意: 子订单号与父订单号通过数据库表字段 subscription_id 关联
     */
    public static String generateChildOrderNo() {
        return generateOrderNo();
    }
}

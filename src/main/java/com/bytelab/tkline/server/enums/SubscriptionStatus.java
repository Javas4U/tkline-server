package com.bytelab.tkline.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订阅状态枚举
 * <p>
 * 状态说明：
 * - DISABLED: 禁用状态（不可用于新的节点绑定）
 * - ACTIVE: 有效状态（正常可用）
 * - EXPIRED: 过期状态（有效期已过）
 * <p>
 * 创建日期：2026-01-07
 * 功能分支：014-node-subscription
 *
 * @author apex-tunnel
 */
@Getter
@AllArgsConstructor
public enum SubscriptionStatus {

    /**
     * 禁用状态
     */
    DISABLED(0, "禁用"),

    /**
     * 有效状态
     */
    ACTIVE(1, "有效"),

    /**
     * 过期状态
     */
    EXPIRED(2, "过期");

    /**
     * 状态编码
     */
    private final Integer code;

    /**
     * 中文标签
     */
    private final String label;

    /**
     * 根据状态编码获取标签
     *
     * @param code 状态编码
     * @return 中文标签，未找到返回"未知"
     */
    public static String getLabelByCode(Integer code) {
        if (code == null) {
            return "未知";
        }

        for (SubscriptionStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }
        return "未知";
    }

    /**
     * 根据状态编码获取枚举值
     *
     * @param code 状态编码
     * @return 枚举值，未找到返回null
     */
    public static SubscriptionStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (SubscriptionStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断订阅是否有效（可用于新绑定）
     *
     * @return true表示有效
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * 判断订阅是否已过期
     *
     * @return true表示已过期
     */
    public boolean isExpired() {
        return this == EXPIRED;
    }
}

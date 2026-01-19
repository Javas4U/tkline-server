package com.bytelab.tkline.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点状态枚举
 * <p>
 * 状态说明：
 * - DISABLED: 禁用状态（不可用于新的订阅绑定）
 * - ENABLED: 启用状态（正常可用）
 * <p>
 * 创建日期：2026-01-07
 * 功能分支：014-node-subscription
 *
 * @author apex-tunnel
 */
@Getter
@AllArgsConstructor
public enum NodeStatus {

    /**
     * 禁用状态
     */
    DISABLED(0, "禁用"),

    /**
     * 启用状态
     */
    ENABLED(1, "启用");

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

        for (NodeStatus status : values()) {
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
    public static NodeStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (NodeStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断节点是否启用
     *
     * @return true表示启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }
}

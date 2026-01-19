package com.bytelab.tkline.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点状态枚举
 * <p>
 * 状态说明：
 * - OFFLINE: 离线状态（节点不可用）
 * - ONLINE: 在线状态（节点正常可用）
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
     * 离线状态
     */
    OFFLINE(0, "离线"),

    /**
     * 在线状态
     */
    ONLINE(1, "在线");

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
     * 判断节点是否在线
     *
     * @return true表示在线
     */
    public boolean isOnline() {
        return this == ONLINE;
    }
}

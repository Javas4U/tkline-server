package com.bytelab.tkline.server.constant;

/**
 * 安全配置常量
 * 集中管理安全相关配置
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * Jasypt 加密配置常量
     */
    public static final class Jasypt {
        private Jasypt() {
            // 私有构造函数，防止实例化
        }

        /**
         * 字段加密密钥（用于数据库敏感字段加密）
         * 生产环境应通过环境变量配置
         */
        public static final String FIELD_PASSWORD = System.getenv().getOrDefault(
            "JASYPT_FIELD_PASSWORD",
            "apex-tunnel-field-encryption-key-2024"
        );

        /**
         * 加密算法
         */
        public static final String ALGORITHM = "PBEWithHMACSHA512AndAES_256";

        /**
         * 密钥迭代次数
         */
        public static final String KEY_OBTENTION_ITERATIONS = "1000";

        /**
         * 加密器池大小
         */
        public static final String POOL_SIZE = "1";

        /**
         * 加密提供者
         */
        public static final String PROVIDER_NAME = "SunJCE";

        /**
         * 盐值生成器
         */
        public static final String SALT_GENERATOR_CLASS = "org.jasypt.salt.RandomSaltGenerator";

        /**
         * IV生成器
         */
        public static final String IV_GENERATOR_CLASS = "org.jasypt.iv.RandomIvGenerator";

        /**
         * 输出类型
         */
        public static final String STRING_OUTPUT_TYPE = "base64";
    }
}

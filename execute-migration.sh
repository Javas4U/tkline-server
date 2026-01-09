#!/bin/bash

# 数据库连接配置
DB_HOST="1.94.145.5"
DB_PORT="3306"
DB_NAME="tkline"
DB_USER="root"
DB_PASS="hunter123@bytelab.com"

# SQL文件路径
SQL_FILE="src/main/resources/db/migration/V20260109_add_subscription_order_fields.sql"

echo "=========================================="
echo "开始执行数据库迁移"
echo "=========================================="
echo "目标数据库: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "SQL文件: ${SQL_FILE}"
echo ""

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "错误: SQL文件不存在: $SQL_FILE"
    exit 1
fi

# 显示SQL内容
echo "SQL内容:"
echo "------------------------------------------"
cat "$SQL_FILE"
echo "------------------------------------------"
echo ""

# 询问是否继续
read -p "是否执行以上SQL? (y/n): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "取消执行"
    exit 0
fi

# 执行SQL
echo ""
echo "执行中..."
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "迁移执行成功!"
    echo "=========================================="
    echo ""
    echo "验证表结构:"
    echo ""
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "DESC subscription; DESC node_subscription_relation;"
else
    echo ""
    echo "=========================================="
    echo "迁移执行失败!"
    echo "=========================================="
    exit 1
fi

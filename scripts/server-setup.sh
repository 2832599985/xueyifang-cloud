#!/usr/bin/env bash
# xueyifang-cloud 服务器端初始化脚本
# 在目标服务器上执行，用于首次部署环境准备
set -euo pipefail

INSTALL_DIR="/opt/xueyifang"
COMPOSE_FILE="docker-compose.prod.yml"

echo "========================================"
echo " xueyifang-cloud Server Setup"
echo "========================================"

# 1. 安装 Docker（如未安装）
if ! command -v docker &> /dev/null; then
    echo "Installing Docker ..."
    curl -fsSL https://get.docker.com | sh
    sudo systemctl enable docker
    sudo systemctl start docker
else
    echo "Docker already installed: $(docker --version)"
fi

# 2. 安装 Docker Compose 插件（如未安装）
if ! docker compose version &> /dev/null; then
    echo "Installing Docker Compose plugin ..."
    sudo apt-get update -qq
    sudo apt-get install -y -qq docker-compose-plugin
else
    echo "Docker Compose already installed: $(docker compose version)"
fi

# 3. 创建部署目录
echo "Creating deployment directory: ${INSTALL_DIR}"
sudo mkdir -p "${INSTALL_DIR}"
sudo chown "$(whoami):$(whoami)" "${INSTALL_DIR}"

# 4. 创建数据卷目录
mkdir -p "${INSTALL_DIR}/data/mysql"
mkdir -p "${INSTALL_DIR}/data/redis"
mkdir -p "${INSTALL_DIR}/data/nacos"
mkdir -p "${INSTALL_DIR}/data/file-uploads"

# 5. 复制配置文件（如果不存在）
if [ ! -f "${INSTALL_DIR}/.env" ]; then
    echo ""
    echo "NOTE: Please create ${INSTALL_DIR}/.env from .env.example"
    echo "      and fill in production values before starting services."
fi

echo ""
echo "========================================"
echo " Setup completed!"
echo "========================================"
echo ""
echo "Next steps:"
echo "  1. Copy .env to ${INSTALL_DIR}/.env"
echo "  2. Copy docker-compose.prod.yml to ${INSTALL_DIR}/"
echo "  3. Copy mysql/init/*.sql to ${INSTALL_DIR}/mysql/init/"
echo "  4. Run: cd ${INSTALL_DIR} && docker compose -f ${COMPOSE_FILE} up -d"
# ============================================================
# Lune Makefile - 便捷命令
# 使用方法: make <目标>
# 查看帮助: make help
# ============================================================

COMPOSE_DEV  := docker compose -f docker-compose.dev.yml --env-file .env.local
COMPOSE_PROD := docker compose -f docker-compose.prod.yml

.PHONY: help dev prod setup-dev setup-prod build down clean logs restart status \
        backup restore dev-logs prod-logs backend-logs nginx-logs \
        shell-backend shell-db shell-redis restart-backend

help: ## 显示帮助信息
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-22s\033[0m %s\n", $$1, $$2}'

# ========== 环境初始化 ==========

setup-dev: ## 初始化本地开发环境（首次使用）
	@if [ ! -f .env.local ]; then \
		echo "📋 创建本地开发配置文件 .env.local ..."; \
		cp .env.local.template .env.local; \
		echo "⚠️  请编辑 .env.local 填入真实凭据后重新运行"; \
		exit 1; \
	fi
	@mkdir -p data/upload backups
	@echo "✅ 本地开发环境初始化完成"

setup-prod: ## 初始化生产环境（服务器端执行）
	@if [ ! -f .env ]; then \
		echo "📋 创建生产配置文件 .env ..."; \
		cp .env.template .env; \
		echo "⚠️  请编辑 .env 填入真实凭据后重新运行"; \
		exit 1; \
	fi
	@mkdir -p data/upload backups
	@echo "✅ 生产环境初始化完成"

# ========== 环境管理 ==========

dev: setup-dev ## 启动开发环境（前端 HMR + 后端热重载）
	$(COMPOSE_DEV) down --remove-orphans 2>/dev/null || true
	$(COMPOSE_DEV) up -d --build
	@echo ""
	@echo "✅ 开发环境已启动！"
	@echo "   前端:    http://localhost:$$(grep NGINX_PORT .env.local 2>/dev/null | cut -d= -f2 || echo 8080)"
	@echo "   Vite:    http://localhost:5173"
	@echo "   API:     http://localhost:8081/api"

prod: setup-prod ## 启动生产环境
	$(COMPOSE_PROD) down --remove-orphans 2>/dev/null || true
	$(COMPOSE_PROD) up -d --build
	@echo ""
	@echo "✅ 生产环境已启动！"
	@echo "   地址: http://localhost:$$(grep NGINX_PORT .env 2>/dev/null | cut -d= -f2 || echo 80)"

build: ## 构建所有生产镜像
	$(COMPOSE_PROD) build --no-cache

down: ## 停止所有容器
	$(COMPOSE_DEV) down --remove-orphans 2>/dev/null || true
	$(COMPOSE_PROD) down --remove-orphans 2>/dev/null || true
	@echo "✅ 所有容器已停止"

clean: ## 停止容器并删除数据卷（⚠️ 删除所有数据！）
	@echo "⚠️  即将删除所有 Docker 数据卷（MySQL/Redis/Upload）！"
	@read -p "确认？（输入 yes 继续）: " confirm; \
	if [ "$$confirm" = "yes" ]; then \
		$(COMPOSE_DEV) down -v --remove-orphans 2>/dev/null || true; \
		$(COMPOSE_PROD) down -v --remove-orphans 2>/dev/null || true; \
		echo "✅ 已清理所有容器和数据卷"; \
	else \
		echo "已取消"; \
	fi

restart: ## 重启生产容器
	$(COMPOSE_PROD) restart

restart-backend: ## 仅重启后端
	$(COMPOSE_PROD) restart backend

# ========== 日志 ==========

dev-logs: ## 查看开发环境日志
	$(COMPOSE_DEV) logs -f --tail=100

prod-logs: ## 查看生产环境日志
	$(COMPOSE_PROD) logs -f --tail=100

backend-logs: ## 查看后端日志
	$(COMPOSE_PROD) logs -f --tail=100 backend

nginx-logs: ## 查看 Nginx 日志
	$(COMPOSE_PROD) logs -f --tail=100 nginx

# ========== 状态 ==========

status: ## 查看容器状态
	@echo "========== 开发环境 =========="
	@$(COMPOSE_DEV) ps 2>/dev/null || echo "未运行"
	@echo ""
	@echo "========== 生产环境 =========="
	@$(COMPOSE_PROD) ps 2>/dev/null || echo "未运行"

# ========== 数据库 ==========

backup: ## 备份数据库
	@bash backup.sh

restore: ## 恢复数据库 (用法: make restore FILE=backups/xxx.sql.gz)
	@test -n "$(FILE)" || (echo "❌ 用法: make restore FILE=backups/lune_xxx.sql.gz"; exit 1)
	@if [ -f .env ]; then \
		PASS=$$(grep DB_ROOT_PASSWORD .env | cut -d= -f2); \
	elif [ -f .env.local ]; then \
		PASS=$$(grep DB_ROOT_PASSWORD .env.local | cut -d= -f2); \
	else \
		echo "❌ 未找到 .env 或 .env.local"; exit 1; \
	fi; \
	if [[ "$(FILE)" == *.gz ]]; then \
		gunzip -c "$(FILE)" | docker exec -i lune-mysql mysql -u root -p"$$PASS" lune; \
	else \
		docker exec -i lune-mysql mysql -u root -p"$$PASS" lune < "$(FILE)"; \
	fi
	@echo "✅ 数据库恢复完成"

# ========== Shell ==========

shell-backend: ## 进入后端 Shell
	docker exec -it $$($(COMPOSE_PROD) ps -q backend 2>/dev/null || $(COMPOSE_DEV) ps -q backend) sh

shell-db: ## 进入 MySQL Shell
	@PASS=$$(grep DB_ROOT_PASSWORD .env 2>/dev/null | cut -d= -f2 || grep DB_ROOT_PASSWORD .env.local 2>/dev/null | cut -d= -f2); \
	docker exec -it $$($(COMPOSE_PROD) ps -q mysql 2>/dev/null || $(COMPOSE_DEV) ps -q mysql) mysql -u root -p"$$PASS" lune

shell-redis: ## 进入 Redis Shell
	docker exec -it $$($(COMPOSE_PROD) ps -q redis 2>/dev/null || $(COMPOSE_DEV) ps -q redis) redis-cli

# ========== 维护 ==========

prune: ## 清理 Docker 垃圾
	docker system prune -af
	@echo "✅ 已清理"

# ========== AI Agent ==========

agent-build: ## 构建 Agent JAR
	cd lune-agent && mvn clean package -DskipTests -q
	@echo "✅ lune-agent/target/lune-agent.jar"

agent-logs: ## 查看 Agent 日志（宿主机运行模式）
	@tail -f /tmp/lune-agent.log 2>/dev/null || echo "Agent 未在宿主机运行（或日志不在 /tmp/lune-agent.log）"

agent-start: ## 启动 Agent（宿主机运行模式）
	cd lune-agent && mvn -q package -DskipTests && \
	nohup java -jar target/lune-agent.jar --spring.profiles.active=local > /tmp/lune-agent.log 2>&1 &
	@sleep 3 && curl -s http://localhost:8082/api/actuator/health

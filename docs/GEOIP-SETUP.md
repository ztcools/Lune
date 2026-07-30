# GeoIP 离线库配置

后台「访问统计」的地区维度（中国地图 / 省份排行）依赖 MaxMind **GeoLite2-City** 离线库。
该文件**不入 git**，需在每台运行环境上单独放置一次。

---

## 为什么不打进仓库 / 镜像

| 原因 | 说明 |
|------|------|
| 体积 | 单文件 66MB 二进制。入 git 后每次变更都会在历史里留一份完整副本，仓库无限膨胀 |
| 更新频率 | MaxMind 按月更新，属于「数据」而非「代码」，生命周期与版本库不一致 |
| 部署成本 | 生产用 `docker save` + scp 传镜像。66MB 打进镜像意味着每次部署都在 5Mbps 上行上白传约 2 分钟 |

因此改为**只读挂载**：`./data/geoip:/app/geoip:ro`（`docker-compose.dev.yml` 与
`docker-compose.prod.yml` 均已配置）。

---

## 获取方式

### 方式一：脚本自动下载（推荐）

免费 license key 申请：<https://www.maxmind.com/en/geolite2/signup>

```bash
MAXMIND_LICENSE_KEY=你的key bash scripts/fetch-geoip.sh
```

脚本会下载、解包并放到 `data/geoip/GeoLite2-City.mmdb`。

### 方式二：手工放置

已有 `.mmdb` 文件时，直接放到仓库根目录下：

```
data/geoip/GeoLite2-City.mmdb
```

### 生效

```bash
docker compose -f docker-compose.dev.yml restart backend   # 本地
docker compose -f docker-compose.prod.yml restart backend  # 生产
```

启动日志出现 `[GeoIp] 已从外部路径加载` 即成功。

---

## 服务器部署注意

生产服务器上**不做 git clone**（见 `docs/SERVER-DEPLOYMENT.md`），所以该文件不会随代码同步，
需单独 scp 一次：

```bash
# 本地 → 服务器
ssh ubuntu@111.231.14.63 'mkdir -p /opt/lune/data/geoip'
scp data/geoip/GeoLite2-City.mmdb ubuntu@111.231.14.63:/opt/lune/data/geoip/
ssh ubuntu@111.231.14.63 'cd /opt/lune && docker compose -f docker-compose.prod.yml restart backend'
```

按月更新时重复这一步即可，无需重新部署镜像。

---

## 缺失时的行为（降级，不是故障）

文件不存在或损坏时，后端**不会启动失败**，只打一条 WARN 并降级：

- 访问日志照常写入，`ip` / `path` / `user_agent` 正常
- `country` / `province` / `city` 留空
- 后台统计接口 `summary` 返回 `geoAvailable: false`，前端据此提示「未配置离线库」，
  而不是显示成「没有访问量」

这样即使忘记放库，站点功能与访问量统计也不受影响。

---

## 省份名称归一化

GeoLite2 返回的中文省名不统一（`浙江` 无后缀、`上海市` / `河北省` 带后缀，
港澳以「区」返回、台湾以城市返回），而 ECharts 中国地图 GeoJSON
（`lune-ui/public/maps/china.json`）要求**全称**（`浙江省` / `广西壮族自治区`）。
两者不一致时地图会整片空白。

归一化在 `com.lune.common.ChinaRegion` 完成，并在**写入时**就落库为全称，
因此统计查询可以直接 `GROUP BY province` 喂给地图。修改该映射后需要重启后端；
历史数据不会自动回填。

---

## 相关配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `GEOIP_DB_PATH` | `/app/geoip/GeoLite2-City.mmdb` | 容器内路径，对应 `app.geoip.path` |
| `VISIT_LOG_RETENTION_DAYS` | `90` | 访问日志保留天数，`0` 表示不清理。每日 04:20 分批清理 |

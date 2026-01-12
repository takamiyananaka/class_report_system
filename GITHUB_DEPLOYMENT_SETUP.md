# GitHub Actions 部署配置说明

本文档介绍了如何配置 GitHub Actions 自动部署工作流所需的 Secrets。

## 部署工作流概述

项目包含一个部署工作流：

1. `your-server-deploy.yml` - 针对您服务器环境的定制化部署工作流

## 需要配置的 Secrets

### 生产环境部署所需 Secrets

- `HOST`: 生产服务器 IP 地址或域名
- `USERNAME`: 服务器登录用户名
- `SSH_PRIVATE_KEY`: SSH 私钥内容
- `PORT`: SSH 端口（可选，默认为 22）

## 如何配置 Secrets

1. 进入 GitHub 仓库页面
2. 点击 "Settings" 标签页
3. 在左侧菜单中选择 "Secrets and variables"
4. 点击 "Actions" 
5. 点击 "New repository secret" 按钮
6. 按照上述列表添加所有必需的 Secrets

## SSH 密钥生成指南

如果还没有 SSH 密钥，可以按以下步骤生成：

```bash
# 生成新的 SSH 密钥对
ssh-keygen -t rsa -b 4096 -C "github-actions@your-domain.com" -f ~/.ssh/github_actions_key -N ""

# 查看公钥并将其添加到服务器的 ~/.ssh/authorized_keys 文件中
cat ~/.ssh/github_actions_key.pub

# 将私钥内容复制到 GitHub Secrets 中
cat ~/.ssh/github_actions_key
```

## 部署流程说明

### 部署流程

1. 当代码推送到 main 分支时触发
2. 自动构建项目并运行测试
3. 将 JAR 文件上传为构件
4. 通过 SSH 连接到生产服务器
5. 拉取最新的代码
6. 停止现有的应用程序进程（通过端口8080查找）
7. 备份当前版本的应用程序
8. 上传新的 JAR 文件
9. 使用 Maven 构建应用程序
10. 使用生产配置文件启动应用程序
11. 验证应用程序是否正常运行

## 服务器环境要求

根据您的部署脚本，服务器需要满足以下要求：

- Java 17 安装路径：`/www/server/java/jdk-17.0.8`
- 项目部署路径：`/www/app/class_report_system`
- 应用运行端口：8080
- 需要安装 `lsof` 命令用于查找端口占用进程
- 需要安装 Maven 环境用于构建

## 手动触发部署

除了自动触发外，也可以手动触发部署：

1. 进入 GitHub 仓库的 "Actions" 页面
2. 选择部署工作流
3. 点击 "Run workflow" 按钮
4. 选择分支并运行工作流

## 故障排除

如果部署失败，请检查以下几点：

1. 确认所有必需的 Secrets 已正确配置
2. 确认服务器可以从外部通过 SSH 访问
3. 确认服务器上有 `/www/server/java/jdk-17.0.8` Java 环境
4. 确认服务器上有 `/www/app/class_report_system` 目录
5. 检查 GitHub Actions 的日志以获取详细错误信息
6. 确认服务器上安装了 `lsof` 命令和 Maven
7. 确认服务器上有足够的磁盘空间

## 安全注意事项

1. 不要在代码中硬编码敏感信息
2. 定期轮换 SSH 密钥
3. 限制服务器上的最小权限原则
4. 监控部署活动日志
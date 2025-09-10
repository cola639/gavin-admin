
gavin-admin
├── api-boot          # 启动模块（SpringBootApplication 入口）
├── api-framework     # 框架配置（安全/数据源/异常/日志等）
├── api-common        # 公共工具类（通用 VO/DTO/Result/工具方法）
├── api-system        # 用户/权限/角色/菜单（核心RBAC）
├── api-monitor       # 系统监控（日志/在线用户/服务监控）
├── api-quartz        # 定时任务（可替换为 XXL-Job）
├── api-generator     # 代码生成器（开发效率工具）
├── api-business      # 业务模块聚合目录
├── api-thirdparty    # 第三方服务
└── pom.xml             # 父级依赖管理

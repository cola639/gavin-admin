
1. 项目结构 设计

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

依赖问题

1 由父模块统一管理版本，子模块只声明依赖，不写版本号

2 | 依赖类别                     | 示例依赖                                                                                            | 推荐 `<scope>`                     | 说明                            |
| ------------------------ | ----------------------------------------------------------------------------------------------- | -------------------------------- | ----------------------------- |
| **Spring Boot Starters** | `spring-boot-starter-web`<br>`spring-boot-starter-data-jpa`<br>`spring-boot-starter-validation` | **默认（compile）**                  | 这些是核心依赖，编译 & 运行都需要，直接默认 scope |
| **数据库驱动**                | `mysql:mysql-connector-j`                                                                       | **runtime**                      | 编译时不需要，只在运行时由 JDBC 连接使用       |
| **连接池 & JDBC**           | `spring-boot-starter-jdbc`<br>`HikariCP`                                                        | **默认**                           | 编译和运行都需要                      |
| **日志框架**                 | `spring-boot-starter-logging`                                                                   | **默认**                           | 编译 & 运行都需要                    |
| **Lombok**               | `org.projectlombok:lombok`                                                                      | **provided**                     | 只在编译期生成字节码，运行时不需要             |
| **MapStruct/注解处理器**      | `org.mapstruct:mapstruct-processor`                                                             | **provided**                     | 仅编译期使用，运行时不需要                 |
| **测试依赖**                 | `spring-boot-starter-test`<br>`junit-jupiter`<br>`mockito-core`                                 | **test**                         | 只在测试代码里生效，不会打包                |
| **开发工具**                 | `spring-boot-devtools`                                                                          | **runtime**（可选）                  | 只在开发时热部署用，生产环境一般排除            |
| **第三方工具包**               | `commons-lang3`<br>`guava`                                                                      | **默认**                           | 编译 & 运行都需要                    |
| **编译插件**                 | `maven-compiler-plugin`<br>`spring-boot-maven-plugin`                                           | **N/A**（放在 `<build>` 里，不用 scope） | 插件不作为依赖，而是构建配置                |

默认 scope（不写就是 compile）→ 业务必须的依赖

runtime → 运行时需要，编译期不需要（数据库驱动、devtools）

provided → 仅编译时需要，运行时不打包（Lombok、MapStruct）

test → 单元测试依赖

3 是那个组织在维护 有哪些著名的项目或者公司使用

4 jackjson lombook rewrite

5 （项目使用lombook jackjson 可以看情况能不能用）
  帮我改写成基于java17 springboot3.5 用更优雅的代码 改成英文注释 去掉作者信息 并给出全部改动前后对比
  解释下这段代码作用

6   querydsl-jpa 和   querydsl-apt 5.1.0 来加强jpa查询

7  项目是 java17 springboot3.5 考虑版本兼容性 帮我补充版本号 + Maven 坐标链接

8   项目是 java17 springboot3.5 在迁移JDK8的项目  怎么修复 给出原因
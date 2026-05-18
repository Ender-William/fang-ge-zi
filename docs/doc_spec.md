# 《鸽巢管家》Android 应用技术规格文档（Technical Specification）

> **文档版本**：v1.0  
> **编写日期**：2024年  
> **目标平台**：Android 8.0+ (API 26)  
> **目标用户**：养鸽爱好者（含老年用户群体）  

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术选型](#2-技术选型)
- [3. 系统架构](#3-系统架构)
- [4. 数据模型设计](#4-数据模型设计)
- [5. 家族图谱渲染方案](#5-家族图谱渲染方案)
- [6. 离线同步与备份策略](#6-离线同步与备份策略)
- [7. 项目结构](#7-项目结构)
- [8. 接口定义](#8-接口定义)
- [9. 性能与安全考量](#9-性能与安全考量)

---

## 1. 项目概述

### 1.1 应用定位
《鸽巢管家》是一款面向养鸽爱好者的移动管理工具，核心定位为**鸽舍数字化管理助手**。应用聚焦于鸽子位置追踪、家族谱系管理和可视化展示三大核心功能，特别针对老年养鸽人群进行体验优化。

### 1.2 核心功能模块

| 模块 | 功能描述 | 优先级 |
|------|---------|--------|
| 鸽棚管理 | 创建/编辑鸽棚结构，管理笼位分布 | P0 |
| 鸽子档案 | 记录鸽子基本信息、照片、环号等 | P0 |
| 位置记录 | 记录每只鸽子当前所在的鸽棚/笼位 | P0 |
| 家族关系 | 记录血缘关系（父母、配偶、后代） | P0 |
| 家族图谱 | 可视化树状/图谱展示家族关系 | P0 |
| 数据备份 | 本地备份、导出/导入功能 | P1 |
| 老年友好UI | 大字体、高对比度、简洁交互 | P0 |

### 1.3 约束条件

| 约束项 | 具体要求 |
|--------|---------|
| 最低Android版本 | API 26 (Android 8.0) |
| 目标设备内存 | 2GB - 4GB RAM |
| 网络要求 | 完全支持离线使用 |
| 存储空间 | 安装包 < 30MB，运行时缓存可控 |
| 语言支持 | 简体中文为主 |
| 特殊要求 | 适配老年用户视力、操作习惯 |

---

## 2. 技术选型

### 2.1 选型总览

| 技术领域 | 选定方案 | 备选方案 | 选型理由 |
|----------|---------|---------|---------|
| 编程语言 | **Kotlin** | Java | 官方推荐，语法简洁，空安全，协程支持 |
| 架构模式 | **MVVM** | MVP / MVI | 与Jetpack深度集成，生命周期感知，测试友好 |
| UI框架 | **XML Layout** | Jetpack Compose | 老年设备兼容性好，性能稳定，开发团队熟悉度高 |
| 本地数据库 | **Room (SQLite)** | Realm / 原生SQLite | 官方支持，类型安全，迁移方便，无需额外依赖 |
| 依赖注入 | **Hilt** | Koin / 手动注入 | 与Jetpack生态无缝集成，编译期安全 |
| 家族图谱 | **自定义View + 现有算法库** | 第三方图表库 | 灵活定制交互，完全控制渲染性能 |
| 异步处理 | **Kotlin Coroutines + Flow** | RxJava | 官方推荐，语法简洁，与生命周期集成 |
| 图片加载 | **Glide** | Coil / Picasso | 成熟稳定，缓存策略完善，低端设备适配好 |

### 2.2 详细选型分析

#### 2.2.1 编程语言：Kotlin

| 对比维度 | Kotlin | Java |
|---------|--------|------|
| 代码简洁度 | ★★★★★ 减少40%样板代码 | ★★★☆☆ 冗余较多 |
| 空安全 | ★★★★★ 编译期空指针防护 | ★★☆☆☆ 运行时风险 |
| 协程支持 | ★★★★★ 原生支持，轻量级线程 | ★★☆☆☆ 依赖回调/线程池 |
| 官方支持 | ★★★★★ Google首选语言 | ★★★★☆ 长期兼容 |
| 学习曲线 | ★★★☆☆ 对Java开发者友好 | — |
| 老年设备性能 | ★★★★☆ Kotlin运行时轻量 | ★★★★☆ 略优但差距小 |

**决策理由**：
1. Google官方推荐的Android首选语言，长期演进有保障
2. 空安全机制大幅减少运行时崩溃（对老年用户友好）
3. Kotlin Coroutines的轻量级特性特别适合中低端设备
4. 简洁语法降低代码维护成本

**兼容性说明**：
- Kotlin标准库增加约 1.2MB APK体积（使用ProGuard/R8可大幅缩减）
- 在API 26+设备上运行流畅，无额外性能负担

#### 2.2.2 架构模式：MVVM

```
┌─────────────────────────────────────────┐
│            Presentation 层               │
│  ┌──────────┐     ┌─────────────────┐  │
│  │  Fragment │────▶│     ViewModel   │  │
│  │  (View)   │◀────│  (State Holder) │  │
│  └──────────┘     └─────────────────┘  │
│                           │              │
│                           ▼              │
│                    ┌──────────────┐      │
│                    │   LiveData   │      │
│                    │    /Flow     │      │
│                    └──────────────┘      │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│              Domain 层                   │
│  ┌─────────────────────────────────┐   │
│  │         UseCase (可选)           │   │
│  │     (复杂业务逻辑编排)            │   │
│  └─────────────────────────────────┘   │
│                    │                    │
│                    ▼                    │
│  ┌─────────────────────────────────┐   │
│  │         Repository Interface     │   │
│  │      (数据访问抽象层)             │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│               Data 层                    │
│  ┌──────────┐      ┌─────────────────┐  │
│  │  Room DB  │◀────▶│ Repository Impl │  │
│  │ (Local)   │      │   (数据操作)     │  │
│  └──────────┘      └─────────────────┘  │
│                                            │
│  ┌──────────┐      ┌─────────────────┐   │
│  │  Shared  │◀────▶│   DataSource    │   │
│  │Preference│      │   (本地备份)     │   │
│  └──────────┘      └─────────────────┘   │
└─────────────────────────────────────────┘
```

**选择MVVM而非MVP/MVI的理由**：

| 维度 | MVVM | MVP | MVI |
|------|------|-----|-----|
| 生命周期管理 | ★★★★★ ViewModel自动处理 | ★★☆☆☆ 需手动管理 | ★★★★☆ 需配合处理 |
| Jetpack集成 | ★★★★★ 深度集成 | ★★★☆☆ 需桥接 | ★★★☆☆ 需额外封装 |
| 单向数据流 | ★★★☆☆ 天然支持LiveData | ★★☆☆☆ 需手动实现 | ★★★★★ 强制单向 |
| 学习曲线 | ★★★☆☆ 中等 | ★★☆☆☆ 较陡 | ★★★★☆ 中等 |
| 测试性 | ★★★★★ ViewModel易测试 | ★★★★☆ Presenter易测试 | ★★★★★ 纯函数易测 |
| 老年设备适配 | ★★★★★ 内存友好 | ★★★☆☆ 接口引用风险 | ★★★★☆ 状态对象开销 |

**决策理由**：
1. **生命周期安全**：ViewModel自动处理配置变更（如屏幕旋转），避免老年用户误操作导致数据丢失
2. **Jetpack原生支持**：DataBinding/ViewBinding/LiveData与ViewModel无缝协作
3. **内存安全**：ViewModel持有Activity引用不会导致泄漏，适合长时间挂起的老年使用场景
4. **平衡性**：相比MVI的严格单向数据流，MVVM在简单CRUD应用中代码量更少；相比MVP，避免了Presenter-View双向引用的内存泄漏风险

#### 2.2.3 UI框架：XML Layout

**选择XML而非Jetpack Compose的理由**：

| 维度 | XML Layout | Jetpack Compose |
|------|-----------|-----------------|
| 最低API支持 | API 21+（目标API 26完全兼容） | API 21+（需额外兼容包） |
| 运行时性能 | ★★★★★ 成熟稳定，低端设备验证充分 | ★★★☆☆ 需要较高GPU渲染能力 |
| 内存占用 | ★★★★★ 较低 | ★★★☆☆ XML+Compose双栈运行时内存更高 |
| APK体积 | ★★★★★ 无额外依赖 | ★★★☆☆ +2~4MB |
| 老年设备适配 | ★★★★★ 大量低端设备实测稳定 | ★★☆☆☆ 低端设备可能掉帧 |
| 社区生态 | ★★★★★ 无障碍/老年UI方案成熟 | ★★★★☆ 生态在追赶 |

> **重要提示**：本应用目标用户多为老年群体，设备多为中低端Android机。Jetpack Compose虽然代表未来方向，但在4GB RAM以下设备上存在渲染性能风险。为确保老年用户的流畅体验，选择经过充分验证的XML Layout方案。

#### 2.2.4 本地数据库：Room

```kotlin
// Room示例：定义Pigeon实体
@Entity(tableName = "pigeons")
data class Pigeon(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "ring_number") val ringNumber: String,      // 环号（唯一标识）
    @ColumnInfo(name = "name") val name: String,                    // 鸽子昵称
    @ColumnInfo(name = "color") val color: String?,                 // 羽色
    @ColumnInfo(name = "gender") val gender: Gender,                // 性别枚举
    @ColumnInfo(name = "birth_date") val birthDate: Long?,          // 出生日期时间戳
    @ColumnInfo(name = "photo_path") val photoPath: String?,        // 本地照片路径
    @ColumnInfo(name = "status") val status: PigeonStatus,          // 状态：在养/已售/已故
    @ColumnInfo(name = "notes") val notes: String?,                 // 备注
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
```

**选择Room的理由**：

| 维度 | Room | 原生SQLite | Realm |
|------|------|-----------|-------|
| 编译期SQL检查 | ★★★★★ 编译期验证 | ★☆☆☆☆ 运行时错误 | ★★★☆☆ 部分检查 |
| 类型安全 | ★★★★★ DAO返回类型安全 | ★★☆☆☆ 手动Cursor解析 | ★★★★☆ 需定义模型 |
| 迁移支持 | ★★★★★ Migration API完善 | ★★☆☆☆ 完全手动 | ★★★☆☆ 自动但不可控 |
| 协程支持 | ★★★★★ 原生suspend函数 | ★★☆☆☆ 自行封装 | ★★★☆☆ 需桥接 |
| 额外体积 | ★★★★★ 内置于AndroidX | 零 | ★★☆☆☆ +4~8MB |
| 学习成本 | ★★★☆☆ 中等 | ★★★★☆ 需SQL基础 | ★★★☆☆ 需理解对象映射 |

#### 2.2.5 依赖注入：Hilt

**选择Hilt的理由**：
1. **官方出品**：Google官方DI框架，与Android组件深度集成
2. **编译期安全**：错误在编译期发现，而非运行时
3. **简化Dagger**：基于Dagger2但大幅简化配置，自动处理Android组件生命周期
4. **ViewModel注入**：`@HiltViewModel`注解使ViewModel依赖注入零样板代码

```kotlin
// Hilt应用入口
@HiltAndroidApp
class PigeonNestApp : Application()

// ViewModel注入
@HiltViewModel
class PigeonListViewModel @Inject constructor(
    private val pigeonRepository: PigeonRepository,
    private val getPigeonListUseCase: GetPigeonListUseCase
) : ViewModel() { ... }

// Repository注入
class PigeonRepositoryImpl @Inject constructor(
    private val pigeonDao: PigeonDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PigeonRepository { ... }
```

#### 2.2.6 家族图谱渲染方案

| 方案 | 优点 | 缺点 | 适用性评估 |
|------|------|------|-----------|
| **自定义View（推荐）** | 完全控制渲染，性能最优，交互定制灵活 | 开发工作量大 | ★★★★★ 适合本应用专注的场景 |
| MPAndroidChart | 图表类型丰富，社区活跃 | 不适合家族树/关系图谱 | ★★☆☆☆ 针对统计图表 |
| AndroidTreeView | 树形结构展示 | 不支持图谱交叉关系（如配偶关系） | ★★★☆☆ 功能不足 |
| WebView + ECharts/D3.js | 功能强大，渲染精美 | 内存占用高，低端设备卡顿 | ★★☆☆☆ 内存敏感 |
| GraphView | 关系图有一定支持 | 停更已久，社区不活跃 | ★★☆☆☆ 维护风险 |

**决策**：采用**自定义View方案**，核心理由：
1. 家族关系图谱（含配偶连接、多代层级）没有现成成熟方案
2. 自定义View完全控制Canvas渲染，在低端设备上性能最佳
3. 可以精确设计适合老年用户的交互（大点击区域、简化手势）
4. 无额外依赖，APK体积最小

> 参考算法：采用**Reingold-Tilford树布局算法**的扩展版本，增加对配偶（cross-link）关系的处理。

---

## 3. 系统架构

### 3.1 整体架构图

```
╔══════════════════════════════════════════════════════════════════╗
║                      《鸽巢管家》应用层                           ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  ┌──────────────────────────────────────────────────────────┐   ║
║  │                   Presentation Layer (UI层)               │   ║
║  │                                                          │   ║
║  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   ║
║  │  │ PigeonList   │  │  PigeonDetail│  │ FamilyGraph  │   │   ║
║  │  │   Fragment   │  │   Fragment   │  │   Fragment   │   │   ║
║  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │   ║
║  │         │                 │                 │            │   ║
║  │  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐   │   ║
║  │  │PigeonListView│  │PigeonDetailVM│  │FamilyGraphVM │   │   ║
║  │  │    Model     │  │              │  │              │   │   ║
║  │  └──────────────┘  └──────────────┘  └──────────────┘   │   ║
║  │                                                          │   ║
║  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │   ║
║  │  │ LoftManage   │  │ LocationSet  │  │  Backup      │   │   ║
║  │  │   Fragment   │  │   Fragment   │  │  Activity    │   │   ║
║  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │   ║
║  │         │                 │                 │            │   ║
║  │  ┌──────▼───────┐  ┌──────▼───────┐  ┌──────▼───────┐   │   ║
║  │  │LoftManageView│  │LocationSetVM │  │BackupViewModel│  │   ║
║  │  │    Model     │  │              │  │              │   │   ║
║  │  └──────────────┘  └──────────────┘  └──────────────┘   │   ║
║  └──────────────────────────────────────────────────────────┘   ║
║                              │                                   ║
║                              ▼                                   ║
║  ┌──────────────────────────────────────────────────────────┐   ║
║  │                    Domain Layer (领域层)                   │   ║
║  │                                                          │   ║
║  │  ┌──────────────────────────────────────────────────┐   │   ║
║  │  │              UseCase Layer                        │   │   ║
║  │  │  • GetPigeonListUseCase                          │   │   ║
║  │  │  • GetPigeonDetailUseCase                        │   │   ║
║  │  │  • ManageFamilyRelationUseCase                   │   │   ║
║  │  │  • GetFamilyGraphDataUseCase                     │   │   ║
║  │  │  • ManageLocationUseCase                         │   │   ║
║  │  │  • BackupRestoreUseCase                          │   │   ║
║  │  └──────────────────────────────────────────────────┘   │   ║
║  │                                                          │   ║
║  │  ┌──────────────────────────────────────────────────┐   │   ║
║  │  │              Repository Interface                 │   │   ║
║  │  │  • PigeonRepository                              │   │   ║
║  │  │  • FamilyRepository                              │   │   ║
║  │  │  • LocationRepository                            │   │   ║
║  │  │  • LoftRepository                                │   │   ║
║  │  │  • BackupRepository                              │   │   ║
║  │  └──────────────────────────────────────────────────┘   │   ║
║  │                                                          │   ║
║  │  ┌──────────────────────────────────────────────────┐   │   ║
║  │  │              Domain Model (实体/值对象)            │   │   ║
║  │  │  • Pigeon (鸽子实体)                              │   │   ║
║  │  │  • FamilyRelation (家族关系值对象)                │   │   ║
║  │  │  • Location (位置值对象)                          │   │   ║
║  │  │  • FamilyGraphNode/Edge (图谱数据结构)            │   │   ║
║  │  └──────────────────────────────────────────────────┘   │   ║
║  └──────────────────────────────────────────────────────────┘   ║
║                              │                                   ║
║                              ▼                                   ║
║  ┌──────────────────────────────────────────────────────────┐   ║
║  │                     Data Layer (数据层)                    │   ║
║  │                                                          │   ║
║  │  ┌──────────────────────────────────────────────────┐   │   ║
║  │  │           Local DataSource                        │   │   ║
║  │  │                                                  │   │   ║
║  │  │  ┌──────────┐ ┌──────────┐ ┌──────────────────┐ │   │   ║
║  │  │  │PigeonDao │ │FamilyDao │ │  LoftDao         │ │   │   ║
║  │  │  │          │ │          │ │                  │ │   │   ║
║  │  │  └────┬─────┘ └────┬─────┘ └────────┬─────────┘ │   │   ║
║  │  │       └─────────────┼────────────────┘           │   │   ║
║  │  │                     │                            │   │   ║
║  │  │              ┌──────▼──────┐                     │   │   ║
║  │  │              │  Room DB    │                     │   │   ║
║  │  │              │(SQLite)     │                     │   │   ║
║  │  │              └─────────────┘                     │   │   ║
║  │  └──────────────────────────────────────────────────┘   │   ║
║  │                                                          │   ║
║  │  ┌──────────────────────────────────────────────────┐   │   ║
║  │  │           File DataSource                        │   │   ║
║  │  │  • BackupExporter (JSON导出)                    │   │   ║
║  │  │  • BackupImporter (JSON导入)                    │   │   ║
║  │  │  • PhotoStorageManager (照片管理)               │   │   ║
║  │  │  • SharedPreferences (配置存储)                 │   │   ║
║  │  └──────────────────────────────────────────────────┘   │   ║
║  └──────────────────────────────────────────────────────────┘   ║
║                                                                  ║
╠══════════════════════════════════════════════════════════════════╣
║                      Android Framework                           ║
║         (Activity/Fragment Lifecycle, Canvas, Storage)           ║
╚══════════════════════════════════════════════════════════════════╝
```

### 3.2 各层职责定义

#### 3.2.1 Presentation Layer（展示层）

| 组件 | 职责 | 约束 |
|------|------|------|
| **Fragment** | UI渲染、用户输入响应、生命周期管理 | 不包含业务逻辑，仅调用ViewModel方法 |
| **ViewModel** | 持有UI状态、处理用户意图、调用UseCase/Repository | 不持有Activity/Fragment引用，不直接操作数据库 |
| **UI State** | 密封类定义的UI状态（Loading/Success/Error） | 不可变数据类，通过LiveData/Flow发射 |

**UI状态设计模式**：
```kotlin
// 统一UI状态封装
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val retryable: Boolean = true) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}

// 鸽子列表状态
data class PigeonListUiState(
    val pigeons: List<Pigeon> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filterStatus: PigeonStatus? = null
)
```

#### 3.2.2 Domain Layer（领域层）

| 组件 | 职责 | 约束 |
|------|------|------|
| **UseCase** | 封装单一业务逻辑，编排多个Repository操作 | 每个UseCase只做一件事，可组合 |
| **Repository Interface** | 定义数据访问契约，屏蔽数据源细节 | 纯接口，位于Domain层 |
| **Domain Model** | 定义核心业务实体和数据结构 | 不包含Android框架依赖，纯Kotlin类 |

**领域层独立性原则**：
- Domain层不依赖Android框架，可以独立单元测试
- Domain层定义Repository接口，由Data层提供实现
- UseCase可以被ViewModel直接调用

#### 3.2.3 Data Layer（数据层）

| 组件 | 职责 | 约束 |
|------|------|------|
| **Repository Impl** | 实现Repository接口，协调多个DataSource | 处理数据转换（Entity ↔ Domain Model） |
| **DAO** | 数据库访问对象，定义CRUD操作 | 返回Flow或suspend函数，支持协程 |
| **Entity** | Room数据库表结构定义 | 与Domain Model分离，避免数据库细节泄露 |
| **File DataSource** | 文件读写操作（备份/照片） | 统一异常处理，IO操作在Dispatchers.IO上 |

### 3.3 模块划分

采用**单模块（App Module）+ 功能包**的划分方式，理由：
1. 本应用功能聚焦，无需多模块的解耦收益
2. 减少Gradle构建复杂度，加快老年设备开发调试速度
3. 包级隔离足够满足当前业务复杂度

```
com.pigeonnest.android/
├── presentation/          # 展示层包
│   ├── pigeonlist/       # 鸽子列表功能包
│   ├── pigeondetail/     # 鸽子详情功能包
│   ├── familygraph/      # 家族图谱功能包
│   ├── loftmanage/       # 鸽棚管理功能包
│   ├── locationset/      # 位置设置功能包
│   ├── backup/           # 备份恢复功能包
│   └── common/           # 共享UI组件（适配器、自定义View等）
├── domain/               # 领域层包
│   ├── model/            # 领域模型
│   ├── repository/       # Repository接口
│   └── usecase/          # UseCase类
├── data/                 # 数据层包
│   ├── local/            # 本地数据源
│   │   ├── dao/          # Room DAO
│   │   ├── entity/       # Room Entity
│   │   └── database/     # Room Database
│   ├── repository/       # Repository实现
│   └── file/             # 文件数据源
├── di/                   # 依赖注入模块
└── utils/                # 工具类
```

### 3.4 模块间通信方式

| 通信场景 | 方式 | 说明 |
|---------|------|------|
| UI → ViewModel | 方法调用 | Fragment调用ViewModel的public方法 |
| ViewModel → UI | LiveData/StateFlow | 单向数据流，UI观察状态变化 |
| ViewModel → UseCase | 方法调用 | 直接调用UseCase的invoke() |
| UseCase → Repository | 接口调用 | 依赖Repository接口，运行时注入实现 |
| Repository → DAO | 方法调用 | Repository持有DAO引用 |
| 跨Fragment通信 | 共享ViewModel / Navigation Result | 避免EventBus等松散耦合方案 |

---

## 4. 数据模型设计

### 4.1 核心实体关系图（ER Diagram）

```
┌──────────────────┐         ┌─────────────────────┐
│      Lofts       │         │      Pigeons        │
├──────────────────┤         ├─────────────────────┤
│ PK  id: String   │◀────────┤ FK  loft_id: String?│
│     name: String │   1:N   │     cage_number: Str?│
│     location: Str│         ├─────────────────────┤
│     description  │         │ PK  id: String      │
│     sort_order   │         │     ring_number: Str│
│     created_at   │         │     name: String    │
└──────────────────┘         │     color: String?  │
                             │     gender: Int     │
                             │     birth_date: Long│
                             │     photo_path: Str?│
                             │     status: Int     │
                             │     notes: String?  │
                             │     created_at: Long│
                             │     updated_at: Long│
                             └─────────────────────┘
                                        │
                                        │ 1:N（父→子）
                                        │
                                        ▼
┌──────────────────┐         ┌─────────────────────┐
│  FamilyRelations │         │   PigeonPhotos      │
├──────────────────┤         ├─────────────────────┤
│ PK  id: String   │         │ PK  id: String      │
│ FK  pigeon_id:Str├────────▶│ FK  pigeon_id: String│
│ FK  father_id:Str│  1:1    │     photo_path: Str │
│ FK  mother_id:Str│（扩展） │     caption: String?│
│ FK  mate_id:Str  │         │     taken_date: Long│
│     relation_type│         │     created_at: Long│
│     created_at   │         └─────────────────────┘
└──────────────────┘

关系说明：
• Lofts 1:N Pigeons    — 一个鸽棚可容纳多只鸽子，一只鸽子属于一个鸽棚
• Pigeons 1:N FamilyRelations — 一只鸽子可有多条家族关系记录
• Pigeons 1:N PigeonPhotos — 一只鸽子可有多张照片
• FamilyRelations中 father_id / mother_id / mate_id 均为对Pigeons.id的外键（自关联）
```

### 4.2 Room Entity 定义

#### 4.2.1 PigeonEntity（鸽子实体）

```kotlin
@Entity(
    tableName = "pigeons",
    indices = [
        Index(value = ["ring_number"], unique = true),
        Index(value = ["loft_id"]),
        Index(value = ["status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = LoftEntity::class,
            parentColumns = ["id"],
            childColumns = ["loft_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class PigeonEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "ring_number")
    val ringNumber: String,               // 足环号（全局唯一标识）

    @ColumnInfo(name = "name")
    val name: String,                     // 鸽子名称/昵称

    @ColumnInfo(name = "color")
    val color: String?,                   // 羽色（如"雨点"、"红轮"等）

    @ColumnInfo(name = "gender")
    val gender: Int,                      // 0=未知, 1=雄, 2=雌

    @ColumnInfo(name = "birth_date")
    val birthDate: Long?,                 // 出生日期（时间戳，毫秒）

    @ColumnInfo(name = "photo_path")
    val photoPath: String?,               // 主照片本地路径

    @ColumnInfo(name = "loft_id")
    val loftId: String?,                  // 所属鸽棚ID

    @ColumnInfo(name = "cage_number")
    val cageNumber: String?,              // 笼位编号

    @ColumnInfo(name = "status")
    val status: Int,                      // 0=在养, 1=已售, 2=已故, 3=赠送

    @ColumnInfo(name = "notes")
    val notes: String?,                   // 备注信息

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### 4.2.2 LoftEntity（鸽棚实体）

```kotlin
@Entity(tableName = "lofts")
data class LoftEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,                     // 鸽棚名称

    @ColumnInfo(name = "location")
    val location: String?,                // 鸽棚位置描述

    @ColumnInfo(name = "description")
    val description: String?,             // 描述

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,               // 排序序号

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

#### 4.2.3 FamilyRelationEntity（家族关系实体）

```kotlin
@Entity(
    tableName = "family_relations",
    indices = [
        Index(value = ["pigeon_id"]),
        Index(value = ["father_id"]),
        Index(value = ["mother_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigeon_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["father_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["mother_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["mate_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class FamilyRelationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "pigeon_id")
    val pigeonId: String,                 // 关系所属鸽子ID

    @ColumnInfo(name = "father_id")
    val fatherId: String?,                // 父亲鸽子ID

    @ColumnInfo(name = "mother_id")
    val motherId: String?,                // 母亲鸽子ID

    @ColumnInfo(name = "mate_id")
    val mateId: String?,                  // 配偶鸽子ID（当前配对）

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

#### 4.2.4 PigeonPhotoEntity（鸽子照片实体）

```kotlin
@Entity(
    tableName = "pigeon_photos",
    indices = [Index(value = ["pigeon_id"])],
    foreignKeys = [
        ForeignKey(
            entity = PigeonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pigeon_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PigeonPhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "pigeon_id")
    val pigeonId: String,

    @ColumnInfo(name = "photo_path")
    val photoPath: String,                // 照片本地存储路径

    @ColumnInfo(name = "caption")
    val caption: String?,                 // 照片说明

    @ColumnInfo(name = "taken_date")
    val takenDate: Long?,                 // 拍摄日期

    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean = false,       // 是否为主照片

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

### 4.3 领域模型（Domain Model）

领域模型与数据库实体分离，屏蔽数据层细节：

```kotlin
// ========== 枚举定义 ==========

enum class Gender(val code: Int, val displayName: String) {
    UNKNOWN(0, "未知"),
    MALE(1, "雄"),
    FEMALE(2, "雌");

    companion object {
        fun fromCode(code: Int): Gender = values().find { it.code == code } ?: UNKNOWN
    }
}

enum class PigeonStatus(val code: Int, val displayName: String, val colorRes: Int) {
    ACTIVE(0, "在养", R.color.status_active),
    SOLD(1, "已售", R.color.status_sold),
    DECEASED(2, "已故", R.color.status_deceased),
    GIFTED(3, "赠送", R.color.status_gifted);

    companion object {
        fun fromCode(code: Int): PigeonStatus = values().find { it.code == code } ?: ACTIVE
    }
}

// ========== 领域实体 ==========

/**
 * 鸽子领域模型
 * 包含完整的鸽子信息和关联信息
 */
data class Pigeon(
    val id: String,
    val ringNumber: String,               // 足环号
    val name: String,                     // 名称
    val color: String?,                   // 羽色
    val gender: Gender,                   // 性别
    val birthDate: Long?,                 // 出生日期
    val photoPath: String?,               // 主照片路径
    val loft: Loft?,                      // 所属鸽棚（嵌入对象）
    val cageNumber: String?,              // 笼位号
    val status: PigeonStatus,             // 状态
    val notes: String?,                   // 备注
    val createdAt: Long,
    val updatedAt: Long,
    // 关联信息（查询时填充）
    val familyRelation: FamilyRelation? = null,
    val photos: List<PigeonPhoto> = emptyList()
)

/**
 * 鸽棚领域模型
 */
data class Loft(
    val id: String,
    val name: String,
    val location: String?,
    val description: String?,
    val sortOrder: Int,
    val pigeonCount: Int = 0             // 关联统计：鸽棚内鸽子数量
)

/**
 * 家族关系领域模型
 */
data class FamilyRelation(
    val id: String,
    val pigeonId: String,
    val father: PigeonBrief?,             // 父亲简要信息
    val mother: PigeonBrief?,             // 母亲简要信息
    val mate: PigeonBrief?,               // 配偶简要信息
    val children: List<PigeonBrief> = emptyList()  // 后代列表
)

/**
 * 鸽子简要信息（用于关系引用，避免循环依赖）
 */
data class PigeonBrief(
    val id: String,
    val ringNumber: String,
    val name: String,
    val gender: Gender,
    val photoPath: String?
)

/**
 * 照片领域模型
 */
data class PigeonPhoto(
    val id: String,
    val pigeonId: String,
    val photoPath: String,
    val caption: String?,
    val takenDate: Long?,
    val isPrimary: Boolean
)
```

### 4.4 数据库表结构汇总

| 表名 | 主要字段 | 索引 | 记录数预估 |
|------|---------|------|-----------|
| **lofts** | id, name, location, sort_order | id(PK) | 10-50条 |
| **pigeons** | id, ring_number, name, color, gender, birth_date, photo_path, loft_id, cage_number, status, notes | id(PK), ring_number(UQ), loft_id(FK), status | 100-2000条 |
| **family_relations** | id, pigeon_id, father_id, mother_id, mate_id | id(PK), pigeon_id, father_id, mother_id | 100-2000条 |
| **pigeon_photos** | id, pigeon_id, photo_path, caption, is_primary | id(PK), pigeon_id(FK) | 500-5000条 |

### 4.5 DAO 接口设计

```kotlin
@Dao
interface PigeonDao {

    // 基础CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pigeon: PigeonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pigeons: List<PigeonEntity>)

    @Update
    suspend fun update(pigeon: PigeonEntity)

    @Delete
    suspend fun delete(pigeon: PigeonEntity)

    @Query("DELETE FROM pigeons WHERE id = :pigeonId")
    suspend fun deleteById(pigeonId: String)

    // 查询操作（返回Flow实现实时更新）
    @Query("SELECT * FROM pigeons ORDER BY updated_at DESC")
    fun getAllPigeonsFlow(): Flow<List<PigeonEntity>>

    @Query("SELECT * FROM pigeons WHERE id = :pigeonId")
    suspend fun getById(pigeonId: String): PigeonEntity?

    @Query("SELECT * FROM pigeons WHERE id = :pigeonId")
    fun getByIdFlow(pigeonId: String): Flow<PigeonEntity?>

    // 搜索查询
    @Query("""
        SELECT * FROM pigeons 
        WHERE name LIKE '%' || :query || '%' 
           OR ring_number LIKE '%' || :query || '%'
           OR color LIKE '%' || :query || '%'
           OR cage_number LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
    """)
    fun searchPigeons(query: String): Flow<List<PigeonEntity>>

    // 按状态筛选
    @Query("SELECT * FROM pigeons WHERE status = :status ORDER BY updated_at DESC")
    fun getByStatus(status: Int): Flow<List<PigeonEntity>>

    // 按鸽棚筛选
    @Query("SELECT * FROM pigeons WHERE loft_id = :loftId ORDER BY cage_number")
    fun getByLoft(loftId: String): Flow<List<PigeonEntity>>

    // 统计查询
    @Query("SELECT COUNT(*) FROM pigeons WHERE status = 0")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM pigeons WHERE loft_id = :loftId")
    suspend fun getCountByLoft(loftId: String): Int
}

@Dao
interface FamilyRelationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: FamilyRelationEntity)

    @Update
    suspend fun update(relation: FamilyRelationEntity)

    @Query("SELECT * FROM family_relations WHERE pigeon_id = :pigeonId")
    suspend fun getByPigeonId(pigeonId: String): FamilyRelationEntity?

    @Query("SELECT * FROM family_relations WHERE father_id = :pigeonId OR mother_id = :pigeonId")
    suspend fun getChildrenRelations(pigeonId: String): List<FamilyRelationEntity>

    @Query("SELECT * FROM family_relations WHERE mate_id = :pigeonId")
    suspend fun getMateRelations(pigeonId: String): List<FamilyRelationEntity>

    @Query("DELETE FROM family_relations WHERE pigeon_id = :pigeonId")
    suspend fun deleteByPigeonId(pigeonId: String)
}

@Dao
interface LoftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loft: LoftEntity)

    @Update
    suspend fun update(loft: LoftEntity)

    @Delete
    suspend fun delete(loft: LoftEntity)

    @Query("SELECT * FROM lofts ORDER BY sort_order")
    fun getAllFlow(): Flow<List<LoftEntity>>

    @Query("SELECT * FROM lofts WHERE id = :loftId")
    suspend fun getById(loftId: String): LoftEntity?

    @Query("UPDATE lofts SET sort_order = :order WHERE id = :loftId")
    suspend fun updateSortOrder(loftId: String, order: Int)
}
```

### 4.6 Room Database 定义

```kotlin
@Database(
    entities = [
        PigeonEntity::class,
        LoftEntity::class,
        FamilyRelationEntity::class,
        PigeonPhotoEntity::class
    ],
    version = 1,
    exportSchema = true  // 导出schema用于版本控制
)
@TypeConverters(DateConverter::class)
abstract class PigeonNestDatabase : RoomDatabase() {
    abstract fun pigeonDao(): PigeonDao
    abstract fun familyRelationDao(): FamilyRelationDao
    abstract fun loftDao(): LoftDao
    abstract fun pigeonPhotoDao(): PigeonPhotoDao

    companion object {
        const val DATABASE_NAME = "pigeon_nest.db"
    }
}

/**
 * 类型转换器
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
```

---

## 5. 家族图谱渲染方案

### 5.1 技术方案对比

| 对比维度 | 自定义View（Canvas） | MPAndroidChart | WebView+ECharts |
|---------|---------------------|----------------|-----------------|
| 家族树支持 | ★★★★★ 完全定制 | ★☆☆☆☆ 不支持 | ★★★★☆ 需定制 |
| 交叉关系（配偶） | ★★★★★ 可绘制连线 | ★☆☆☆☆ 不支持 | ★★★★☆ 支持 |
| 低端设备性能 | ★★★★★ 原生Canvas | ★★★★☆ 良好 | ★★☆☆☆ 内存高 |
| 交互定制（老年友好） | ★★★★★ 完全控制 | ★★☆☆☆ 受限 | ★★★☆☆ 需JS桥接 |
| 缩放/拖拽 | ★★★★★ Matrix实现 | ★★★☆☆ 有限 | ★★★★★ 内置 |
| 开发工作量 | ★★☆☆☆ 较大 | ★★★★☆ 小（但不适用） | ★★★☆☆ 中等 |
| APK体积影响 | 零额外依赖 | ~1MB | ~5MB+ |
| 维护成本 | ★★★☆☆ 需自行维护 | 外部依赖 | ★★☆☆☆ 双栈维护 |

**最终决策**：采用 **自定义View（Canvas 2D渲染）** 方案。

### 5.2 渲染架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                  FamilyGraphFragment                         │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              FamilyGraphView (自定义View)             │   │
│  │                                                      │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │            Canvas渲染层                        │   │   │
│  │  │                                               │   │   │
│  │  │  ┌───────────┐     ┌───────────┐            │   │   │
│  │  │  │ 节点绘制   │     │ 连线绘制   │            │   │   │
│  │  │  │ drawNode()│     │drawEdge() │            │   │   │
│  │  │  └───────────┘     └───────────┘            │   │   │
│  │  │                                               │   │   │
│  │  │  ┌───────────┐     ┌───────────┐            │   │   │
│  │  │  │ 文字绘制   │     │ 图片绘制   │            │   │   │
│  │  │  │drawText() │     │drawAvatar()│            │   │   │
│  │  │  └───────────┘     └───────────┘            │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  │                                                      │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │            变换控制器 (Matrix)                  │   │   │
│  │  │  • 缩放 (ScaleGestureDetector)                │   │   │
│  │  │  • 拖拽 (GestureDetector.onScroll)            │   │   │
│  │  │  • 双击缩放 (GestureDetector.onDoubleTap)     │   │   │
│  │  │  • 当前变换矩阵 (currentMatrix)               │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  │                                                      │   │
│  │  ┌──────────────────────────────────────────────┐   │   │
│  │  │            点击检测 (HitTest)                   │   │   │
│  │  │  • 节点点击区域计算                            │   │   │
│  │  │  • 坐标系变换（屏幕→画布）                     │   │   │
│  │  │  • 点击回调 (OnNodeClickListener)             │   │   │
│  │  └──────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              GraphLayoutManager                      │   │
│  │         （布局计算引擎，独立职责分离）                  │   │
│  │  • 计算节点位置（Reingold-Tilford算法）              │   │
│  │  • 计算连线坐标                                      │   │
│  │  • 处理配偶关系的水平偏移                            │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 5.3 核心算法：Reingold-Tilford 树布局

Reingold-Tilford算法是经典的树形布局算法，本应用在此基础上扩展了**配偶关系（mate）**的交叉连线处理。

```kotlin
/**
 * 图谱节点布局数据
 */
data class GraphNode(
    val pigeonId: String,
    val pigeonBrief: PigeonBrief,
    var x: Float = 0f,                   // 布局计算的X坐标
    var y: Float = 0f,                   // 布局计算的Y坐标
    var level: Int = 0,                  // 层级（代数）
    val children: MutableList<GraphNode> = mutableListOf(),
    var father: GraphNode? = null,
    var mother: GraphNode? = null,
    var mate: GraphNode? = null
) {
    // 节点显示尺寸（DP转换后的像素值）
    companion object {
        const val NODE_WIDTH = 200f       // 节点宽度（适配老年用户大点击区域）
        const val NODE_HEIGHT = 100f      // 节点高度
        const val HORIZONTAL_GAP = 60f    // 水平间距
        const val VERTICAL_GAP = 120f     // 垂直间距（代数间距）
        const val MATE_GAP = 40f          // 配偶间间距
    }
}

/**
 * 图谱连线数据
 */
data class GraphEdge(
    val fromNode: GraphNode,
    val toNode: GraphNode,
    val type: EdgeType
) {
    enum class EdgeType {
        PARENT_CHILD,     // 父母-子女连线
        MATE              // 配偶连线
    }
}

/**
 * 布局管理器
 * 基于Reingold-Tilford算法，支持配偶关系展示
 */
class GraphLayoutManager {

    /**
     * 计算整个图谱的布局坐标
     * @param rootNode 图谱根节点（选定鸽子的祖先链顶端）
     * @return 布局结果：所有节点的坐标
     */
    fun calculateLayout(rootNode: GraphNode): LayoutResult {
        // 第一步：自底向上计算每个节点的宽度和子树轮廓
        calculateSubtreeWidth(rootNode)

        // 第二步：自顶向下计算每个节点的X/Y坐标
        assignCoordinates(rootNode, 0f, 0f)

        // 第三步：计算配偶节点位置（水平偏移）
        layoutMateNodes(rootNode)

        // 第四步：收集所有连线
        val edges = collectEdges(rootNode)

        // 第五步：计算整个图谱的边界框
        val bounds = calculateBounds(rootNode)

        return LayoutResult(rootNode, edges, bounds)
    }

    /**
     * 自底向上计算子树宽度
     * 确保同级节点不重叠
     */
    private fun calculateSubtreeWidth(node: GraphNode): Float {
        if (node.children.isEmpty()) {
            node.subtreeWidth = GraphNode.NODE_WIDTH
            return node.subtreeWidth
        }

        var childrenWidth = 0f
        node.children.forEachIndexed { index, child ->
            val childWidth = calculateSubtreeWidth(child)
            childrenWidth += childWidth
            if (index < node.children.size - 1) {
                childrenWidth += GraphNode.HORIZONTAL_GAP
            }
        }

        node.subtreeWidth = maxOf(GraphNode.NODE_WIDTH, childrenWidth)
        return node.subtreeWidth
    }

    /**
     * 自顶向下分配坐标
     */
    private fun assignCoordinates(node: GraphNode, startX: Float, level: Int) {
        node.level = level
        node.y = level * (GraphNode.NODE_HEIGHT + GraphNode.VERTICAL_GAP)

        val childrenTotalWidth = node.children.sumOf { it.subtreeWidth.toDouble() }.toFloat() +
                (node.children.size - 1) * GraphNode.HORIZONTAL_GAP

        var currentX = startX + (node.subtreeWidth - childrenTotalWidth) / 2

        node.x = startX + node.subtreeWidth / 2 - GraphNode.NODE_WIDTH / 2

        node.children.forEach { child ->
            assignCoordinates(child, currentX, level + 1)
            currentX += child.subtreeWidth + GraphNode.HORIZONTAL_GAP
        }
    }

    /**
     * 配偶节点布局：将配偶放置在右侧
     */
    private fun layoutMateNodes(node: GraphNode) {
        node.mate?.let { mate ->
            mate.y = node.y
            mate.x = node.x + GraphNode.NODE_WIDTH + GraphNode.MATE_GAP
            mate.level = node.level
        }
        node.children.forEach { layoutMateNodes(it) }
    }

    /**
     * 收集所有连线
     */
    private fun collectEdges(node: GraphNode): List<GraphEdge> {
        val edges = mutableListOf<GraphEdge>()

        // 父母-子女连线
        node.children.forEach { child ->
            edges.add(GraphEdge(node, child, GraphEdge.EdgeType.PARENT_CHILD))
            edges.addAll(collectEdges(child))
        }

        // 配偶连线
        node.mate?.let { mate ->
            edges.add(GraphEdge(node, mate, GraphEdge.EdgeType.MATE))
        }

        return edges
    }

    private fun calculateBounds(node: GraphNode): RectF {
        // 遍历所有节点计算边界
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        traverseNodes(node) { n ->
            minX = minOf(minX, n.x)
            minY = minOf(minY, n.y)
            maxX = maxOf(maxX, n.x + GraphNode.NODE_WIDTH)
            maxY = maxOf(maxY, n.y + GraphNode.NODE_HEIGHT)
        }

        return RectF(minX - 50, minY - 50, maxX + 50, maxY + 50)
    }

    private fun traverseNodes(node: GraphNode, action: (GraphNode) -> Unit) {
        action(node)
        node.children.forEach { traverseNodes(it, action) }
        node.mate?.let { traverseNodes(it, action) }
    }

    private var GraphNode.subtreeWidth: Float by Delegates.notNull()

    data class LayoutResult(
        val rootNode: GraphNode,
        val edges: List<GraphEdge>,
        val bounds: RectF
    )
}
```

### 5.4 自定义View渲染核心

```kotlin
/**
 * 家族图谱自定义View
 * 支持：缩放、拖拽、节点点击
 * 针对老年用户优化：大点击区域、清晰的视觉反馈
 */
class FamilyGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 画笔定义
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_node_bg)
        style = Paint.Style.FILL
    }
    private val nodeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_node_border)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val nodeSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_node_selected)
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_text_primary)
        textSize = context.resources.getDimension(R.dimen.graph_text_size)  // 大字体适配老年用户
        textAlign = Paint.Align.CENTER
    }
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_edge_normal)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private val mateEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.graph_edge_mate)
        strokeWidth = 2.5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)  // 虚线表示配偶关系
    }

    // 变换矩阵（支持缩放/拖拽）
    private val currentMatrix = Matrix()
    private val savedMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    // 缩放限制（适配老年用户：最小不缩太小看不清，最大不缩太大迷路）
    private val minScale = 0.3f
    private val maxScale = 3.0f

    // 手势检测器
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    // 当前布局数据
    private var layoutResult: LayoutResult? = null
    private var selectedNodeId: String? = null
    private var onNodeClickListener: ((String) -> Unit)? = null

    // 节点点击区域缓存（屏幕坐标系）
    private val hitTestRects = mutableMapOf<String, RectF>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val result = layoutResult ?: return

        canvas.save()
        canvas.concat(currentMatrix)

        // 绘制连线（先绘制连线确保在节点下层）
        result.edges.forEach { edge ->
            drawEdge(canvas, edge)
        }

        // 绘制节点
        drawNodeRecursive(canvas, result.rootNode)

        canvas.restore()
    }

    /**
     * 绘制单个节点（递归）
     */
    private fun drawNodeRecursive(canvas: Canvas, node: GraphNode) {
        drawSingleNode(canvas, node)
        node.children.forEach { drawNodeRecursive(canvas, it) }
        node.mate?.let { drawSingleNode(canvas, it) }
    }

    /**
     * 绘制节点外观
     * 老年友好设计：大圆角、高对比度、清晰边框
     */
    private fun drawSingleNode(canvas: Canvas, node: GraphNode) {
        val rect = RectF(
            node.x,
            node.y,
            node.x + GraphNode.NODE_WIDTH,
            node.y + GraphNode.NODE_HEIGHT
        )

        // 背景圆角矩形
        val cornerRadius = 16f
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, nodePaint)

        // 边框（选中态高亮）
        val borderPaint = if (node.pigeonId == selectedNodeId) {
            nodeSelectedPaint
        } else {
            nodeBorderPaint
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

        // 绘制头像区域（圆形裁剪）
        node.pigeonBrief.photoPath?.let { photoPath ->
            drawAvatar(canvas, photoPath, node.x + 50f, node.y + GraphNode.NODE_HEIGHT / 2, 36f)
        }

        // 绘制名称（大字体，居中）
        val displayText = if (node.pigeonBrief.name.length > 6) {
            node.pigeonBrief.name.substring(0, 6) + "..."
        } else {
            node.pigeonBrief.name
        }
        canvas.drawText(
            displayText,
            node.x + GraphNode.NODE_WIDTH / 2 + 20f,
            node.y + GraphNode.NODE_HEIGHT / 2 + 8f,
            textPaint
        )

        // 绘制性别标记
        val genderMarker = when (node.pigeonBrief.gender) {
            Gender.MALE -> "♂"
            Gender.FEMALE -> "♀"
            else -> "?"
        }
        canvas.drawText(
            genderMarker,
            node.x + GraphNode.NODE_WIDTH - 20f,
            node.y + 24f,
            textPaint.apply { textSize = textPaint.textSize * 0.7f }
        )
    }

    /**
     * 绘制连线
     */
    private fun drawEdge(canvas: Canvas, edge: GraphEdge) {
        val paint = when (edge.type) {
            GraphEdge.EdgeType.MATE -> mateEdgePaint
            GraphEdge.EdgeType.PARENT_CHILD -> edgePaint
        }

        val startX = edge.fromNode.x + GraphNode.NODE_WIDTH / 2
        val startY = edge.fromNode.y + if (edge.type == GraphEdge.EdgeType.PARENT_CHILD)
            GraphNode.NODE_HEIGHT else GraphNode.NODE_HEIGHT / 2
        val endX = edge.toNode.x + if (edge.type == GraphEdge.EdgeType.MATE)
            0f else GraphNode.NODE_WIDTH / 2
        val endY = edge.toNode.y + if (edge.type == GraphEdge.EdgeType.PARENT_CHILD)
            0f else GraphNode.NODE_HEIGHT / 2

        when (edge.type) {
            GraphEdge.EdgeType.PARENT_CHILD -> {
                // 父母-子女使用正交连线（先下后中再上）
                val midY = startY + (endY - startY) / 2
                canvas.drawLine(startX, startY, startX, midY, paint)
                canvas.drawLine(startX, midY, endX, midY, paint)
                canvas.drawLine(endX, midY, endX, endY, paint)
            }
            GraphEdge.EdgeType.MATE -> {
                // 配偶使用水平虚线
                canvas.drawLine(
                    edge.fromNode.x + GraphNode.NODE_WIDTH,
                    startY,
                    edge.toNode.x,
                    endY,
                    paint
                )
            }
        }
    }

    /**
     * 触摸事件处理
     * 老年用户优化：
     * 1. 点击区域扩大（比视觉区域大20%）
     * 2. 双击放大/还原
     * 3. 单指拖拽平移
     * 4. 双指缩放
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(currentMatrix)
            }
        }
        return true
    }

    /**
     * 手势监听器
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // 单指拖拽：平移画布
            currentMatrix.postTranslate(-distanceX, -distanceY)
            invalidate()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // 双击：放大/还原
            getCurrentScale().let { currentScale ->
                if (currentScale > 1.5f) {
                    // 还原
                    currentMatrix.reset()
                } else {
                    // 放大到2倍，以点击点为中心
                    val scale = 2f
                    currentMatrix.postScale(
                        scale, scale,
                        e.x, e.y
                    )
                }
            }
            constrainScale()
            invalidate()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // 单指点击：检测节点命中
            val invertedMatrix = Matrix()
            currentMatrix.invert(invertedMatrix)
            val point = floatArrayOf(e.x, e.y)
            invertedMatrix.mapPoints(point)

            val hitNode = findNodeAt(point[0], point[1])
            hitNode?.let {
                selectedNodeId = it.pigeonId
                onNodeClickListener?.invoke(it.pigeonId)
                invalidate()
                return true
            }
            return false
        }
    }

    /**
     * 缩放手势监听器
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = getCurrentScale() * scaleFactor

            if (newScale in minScale..maxScale) {
                currentMatrix.postScale(
                    scaleFactor, scaleFactor,
                    detector.focusX, detector.focusY
                )
            }
            invalidate()
            return true
        }
    }

    /**
     * 在指定坐标查找节点
     */
    private fun findNodeAt(x: Float, y: Float): GraphNode? {
        val result = layoutResult ?: return null
        return findNodeRecursive(result.rootNode, x, y)
    }

    private fun findNodeRecursive(node: GraphNode, x: Float, y: Float): GraphNode? {
        // 扩大20%的点击区域（老年用户友好）
        val expandedWidth = GraphNode.NODE_WIDTH * 1.2f
        val expandedHeight = GraphNode.NODE_HEIGHT * 1.2f
        val halfExpandW = (expandedWidth - GraphNode.NODE_WIDTH) / 2
        val halfExpandH = (expandedHeight - GraphNode.NODE_HEIGHT) / 2

        if (x >= node.x - halfExpandW && x <= node.x + GraphNode.NODE_WIDTH + halfExpandW &&
            y >= node.y - halfExpandH && y <= node.y + GraphNode.NODE_HEIGHT + halfExpandH
        ) {
            return node
        }

        node.children.forEach { child ->
            findNodeRecursive(child, x, y)?.let { return it }
        }
        node.mate?.let { mate ->
            findNodeRecursive(mate, x, y)?.let { return it }
        }

        return null
    }

    private fun getCurrentScale(): Float {
        currentMatrix.getValues(matrixValues)
        return matrixValues[Matrix.MSCALE_X]
    }

    private fun constrainScale() {
        val scale = getCurrentScale()
        if (scale < minScale) {
            val factor = minScale / scale
            currentMatrix.postScale(factor, factor, width / 2f, height / 2f)
        } else if (scale > maxScale) {
            val factor = maxScale / scale
            currentMatrix.postScale(factor, factor, width / 2f, height / 2f)
        }
    }

    // 公共API
    fun setGraphData(result: LayoutResult) {
        this.layoutResult = result
        // 初始适配：缩放到View宽度
        currentMatrix.reset()
        invalidate()
    }

    fun setOnNodeClickListener(listener: (String) -> Unit) {
        this.onNodeClickListener = listener
    }

    fun setSelectedNode(pigeonId: String?) {
        this.selectedNodeId = pigeonId
        invalidate()
    }
}
```

### 5.5 老年用户交互优化

| 交互设计 | 具体实现 | 目的 |
|---------|---------|------|
| **大点击区域** | 节点视觉区域外扩20%作为热区 | 减少误操作，适配手抖 |
| **双击缩放** | 双击放大2倍，再次双击还原 | 无需精确操作双指缩放 |
| **最小/最大缩放限制** | 0.3x ~ 3.0x | 防止缩太小看不清或缩太大迷路 |
| **高对比度选中态** | 选中节点边框加粗+颜色变化 | 明确反馈当前选中项 |
| **正交连线** | 父母-子女使用横平竖直的折线 | 关系清晰，避免斜线混乱 |
| **虚线配偶连线** | 配偶关系使用水平虚线 | 与父母-子女实线区分 |
| **大字体** | 节点名称18sp+ | 适配老年视力 |
| **性别颜色区分** | 雄♂蓝色/雌♀粉色/未知灰色 | 快速识别性别 |

---

## 6. 离线同步与备份策略

### 6.1 数据存储策略

本应用采用**纯本地存储**策略，所有数据存储在设备本地，无需云端同步服务器。这是基于老年用户的使用场景做出的决策：

1. 老年用户网络不稳定，云端同步体验差
2. 鸽棚数据属于个人隐私，本地存储更安全
3. 纯本地架构简单可靠，减少故障点
4. 通过文件导出实现数据的迁移和备份

```
数据存储架构：
┌─────────────────────────────────────────────────────────────┐
│                     应用存储空间                              │
│                                                             │
│  ┌───────────────────┐    ┌──────────────────────────────┐ │
│  │  Internal Storage │    │   External Storage (可选)    │ │
│  │                   │    │                              │ │
│  │  /databases/      │    │  /Documents/PigeonNest/      │ │
│  │  ├── pigeon_nest.db│   │  ├── backups/                │ │
│  │  └── *.db-shm/wal │    │  │   ├── backup_20240115.zip │ │
│  │                   │    │  │   └── backup_20240201.zip │ │
│  │  /files/photos/   │    │  └── exports/                │ │
│  │  ├── pigeon_001/  │    │      └── export_20240115.json│ │
│  │  │   ├── main.jpg │    │                              │ │
│  │  │   └── side.jpg │    │                              │ │
│  │  └── pigeon_002/  │    │                              │ │
│  │      └── main.jpg │    │                              │ │
│  │                   │    │                              │ │
│  │  /shared_prefs/   │    │                              │ │
│  │  └── settings.xml │    │                              │ │
│  └───────────────────┘    └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 数据备份/导出机制

#### 6.2.1 备份格式设计

采用 **JSON + 照片ZIP包** 的备份格式，兼顾可读性和完整性：

```json
{
  "backup_version": 1,
  "backup_date": 1705315200000,
  "app_version": "1.0.0",
  "data": {
    "lofts": [
      {
        "id": "loft_001",
        "name": "东棚",
        "location": "院子东侧",
        "description": "主要种鸽棚",
        "sort_order": 0,
        "created_at": 1705315200000
      }
    ],
    "pigeons": [
      {
        "id": "pigeon_001",
        "ring_number": "CHN.2023.01.000001",
        "name": "闪电",
        "color": "雨点",
        "gender": 1,
        "birth_date": 1672531200000,
        "photo_path": "photos/pigeon_001/main.jpg",
        "loft_id": "loft_001",
        "cage_number": "A-01",
        "status": 0,
        "notes": "公棚赛归巢鸽，速度快",
        "created_at": 1705315200000,
        "updated_at": 1705315200000
      }
    ],
    "family_relations": [
      {
        "id": "rel_001",
        "pigeon_id": "pigeon_001",
        "father_id": "pigeon_002",
        "mother_id": "pigeon_003",
        "mate_id": "pigeon_004",
        "created_at": 1705315200000
      }
    ],
    "pigeon_photos": [
      {
        "id": "photo_001",
        "pigeon_id": "pigeon_001",
        "photo_path": "photos/pigeon_001/side.jpg",
        "caption": "侧面照",
        "taken_date": 1705315200000,
        "is_primary": false,
        "created_at": 1705315200000
      }
    ]
  }
}
```

#### 6.2.2 备份导出流程

```kotlin
/**
 * 备份管理器
 */
class BackupManager @Inject constructor(
    private val context: Context,
    private val database: PigeonNestDatabase,
    private val photoStorage: PhotoStorageManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    companion object {
        const val BACKUP_VERSION = 1
        const val BACKUP_FILE_PREFIX = "pigeonnest_backup_"
        const val BACKUP_FILE_EXTENSION = ".zip"
    }

    /**
     * 导出完整备份（JSON数据 + 照片ZIP）
     * @return 备份文件Uri
     */
    suspend fun exportBackup(): Result<Uri> = withContext(ioDispatcher) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
                .format(Date())
            val backupDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "PigeonNest/backups"
            ).apply { mkdirs() }

            val backupFile = File(backupDir, "${BACKUP_FILE_PREFIX}${timestamp}${BACKUP_FILE_EXTENSION}")

            // 1. 导出数据库为JSON
            val backupData = createBackupData()
            val jsonString = GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(backupData)

            // 2. 打包JSON和照片为ZIP
            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                // 写入JSON数据文件
                zos.putNextEntry(ZipEntry("data.json"))
                zos.write(jsonString.toByteArray(Charsets.UTF_8))
                zos.closeEntry()

                // 写入所有照片文件
                photoStorage.getAllPhotoFiles().forEach { photoFile ->
                    if (photoFile.exists()) {
                        val entryName = "photos/${photoFile.name}"
                        zos.putNextEntry(ZipEntry(entryName))
                        photoFile.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }

            // 3. 通知媒体库扫描
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导入备份
     * @param backupUri 备份文件Uri
     * @param mode 导入模式：REPLACE(全部替换) / MERGE(合并)
     */
    suspend fun importBackup(
        backupUri: Uri,
        mode: ImportMode
    ): Result<ImportResult> = withContext(ioDispatcher) {
        try {
            val tempDir = File(context.cacheDir, "backup_temp").apply {
                deleteRecursively()
                mkdirs()
            }

            // 1. 解压ZIP到临时目录
            context.contentResolver.openInputStream(backupUri)?.use { input ->
                ZipInputStream(input).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        val entryFile = File(tempDir, entry!!.name)
                        entryFile.parentFile?.mkdirs()
                        entryFile.outputStream().use { output ->
                            zis.copyTo(output)
                        }
                    }
                }
            } ?: return@withContext Result.failure(IllegalArgumentException("无法读取备份文件"))

            // 2. 读取并解析JSON
            val jsonFile = File(tempDir, "data.json")
            if (!jsonFile.exists()) {
                return@withContext Result.failure(IllegalArgumentException("备份文件格式错误：缺少data.json"))
            }
            val backupData = Gson().fromJson(
                jsonFile.readText(),
                BackupData::class.java
            )

            // 3. 验证备份版本
            if (backupData.backup_version > BACKUP_VERSION) {
                return@withContext Result.failure(
                    UnsupportedOperationException("备份版本 ${backupData.backup_version} 不兼容，请升级应用")
                )
            }

            // 4. 执行导入
            val importResult = when (mode) {
                ImportMode.REPLACE -> importReplace(backupData, tempDir)
                ImportMode.MERGE -> importMerge(backupData, tempDir)
            }

            // 5. 清理临时文件
            tempDir.deleteRecursively()

            Result.success(importResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 替换模式导入：清空现有数据，导入备份数据
     */
    private suspend fun importReplace(backupData: BackupData, tempDir: File): ImportResult {
        database.runInTransaction {
            // 清空现有数据
            database.pigeonDao().deleteAll()
            database.familyRelationDao().deleteAll()
            database.loftDao().deleteAll()
            database.pigeonPhotoDao().deleteAll()

            // 导入鸽棚
            backupData.data.lofts.forEach { database.loftDao().insert(it.toEntity()) }

            // 导入鸽子
            backupData.data.pigeons.forEach { database.pigeonDao().insert(it.toEntity()) }

            // 导入家族关系
            backupData.data.family_relations.forEach {
                database.familyRelationDao().insert(it.toEntity())
            }

            // 导入照片记录
            backupData.data.pigeon_photos.forEach {
                database.pigeonPhotoDao().insert(it.toEntity())
            }
        }

        // 复制照片文件
        val photosDir = File(tempDir, "photos")
        if (photosDir.exists()) {
            photoStorage.importPhotosFromDirectory(photosDir)
        }

        return ImportResult(
            importedLofts = backupData.data.lofts.size,
            importedPigeons = backupData.data.pigeons.size,
            importedRelations = backupData.data.family_relations.size,
            importedPhotos = backupData.data.pigeon_photos.size
        )
    }

    /**
     * 合并模式导入：保留现有数据，备份数据按ID合并
     */
    private suspend fun importMerge(backupData: BackupData, tempDir: File): ImportResult {
        var loftCount = 0
        var pigeonCount = 0
        var relationCount = 0
        var photoCount = 0

        database.runInTransaction {
            backupData.data.lofts.forEach { loft ->
                if (database.loftDao().getById(loft.id) == null) {
                    database.loftDao().insert(loft.toEntity())
                    loftCount++
                }
            }

            backupData.data.pigeons.forEach { pigeon ->
                database.pigeonDao().insertOrIgnore(pigeon.toEntity())
                pigeonCount++
            }

            backupData.data.family_relations.forEach { relation ->
                database.familyRelationDao().insertOrUpdate(relation.toEntity())
                relationCount++
            }

            backupData.data.pigeon_photos.forEach { photo ->
                database.pigeonPhotoDao().insertOrIgnore(photo.toEntity())
                photoCount++
            }
        }

        // 合并模式也复制照片
        val photosDir = File(tempDir, "photos")
        if (photosDir.exists()) {
            photoStorage.importPhotosFromDirectory(photosDir)
        }

        return ImportResult(loftCount, pigeonCount, relationCount, photoCount)
    }

    data class ImportResult(
        val importedLofts: Int,
        val importedPigeons: Int,
        val importedRelations: Int,
        val importedPhotos: Int
    )

    enum class ImportMode { REPLACE, MERGE }
}
```

#### 6.2.3 自动备份策略

| 备份策略 | 实现方式 | 频率 |
|---------|---------|------|
| **本地自动备份** | 每次数据变更后，异步写入JSON备份副本 | 实时 |
| **手动导出** | 用户手动触发，导出到Documents目录 | 按需 |
| **分享备份** | 通过Android Share Sheet分享到微信/QQ等 | 按需 |

```kotlin
/**
 * 自动备份Worker（每次数据库变更后触发）
 */
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val backupManager = EntryPointAccessors.fromApplication(
                applicationContext,
                BackupEntryPoint::class.java
            ).backupManager()

            // 导出到应用的私有备份目录（轻量级）
            backupManager.exportInternalBackup()
            Result.success()
        } catch (e: Exception) {
            // 自动备份失败不影响主流程
            Result.failure()
        }
    }
}
```

### 6.3 照片存储策略

```kotlin
/**
 * 照片存储管理器
 * 负责照片的保存、加载、压缩和清理
 */
class PhotoStorageManager @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val photosDir: File by lazy {
        File(context.filesDir, "photos").apply { mkdirs() }
    }

    /**
     * 保存鸽子照片
     * 老年用户设备存储有限，进行智能压缩
     */
    suspend fun savePigeonPhoto(
        pigeonId: String,
        sourceUri: Uri,
        maxSizeKB: Int = 500        // 老年设备友好：单张照片最大500KB
    ): String = withContext(ioDispatcher) {
        val pigeonPhotoDir = File(photosDir, pigeonId).apply { mkdirs() }
        val fileName = "${System.currentTimeMillis()}.jpg"
        val destFile = File(pigeonPhotoDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            // 读取图片并压缩
            val bitmap = BitmapFactory.decodeStream(input)
            val compressed = compressBitmap(bitmap, maxSizeKB)
            destFile.outputStream().use {
                compressed.compress(Bitmap.CompressFormat.JPEG, 90, it)
            }
            bitmap.recycle()
            compressed.recycle()
        }

        destFile.absolutePath
    }

    /**
     * 智能压缩：根据目标大小调整分辨率
     */
    private fun compressBitmap(original: Bitmap, maxSizeKB: Int): Bitmap {
        val maxDimension = 1200  // 最大边长1200px（足够展示，节省空间）
        val scale = minOf(
            maxDimension.toFloat() / original.width,
            maxDimension.toFloat() / original.height,
            1f
        )

        return if (scale < 1f) {
            val newWidth = (original.width * scale).toInt()
            val newHeight = (original.height * scale).toInt()
            Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
        } else {
            original
        }
    }

    /**
     * 加载照片（使用Glide缓存）
     */
    fun loadPhoto(imageView: ImageView, photoPath: String?, placeholderRes: Int = R.drawable.ic_pigeon_placeholder) {
        Glide.with(imageView.context)
            .load(photoPath)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .centerCrop()
            .into(imageView)
    }

    /**
     * 获取所有照片文件（用于备份打包）
     */
    fun getAllPhotoFiles(): List<File> {
        return photosDir.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png") }
            .toList()
    }

    /**
     * 导入照片目录（恢复时使用）
     */
    suspend fun importPhotosFromDirectory(sourceDir: File) = withContext(ioDispatcher) {
        sourceDir.walkTopDown()
            .filter { it.isFile }
            .forEach { sourceFile ->
                val relativePath = sourceFile.relativeTo(sourceDir).path
                val destFile = File(photosDir, relativePath)
                destFile.parentFile?.mkdirs()
                sourceFile.copyTo(destFile, overwrite = true)
            }
    }

    /**
     * 删除鸽子的所有照片
     */
    suspend fun deletePigeonPhotos(pigeonId: String) = withContext(ioDispatcher) {
        File(photosDir, pigeonId).deleteRecursively()
    }
}
```

### 6.4 存储空间管理

```kotlin
/**
 * 存储空间管理器
 * 监控和管理应用的存储使用，防止老年设备存储不足
 */
class StorageManager @Inject constructor(
    private val context: Context,
    private val photoStorage: PhotoStorageManager
) {
    /**
     * 获取当前存储使用情况
     */
    fun getStorageUsage(): StorageInfo {
        val dataDir = context.filesDir.parentFile!!
        val dbSize = File(dataDir, "databases").listFiles()?.sumOf { it.length() } ?: 0
        val photoSize = photoStorage.getAllPhotoFiles().sumOf { it.length() }
        val cacheSize = context.cacheDir.listFiles()?.sumOf { it.length() } ?: 0
        val totalUsed = dbSize + photoSize + cacheSize

        val stat = StatFs(dataDir.path)
        val availableBytes = stat.availableBytes

        return StorageInfo(
            databaseBytes = dbSize,
            photosBytes = photoSize,
            cacheBytes = cacheSize,
            totalUsedBytes = totalUsed,
            availableBytes = availableBytes
        )
    }

    /**
     * 检查存储空间是否充足
     */
    fun hasEnoughSpace(requiredMB: Int = 100): Boolean {
        val stat = StatFs(context.filesDir.path)
        val availableMB = stat.availableBytes / (1024 * 1024)
        return availableMB >= requiredMB
    }

    /**
     * 清理缓存
     */
    fun clearCache() {
        context.cacheDir.deleteRecursively()
    }
}

data class StorageInfo(
    val databaseBytes: Long,
    val photosBytes: Long,
    val cacheBytes: Long,
    val totalUsedBytes: Long,
    val availableBytes: Long
) {
    fun toDisplayString(): String {
        return buildString {
            appendLine("数据库: ${databaseBytes / 1024}KB")
            appendLine("照片: ${photosBytes / 1024 / 1024}MB")
            appendLine("缓存: ${cacheBytes / 1024}KB")
            appendLine("总计已用: ${totalUsedBytes / 1024 / 1024}MB")
            appendLine("可用空间: ${availableBytes / 1024 / 1024}MB")
        }
    }
}
```

---

## 7. 项目结构

### 7.1 目录结构规范

```
PigeonNest/
├── app/                                          # 主应用模块
│   ├── build.gradle.kts                          # 模块构建配置
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml               # 应用清单
│       │   │
│       │   ├── java/com/pigeonnest/
│       │   │   ├── PigeonNestApp.kt              # Application入口（@HiltAndroidApp）
│       │   │   │
│       │   │   ├── presentation/                 # ========== 展示层 ==========
│       │   │   │   ├── main/                     # 主Activity + 底部导航
│       │   │   │   │   ├── MainActivity.kt
│       │   │   │   │   └── MainViewModel.kt
│       │   │   │   │
│       │   │   │   ├── pigeonlist/               # 鸽子列表
│       │   │   │   │   ├── PigeonListFragment.kt
│       │   │   │   │   ├── PigeonListViewModel.kt
│       │   │   │   │   ├── PigeonListAdapter.kt          # RecyclerView.Adapter
│       │   │   │   │   ├── PigeonListUiState.kt
│       │   │   │   │   └── PigeonDiffCallback.kt
│       │   │   │   │
│       │   │   │   ├── pigeondetail/             # 鸽子详情
│       │   │   │   │   ├── PigeonDetailFragment.kt
│       │   │   │   │   ├── PigeonDetailViewModel.kt
│       │   │   │   │   └── PigeonDetailUiState.kt
│       │   │   │   │
│       │   │   │   ├── familygraph/              # 家族图谱
│       │   │   │   │   ├── FamilyGraphFragment.kt
│       │   │   │   │   ├── FamilyGraphViewModel.kt
│       │   │   │   │   ├── FamilyGraphView.kt            # 自定义View
│       │   │   │   │   ├── GraphLayoutManager.kt         # 布局算法
│       │   │   │   │   └── GraphData.kt                  # 图谱数据结构
│       │   │   │   │
│       │   │   │   ├── familyedit/                 # 家族关系编辑
│       │   │   │   │   ├── FamilyEditFragment.kt
│       │   │   │   │   └── FamilyEditViewModel.kt
│       │   │   │   │
│       │   │   │   ├── loftmanage/                 # 鸽棚管理
│       │   │   │   │   ├── LoftListFragment.kt
│       │   │   │   │   ├── LoftEditDialogFragment.kt
│       │   │   │   │   └── LoftManageViewModel.kt
│       │   │   │   │
│       │   │   │   ├── locationset/                # 位置设置
│       │   │   │   │   ├── LocationSetFragment.kt
│       │   │   │   │   └── LocationSetViewModel.kt
│       │   │   │   │
│       │   │   │   ├── backup/                     # 备份恢复
│       │   │   │   │   ├── BackupRestoreFragment.kt
│       │   │   │   │   ├── BackupRestoreViewModel.kt
│       │   │   │   │   └── BackupAdapter.kt
│       │   │   │   │
│       │   │   │   ├── search/                     # 搜索
│       │   │   │   │   ├── SearchFragment.kt
│       │   │   │   │   └── SearchViewModel.kt
│       │   │   │   │
│       │   │   │   └── common/                     # 共享UI组件
│       │   │   │       ├── ElderlyFriendlyDialog.kt      # 老年友好对话框
│       │   │   │       ├── LargeButton.kt                # 大按钮组件
│       │   │   │       ├── PhotoPicker.kt                # 照片选择器
│       │   │   │       ├── EmptyView.kt                  # 空状态视图
│       │   │   │       ├── LoadingView.kt                # 加载视图
│       │   │   │       └── ErrorView.kt                  # 错误视图
│       │   │   │
│       │   │   ├── domain/                       # ========== 领域层 ==========
│       │   │   │   ├── model/                    # 领域模型
│       │   │   │   │   ├── Pigeon.kt
│       │   │   │   │   ├── Loft.kt
│       │   │   │   │   ├── FamilyRelation.kt
│       │   │   │   │   ├── PigeonBrief.kt
│       │   │   │   │   ├── PigeonPhoto.kt
│       │   │   │   │   ├── Gender.kt
│       │   │   │   │   ├── PigeonStatus.kt
│       │   │   │   │   └── FamilyGraph.kt        # 图谱数据结构
│       │   │   │   │
│       │   │   │   ├── repository/               # Repository接口
│       │   │   │   │   ├── PigeonRepository.kt
│       │   │   │   │   ├── FamilyRepository.kt
│       │   │   │   │   ├── LoftRepository.kt
│       │   │   │   │   └── BackupRepository.kt
│       │   │   │   │
│       │   │   │   └── usecase/                  # UseCase
│       │   │   │       ├── pigeon/
│       │   │   │       │   ├── GetPigeonListUseCase.kt
│       │   │   │       │   ├── GetPigeonDetailUseCase.kt
│       │   │   │       │   ├── SavePigeonUseCase.kt
│       │   │   │       │   ├── DeletePigeonUseCase.kt
│       │   │   │       │   └── SearchPigeonsUseCase.kt
│       │   │   │       │
│       │   │   │       ├── family/
│       │   │   │       │   ├── GetFamilyGraphUseCase.kt
│       │   │   │       │   ├── UpdateFamilyRelationUseCase.kt
│       │   │   │       │   └── GetLineageUseCase.kt
│       │   │   │       │
│       │   │   │       ├── loft/
│       │   │   │       │   ├── GetLoftListUseCase.kt
│       │   │   │       │   ├── SaveLoftUseCase.kt
│       │   │   │       │   └── DeleteLoftUseCase.kt
│       │   │   │       │
│       │   │   │       ├── location/
│       │   │   │       │   └── UpdatePigeonLocationUseCase.kt
│       │   │   │       │
│       │   │   │       └── backup/
│       │   │   │           ├── ExportBackupUseCase.kt
│       │   │   │           └── ImportBackupUseCase.kt
│       │   │   │
│       │   │   ├── data/                         # ========== 数据层 ==========
│       │   │   │   ├── local/                    # 本地数据源
│       │   │   │   │   ├── dao/
│       │   │   │   │   │   ├── PigeonDao.kt
│       │   │   │   │   │   ├── FamilyRelationDao.kt
│       │   │   │   │   │   ├── LoftDao.kt
│       │   │   │   │   │   └── PigeonPhotoDao.kt
│       │   │   │   │   │
│       │   │   │   │   ├── entity/               # Room实体
│       │   │   │   │   │   ├── PigeonEntity.kt
│       │   │   │   │   │   ├── LoftEntity.kt
│       │   │   │   │   │   ├── FamilyRelationEntity.kt
│       │   │   │   │   │   └── PigeonPhotoEntity.kt
│       │   │   │   │   │
│       │   │   │   │   ├── database/
│       │   │   │   │   │   ├── PigeonNestDatabase.kt
│       │   │   │   │   │   └── Converters.kt
│       │   │   │   │   │
│       │   │   │   │   └── mapper/               # 实体↔领域模型转换
│       │   │   │   │       ├── PigeonMapper.kt
│       │   │   │   │       ├── LoftMapper.kt
│       │   │   │   │       └── FamilyMapper.kt
│       │   │   │   │
│       │   │   │   ├── repository/               # Repository实现
│       │   │   │   │   ├── PigeonRepositoryImpl.kt
│       │   │   │   │   ├── FamilyRepositoryImpl.kt
│       │   │   │   │   ├── LoftRepositoryImpl.kt
│       │   │   │   │   └── BackupRepositoryImpl.kt
│       │   │   │   │
│       │   │   │   └── file/                     # 文件数据源
│       │   │   │       ├── BackupManager.kt
│       │   │   │       ├── PhotoStorageManager.kt
│       │   │   │       └── StorageManager.kt
│       │   │   │
│       │   │   ├── di/                           # ========== 依赖注入 ==========
│       │   │   │   ├── DatabaseModule.kt         # 数据库/DAO注入
│       │   │   │   ├── RepositoryModule.kt       # Repository绑定
│       │   │   │   ├── UseCaseModule.kt          # UseCase注入
│       │   │   │   ├── DispatcherModule.kt       # 协程调度器注入
│       │   │   │   └── BackupEntryPoint.kt       # BackupWorker用EntryPoint
│       │   │   │
│       │   │   └── utils/                        # ========== 工具类 ==========
│       │   │       ├── DateUtils.kt              # 日期格式化
│       │   │       ├── FileUtils.kt              # 文件操作
│       │   │       ├── PermissionHelper.kt       # 权限管理
│       │   │       ├── ValidationUtils.kt        # 输入验证
│       │   │       └── UiStateUtils.kt           # UI状态辅助
│       │   │
│       │   └── res/                              # ========== 资源文件 ==========
│       │       ├── layout/                       # XML布局
│       │       ├── values/
│       │       │   ├── colors.xml                # 老年友好高对比度配色
│       │       │   ├── dimens.xml                # 大尺寸尺寸定义
│       │       │   ├── strings.xml               # 中文字符串
│       │       │   ├── styles.xml                # 主题样式
│       │       │   └── themes.xml
│       │       ├── drawable/                     # 矢量图标/背景
│       │       ├── menu/                         # 菜单资源
│       │       ├── navigation/                   # Navigation图
│       │       │   └── nav_graph.xml
│       │       └── xml/
│       │           └── file_paths.xml            # FileProvider配置
│       │
│       ├── test/                                 # 单元测试
│       │   └── java/com/pigeonnest/
│       │       ├── domain/
│       │       │   └── usecase/
│       │       └── data/
│       │           └── repository/
│       │
│       └── androidTest/                          # 仪器测试
│           └── java/com/pigeonnest/
│
├── build.gradle.kts                              # 项目级构建配置
├── gradle.properties
├── settings.gradle.kts
└── README.md
```

### 7.2 代码组织原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **单一职责** | 每个类只做一件事 | `PigeonListAdapter`只负责列表渲染 |
| **依赖倒置** | 依赖接口而非实现 | ViewModel依赖`PigeonRepository`接口 |
| **包级隔离** | 功能按包隔离，禁止跨层直接引用 | Presentation层不直接引用Entity |
| **不可变状态** | UI状态使用data class，copy更新 | `state.copy(isLoading = true)` |
| **协程安全** | 所有挂起函数在正确调度器执行 | IO操作使用`Dispatchers.IO` |

---

## 8. 接口定义

### 8.1 Repository 接口定义

Repository接口位于Domain层，定义数据访问契约：

```kotlin
// ============================================================================
// PigeonRepository - 鸽子数据仓库接口
// ============================================================================
interface PigeonRepository {

    // 查询操作（返回Flow，支持实时更新）
    fun getAllPigeons(): Flow<List<Pigeon>>
    fun getPigeonById(pigeonId: String): Flow<Pigeon?>
    fun searchPigeons(query: String): Flow<List<Pigeon>>
    fun getPigeonsByLoft(loftId: String): Flow<List<Pigeon>>
    fun getPigeonsByStatus(status: PigeonStatus): Flow<List<Pigeon>>

    // 统计操作
    suspend fun getActivePigeonCount(): Int
    suspend fun getPigeonCountByLoft(loftId: String): Int

    // 写操作
    suspend fun savePigeon(pigeon: Pigeon): Result<String>        // 返回保存后的ID
    suspend fun deletePigeon(pigeonId: String): Result<Unit>
    suspend fun updatePigeonLocation(pigeonId: String, loftId: String?, cageNumber: String?): Result<Unit>

    // 照片操作
    suspend fun addPigeonPhoto(pigeonId: String, photoUri: Uri, caption: String? = null): Result<String>
    suspend fun deletePigeonPhoto(photoId: String): Result<Unit>
}

// ============================================================================
// FamilyRepository - 家族关系数据仓库接口
// ============================================================================
interface FamilyRepository {

    /**
     * 获取鸽子的家族关系
     */
    suspend fun getFamilyRelation(pigeonId: String): FamilyRelation?

    /**
     * 获取鸽子的完整血缘链路
     * @param pigeonId 目标鸽子ID
     * @param generations 向上追溯代数（默认3代）
     */
    suspend fun getLineage(pigeonId: String, generations: Int = 3): LineageResult

    /**
     * 获取鸽子的后代列表
     */
    suspend function getChildren(pigeonId: String): List<PigeonBrief>

    /**
     * 获取鸽子的兄弟姐妹
     */
    suspend fun getSiblings(pigeonId: String): List<PigeonBrief>

    /**
     * 更新家族关系
     */
    suspend fun updateParents(
        pigeonId: String,
        fatherId: String?,
        motherId: String?
    ): Result<Unit>

    suspend fun updateMate(pigeonId: String, mateId: String?): Result<Unit>

    /**
     * 获取用于图谱展示的数据
     */
    suspend fun getGraphData(centerPigeonId: String, depth: Int = 3): GraphData
}

/**
 * 血缘追溯结果
 */
data class LineageResult(
    val pigeon: PigeonBrief,
    val father: LineageResult? = null,
    val mother: LineageResult? = null,
    val generation: Int = 0
)

/**
 * 图谱数据
 */
data class GraphData(
    val rootNode: GraphNode,
    val allNodes: List<GraphNode>,
    val edges: List<GraphEdge>
)

// ============================================================================
// LoftRepository - 鸽棚数据仓库接口
// ============================================================================
interface LoftRepository {

    fun getAllLofts(): Flow<List<Loft>>
    suspend fun getLoftById(loftId: String): Loft?
    suspend fun saveLoft(loft: Loft): Result<String>
    suspend fun deleteLoft(loftId: String): Result<Unit>
    suspend fun updateSortOrder(loftIds: List<String>): Result<Unit>
}

// ============================================================================
// BackupRepository - 备份数据仓库接口
// ============================================================================
interface BackupRepository {

    /**
     * 导出完整备份
     * @return 备份文件Uri
     */
    suspend fun exportBackup(destinationUri: Uri? = null): Result<Uri>

    /**
     * 导入备份
     * @param backupUri 备份文件Uri
     * @param mode 导入模式
     */
    suspend fun importBackup(backupUri: Uri, mode: ImportMode): Result<BackupRepositoryImpl.ImportResult>

    /**
     * 获取备份历史列表
     */
    fun getBackupHistory(): Flow<List<BackupInfo>>

    /**
     * 删除备份文件
     */
    suspend fun deleteBackup(backupUri: Uri): Result<Unit>
}

/**
 * 备份信息
 */
data class BackupInfo(
    val uri: Uri,
    val fileName: String,
    val createdAt: Long,
    val fileSizeBytes: Long
)
```

### 8.2 UseCase 接口定义

UseCase封装单一业务逻辑，采用函数式调用风格（invoke运算符重载）：

```kotlin
// ============================================================================
// 基础UseCase抽象类
// ============================================================================

/**
 * 无参数UseCase基类
 */
abstract class NoParamUseCase<out Type> where Type : Any {
    abstract suspend operator fun invoke(): Result<Type>
}

/**
 * 单参数UseCase基类
 */
abstract class UseCase<in Params, out Type> where Type : Any {
    abstract suspend operator fun invoke(params: Params): Result<Type>
}

/**
 * Flow类型UseCase基类（用于持续观察的场景）
 */
abstract class FlowUseCase<in Params, out Type> {
    abstract operator fun invoke(params: Params): Flow<Type>
}

// ============================================================================
// 鸽子相关UseCase
// ============================================================================

/**
 * 获取鸽子列表
 */
class GetPigeonListUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository
) : FlowUseCase<GetPigeonListUseCase.Params, List<Pigeon>>() {

    data class Params(
        val loftId: String? = null,
        val status: PigeonStatus? = null
    )

    override fun invoke(params: Params): Flow<List<Pigeon>> {
        return when {
            params.loftId != null -> pigeonRepository.getPigeonsByLoft(params.loftId)
            params.status != null -> pigeonRepository.getPigeonsByStatus(params.status)
            else -> pigeonRepository.getAllPigeons()
        }
    }
}

/**
 * 搜索鸽子
 */
class SearchPigeonsUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository
) : FlowUseCase<String, List<Pigeon>>() {

    override fun invoke(params: String): Flow<List<Pigeon>> {
        return if (params.isBlank()) {
            pigeonRepository.getAllPigeons()
        } else {
            pigeonRepository.searchPigeons(params.trim())
        }
    }
}

/**
 * 获取鸽子详情
 */
class GetPigeonDetailUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository,
    private val familyRepository: FamilyRepository
) : UseCase<String, Pigeon>() {

    override suspend fun invoke(params: String): Result<Pigeon> {
        return try {
            val pigeon = pigeonRepository.getPigeonById(params).first()
                ?: return Result.failure(NotFoundException("鸽子不存在: $params"))

            // 加载家族关系
            val familyRelation = familyRepository.getFamilyRelation(params)

            Result.success(pigeon.copy(familyRelation = familyRelation))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 保存鸽子（新增/更新）
 */
class SavePigeonUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository,
    private val validationUtils: ValidationUtils
) : UseCase<SavePigeonUseCase.Params, String>() {

    data class Params(
        val id: String? = null,               // null=新增，有值=更新
        val ringNumber: String,
        val name: String,
        val color: String?,
        val gender: Gender,
        val birthDate: Long?,
        val loftId: String?,
        val cageNumber: String?,
        val status: PigeonStatus,
        val notes: String?,
        val photoUri: Uri? = null             // 新增时可选传照片
    )

    override suspend fun invoke(params: Params): Result<String> {
        // 参数校验
        val validationError = validateParams(params)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val pigeon = Pigeon(
            id = params.id ?: UUID.randomUUID().toString(),
            ringNumber = params.ringNumber.trim(),
            name = params.name.trim(),
            color = params.color?.trim(),
            gender = params.gender,
            birthDate = params.birthDate,
            photoPath = null,  // 照片通过addPigeonPhoto单独处理
            loft = null,       // 通过loftId关联
            cageNumber = params.cageNumber?.trim(),
            status = params.status,
            notes = params.notes?.trim(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return pigeonRepository.savePigeon(pigeon)
    }

    private fun validateParams(params: Params): String? {
        if (params.ringNumber.isBlank()) return "足环号不能为空"
        if (params.name.isBlank()) return "鸽子名称不能为空"
        if (params.ringNumber.length > 50) return "足环号过长（最多50字符）"
        if (params.name.length > 30) return "名称过长（最多30字符）"
        return null
    }
}

// ============================================================================
// 家族关系相关UseCase
// ============================================================================

/**
 * 获取家族图谱数据
 */
class GetFamilyGraphUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val graphLayoutManager: GraphLayoutManager
) : UseCase<GetFamilyGraphUseCase.Params, LayoutResult>() {

    data class Params(
        val centerPigeonId: String,           // 以哪只鸽子为中心展示
        val depth: Int = 3                    // 展示深度（几代）
    )

    override suspend fun invoke(params: Params): Result<LayoutResult> {
        return try {
            val graphData = familyRepository.getGraphData(
                params.centerPigeonId,
                params.depth
            )
            val layoutResult = graphLayoutManager.calculateLayout(graphData.rootNode)
            Result.success(layoutResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 更新家族关系（父母/配偶）
 */
class UpdateFamilyRelationUseCase @Inject constructor(
    private val familyRepository: FamilyRepository
) : UseCase<UpdateFamilyRelationUseCase.Params, Unit>() {

    data class Params(
        val pigeonId: String,
        val fatherId: String? = null,
        val motherId: String? = null,
        val mateId: String? = null
    )

    override suspend fun invoke(params: Params): Result<Unit> {
        // 校验逻辑：不能设置自己为自己的父母或配偶
        if (params.fatherId == params.pigeonId || params.motherId == params.pigeonId) {
            return Result.failure(IllegalArgumentException("不能设置自己为自己的父母"))
        }
        if (params.mateId == params.pigeonId) {
            return Result.failure(IllegalArgumentException("不能设置自己为自己的配偶"))
        }
        // 校验性别：父亲必须是雄性，母亲必须是雌性
        // 这些校验在Repository层完成

        return try {
            params.fatherId?.let {
                familyRepository.updateParents(params.pigeonId, it, params.motherId)
            }
            params.mateId?.let {
                familyRepository.updateMate(params.pigeonId, it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ============================================================================
// 鸽棚管理相关UseCase
// ============================================================================

class GetLoftListUseCase @Inject constructor(
    private val loftRepository: LoftRepository
) : NoParamUseCase<List<Loft>>() {

    override suspend fun invoke(): Result<List<Loft>> {
        return try {
            val lofts = loftRepository.getAllLofts().first()
            Result.success(lofts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ============================================================================
// 位置设置相关UseCase
// ============================================================================

class UpdatePigeonLocationUseCase @Inject constructor(
    private val pigeonRepository: PigeonRepository
) : UseCase<UpdatePigeonLocationUseCase.Params, Unit>() {

    data class Params(
        val pigeonId: String,
        val loftId: String?,
        val cageNumber: String?
    )

    override suspend fun invoke(params: Params): Result<Unit> {
        return pigeonRepository.updatePigeonLocation(
            params.pigeonId,
            params.loftId,
            params.cageNumber
        )
    }
}

// ============================================================================
// 备份恢复相关UseCase
// ============================================================================

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) : NoParamUseCase<Uri>() {

    override suspend fun invoke(): Result<Uri> {
        return backupRepository.exportBackup()
    }
}

class ImportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) : UseCase<ImportBackupUseCase.Params, BackupRepositoryImpl.ImportResult>() {

    data class Params(
        val backupUri: Uri,
        val mode: ImportMode
    )

    override suspend fun invoke(params: Params): Result<BackupRepositoryImpl.ImportResult> {
        return backupRepository.importBackup(params.backupUri, params.mode)
    }
}
```

### 8.3 Repository 实现示例

```kotlin
/**
 * PigeonRepository 实现
 */
class PigeonRepositoryImpl @Inject constructor(
    private val pigeonDao: PigeonDao,
    private val pigeonPhotoDao: PigeonPhotoDao,
    private val photoStorage: PhotoStorageManager,
    private val pigeonMapper: PigeonMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PigeonRepository {

    override fun getAllPigeons(): Flow<List<Pigeon>> {
        return pigeonDao.getAllPigeonsFlow()
            .map { entities ->
                entities.map { pigeonMapper.toDomain(it) }
            }
            .flowOn(ioDispatcher)
    }

    override fun getPigeonById(pigeonId: String): Flow<Pigeon?> {
        return pigeonDao.getByIdFlow(pigeonId)
            .map { entity ->
                entity?.let { pigeonMapper.toDomain(it) }
            }
            .flowOn(ioDispatcher)
    }

    override fun searchPigeons(query: String): Flow<List<Pigeon>> {
        return pigeonDao.searchPigeons(query)
            .map { entities ->
                entities.map { pigeonMapper.toDomain(it) }
            }
            .flowOn(ioDispatcher)
    }

    override fun getPigeonsByLoft(loftId: String): Flow<List<Pigeon>> {
        return pigeonDao.getByLoft(loftId)
            .map { entities ->
                entities.map { pigeonMapper.toDomain(it) }
            }
            .flowOn(ioDispatcher)
    }

    override fun getPigeonsByStatus(status: PigeonStatus): Flow<List<Pigeon>> {
        return pigeonDao.getByStatus(status.code)
            .map { entities ->
                entities.map { pigeonMapper.toDomain(it) }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getActivePigeonCount(): Int {
        return pigeonDao.getActiveCount()
    }

    override suspend fun getPigeonCountByLoft(loftId: String): Int {
        return pigeonDao.getCountByLoft(loftId)
    }

    override suspend fun savePigeon(pigeon: Pigeon): Result<String> {
        return try {
            val entity = pigeonMapper.toEntity(pigeon)
            pigeonDao.insert(entity)
            Result.success(pigeon.id)
        } catch (e: SQLiteConstraintException) {
            Result.failure(DuplicateException("足环号已存在: ${pigeon.ringNumber}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePigeon(pigeonId: String): Result<Unit> {
        return try {
            // 先删除照片
            photoStorage.deletePigeonPhotos(pigeonId)
            // 再删除数据库记录（级联删除会处理关联数据）
            pigeonDao.deleteById(pigeonId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePigeonLocation(
        pigeonId: String,
        loftId: String?,
        cageNumber: String?
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val entity = pigeonDao.getById(pigeonId)
                ?: return@withContext Result.failure(NotFoundException("鸽子不存在"))
            pigeonDao.update(
                entity.copy(
                    loftId = loftId,
                    cageNumber = cageNumber,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPigeonPhoto(
        pigeonId: String,
        photoUri: Uri,
        caption: String?
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val photoPath = photoStorage.savePigeonPhoto(pigeonId, photoUri)
            val photoEntity = PigeonPhotoEntity(
                pigeonId = pigeonId,
                photoPath = photoPath,
                caption = caption,
                takenDate = System.currentTimeMillis()
            )
            pigeonPhotoDao.insert(photoEntity)
            Result.success(photoEntity.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePigeonPhoto(photoId: String): Result<Unit> {
        return try {
            pigeonPhotoDao.deleteById(photoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 8.4 Mapper 转换层

```kotlin
/**
 * 鸽子实体转换器
 * 负责 PigeonEntity ↔ Pigeon 的双向转换
 */
class PigeonMapper @Inject constructor() {

    fun toDomain(entity: PigeonEntity): Pigeon {
        return Pigeon(
            id = entity.id,
            ringNumber = entity.ringNumber,
            name = entity.name,
            color = entity.color,
            gender = Gender.fromCode(entity.gender),
            birthDate = entity.birthDate,
            photoPath = entity.photoPath,
            loft = null,  // 通过loftId延迟加载或使用JOIN查询
            cageNumber = entity.cageNumber,
            status = PigeonStatus.fromCode(entity.status),
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: Pigeon): PigeonEntity {
        return PigeonEntity(
            id = domain.id,
            ringNumber = domain.ringNumber,
            name = domain.name,
            color = domain.color,
            gender = domain.gender.code,
            birthDate = domain.birthDate,
            photoPath = domain.photoPath,
            loftId = domain.loft?.id,
            cageNumber = domain.cageNumber,
            status = domain.status.code,
            notes = domain.notes,
            createdAt = domain.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}
```

---

## 9. 性能与安全考量

### 9.1 性能优化策略

#### 9.1.1 数据库性能

| 优化点 | 具体措施 | 目标 |
|--------|---------|------|
| 索引优化 | 为外键、搜索字段、排序字段添加索引 | 查询 < 50ms |
| 分页查询 | 列表使用Paging3库，每页20条 | 避免一次加载过多数据 |
| 数据库连接池 | Room自动管理，复用连接 | 减少连接开销 |
| 事务批处理 | 批量导入使用`@Transaction` | 提高写入性能 |

```kotlin
// 分页查询示例
@Query("SELECT * FROM pigeons ORDER BY updated_at DESC")
fun getAllPigeonsPaged(): PagingSource<Int, PigeonEntity>
```

#### 9.1.2 内存优化

| 优化点 | 具体措施 | 目标 |
|--------|---------|------|
| 大图压缩 | 照片最大边1200px，单张<500KB | 避免OOM |
| Glide缓存 | 内存缓存+磁盘缓存+LruBitmapPool | 流畅滚动 |
| ViewHolder复用 | RecyclerView标准ViewHolder模式 | 减少inflate |
| 生命周期感知 | ViewModel + LiveData自动清理 | 避免泄漏 |
| 懒加载 | 家族图谱按需计算布局 | 减少初始内存占用 |

#### 9.1.3 渲染性能（家族图谱）

| 优化点 | 具体措施 |
|--------|---------|
| 硬件加速 | Canvas使用硬件加速（默认开启） |
| 增量绘制 | 仅变换矩阵变化时重绘，不重新计算布局 |
| 离屏缓存 | 静态内容缓存为Bitmap，交互时仅叠加变换 |
| 节点数量限制 | 最大展示100个节点，超出提示缩小深度 |
| 简化绘制 | 缩放<0.5x时隐藏节点文字，仅显示颜色块 |

```kotlin
// 渲染优化：缩放级别适配
override fun onDraw(canvas: Canvas) {
    val scale = getCurrentScale()

    // 缩小时简化渲染
    val showText = scale >= 0.5f
    val showPhoto = scale >= 0.8f
    val showBorder = scale >= 0.3f

    // ... 根据标志位控制绘制细节
}
```

### 9.2 安全设计

| 安全项 | 措施 |
|--------|------|
| 数据安全 | 所有数据本地存储，不上传云端 |
| 文件共享 | 使用FileProvider生成临时content URI，避免file://暴露路径 |
| 权限最小化 | 仅申请必要权限（存储、相机），Android 10+使用Scoped Storage |
| 备份加密 | 支持导出时设置密码保护（ZIP密码加密） |
| 数据库安全 | 存储在应用的内部存储，其他应用无法直接访问 |

### 9.3 老年友好设计清单

| 设计项 | 实现标准 |
|--------|---------|
| 字体大小 | 正文18sp+，标题22sp+ |
| 按钮尺寸 | 最小触摸目标 56dp x 56dp |
| 对比度 | 文字与背景对比度 >= 4.5:1 |
| 颜色辅助 | 不使用颜色作为唯一信息载体（如性别同时用文字和颜色） |
| 操作确认 | 删除等重要操作二次确认 |
| 返回提示 | 按返回键时提示"再按一次退出" |
| 加载提示 | 耗时操作显示"请稍候..." |
| 错误提示 | 用大白话说明错误原因和解决方法 |
| 语音辅助 | 关键界面支持TalkBack屏幕朗读 |
| 震动反馈 | 操作成功/失败提供震动提示 |

---

## 附录

### A. Gradle 依赖配置参考

```kotlin
// build.gradle.kts (app module)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    kotlin("kapt")
}

android {
    namespace = "com.pigeonnest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pigeonnest"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Glide Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // WorkManager (自动备份)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

### B. 版本规划

| 版本 | 功能范围 | 预计工期 |
|------|---------|---------|
| v1.0 | 鸽子档案、鸽棚管理、位置记录、家族关系编辑 | 6周 |
| v1.1 | 家族图谱展示（自定义View）、照片管理 | 3周 |
| v1.2 | 数据备份/恢复/导出导入、存储管理 | 2周 |
| v1.3 | 老年友好优化、性能调优、用户反馈修复 | 2周 |

### C. 风险评估

| 风险项 | 影响 | 缓解措施 |
|--------|------|---------|
| 家族图谱复杂场景渲染性能 | 高 | 限制节点数、分级渲染、离屏缓存 |
| 老年用户学习成本 | 中高 | 新手引导、大按钮、简洁流程 |
| 低端设备OOM | 中高 | 图片压缩、内存监控、分页加载 |
| 数据丢失 | 高 | 自动备份、导入导出验证、操作确认 |

---

> **文档结束**  
> 本技术规格文档作为《鸽巢管家》Android应用的开发基准，所有技术决策均围绕"老年用户友好"和"中低端设备适配"两大核心约束进行。开发过程中如需调整技术方案，应更新本文档并记录变更原因。

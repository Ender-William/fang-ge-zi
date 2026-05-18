# 《鸽巢管家》Android 应用代码审计报告

> **审计日期**：2026-05-18  
> **审计范围**：`PigeonNest/` 目录下全部源代码  
> **参考文档**：
> - `docs/doc_prd.md`（产品需求文档）
> - `docs/doc_design.md`（视觉设计规范）
> - `docs/doc_spec.md`（技术规格文档）

---

## 一、审计概览

| 层级 | 当前状态 | 问题数量 | 风险等级 |
|------|---------|---------|---------|
| 基础设施层 | ⚠️ 部分符合 | 5 | 中 |
| 数据层 | ⚠️ 部分符合 | 9 | 高 |
| 领域层 | ⚠️ 部分符合 | 7 | 高 |
| UI 核心层 | ⚠️ 部分符合 | 8 | 中 |
| 扩展 UI 层 | ⚠️ 部分符合 | 6 | 中 |
| 家族图谱层 | ⚠️ 部分符合 | 8 | 高 |

**总体评估**：项目已完成基础架构搭建，核心 MVVM + Hilt + Room 框架就位，主要页面（列表、详情、编辑、设置、图谱）已 skeleton 实现。但存在大量与 PRD/设计/技术规格不符的缺口，尤其是**数据库 schema 缺失家族关系独立表**、**家族图谱仅支持单代渲染**、**字体/高对比度设置未生效**、**minSdk 高于要求**等关键问题。

---

## 二、各阶段详细审计

### 2.1 基础设施层（Build / Manifest / DI / Gradle）

#### 问题 INF-01 [P0] minSdk 29 不符合文档要求的 API 26
- **文件**：`PigeonNest/app/build.gradle.kts`
- **行号**：15
- **问题描述**：`minSdk = 29`（Android 10），但 PRD 与技术规格均明确要求最低支持 **Android 8.0（API 26）**。目标用户多为老年群体，使用千元机，Android 8/9 设备占比不可忽略。
- **应如何修复**：将 `minSdk = 29` 改为 `minSdk = 26`，并检查所有 API 29+ 独占调用（如 `Environment.getExternalStoragePublicDirectory` 在 Android 10+ 已废弃）。

#### 问题 INF-02 [P0] 缺少 SQLCipher 数据库加密
- **文件**：`PigeonNest/app/build.gradle.kts`
- **行号**：74-76（Room 依赖区域）
- **问题描述**：PRD 5.2 安全性需求明确要求 **"本地数据加密"（P0）**，技术规格也提到数据库安全。当前仅使用普通 Room/SQLite，未集成 SQLCipher。
- **应如何修复**：添加 `net.zetetic:android-database-sqlcipher` 依赖，并将 `Room.databaseBuilder` 替换为 `Room.databaseBuilder(...).openHelperFactory(SupportFactory(passphrase))`。

#### 问题 INF-03 [P1] Room 使用 fallbackToDestructiveMigration() 过于粗暴
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/di/DatabaseModule.kt`
- **行号**：30
- **问题描述**：技术规格 7.1 风险 T04 强调需使用 Room Migration 机制，`fallbackToDestructiveMigration()` 会在 schema 变更时直接清空用户数据，对老年用户是灾难性体验。
- **应如何修复**：移除 `fallbackToDestructiveMigration()`，定义 `Migration` 对象；导出 schema (`exportSchema = true`) 并纳入版本控制。

#### 问题 INF-04 [P1] 缺少 UseCaseModule
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/di/`
- **行号**：目录层级
- **问题描述**：技术规格 7.1 目录结构要求存在 `di/UseCaseModule.kt`，当前仅有 `DatabaseModule`、`DispatcherModule`、`RepositoryModule`。
- **应如何修复**：新增 `UseCaseModule.kt`，使用 `@Provides` 或 `@Binds` 注入各 UseCase。

#### 问题 INF-05 [P2] 声明了 INTERNET 权限与纯离线定位冲突
- **文件**：`PigeonNest/app/src/main/AndroidManifest.xml`
- **行号**：5
- **问题描述**：PRD 5.2 / 5.3 明确 V1.0 为纯离线应用，不上传网络。Manifest 中仍声明了 `<uses-permission android:name="android.permission.INTERNET" />`，虽无实际网络代码，但会给隐私敏感用户及审核带来困惑。
- **应如何修复**：移除 `INTERNET` 权限声明（如后续版本需要再加回）。

---

### 2.2 数据层（Entity / DAO / Database / Repository / File）

#### 问题 DATA-01 [P0] 缺少独立的 FamilyRelationEntity 表
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/local/entity/PigeonEntity.kt`
- **行号**：67-74
- **问题描述**：PRD 6.1 与技术规格 4.2.3 均要求独立的 `family_relations` 表管理家族关系（含 father_id / mother_id / mate_id 的外键约束、级联删除等）。当前实现将 `father_id`、`mother_id`、`mate_id` 直接放在 `pigeons` 表中，虽功能上可工作，但：
  - 无法支持多配偶历史（P1 需求）
  - 无法清晰管理关系元数据（如关系建立时间）
  - 与技术规格的数据模型设计严重不符
- **应如何修复**：
  1. 新建 `FamilyRelationEntity.kt`，按技术规格 4.2.3 定义独立表；
  2. 从 `PigeonEntity` 中移除 `father_id` / `mother_id` / `mate_id`；
  3. 新建 `FamilyRelationDao.kt`；
  4. 更新 `PigeonNestDatabase` entities 列表。

#### 问题 DATA-02 [P0] 缺少 PigeonPhotoEntity 表与 PigeonPhotoDao
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/local/database/PigeonNestDatabase.kt`
- **行号**：14-18
- **问题描述**：技术规格 4.2.4 明确要求 `pigeon_photos` 表支持多张照片管理（含主照片标记 `is_primary`）。当前数据库仅有 `PigeonEntity`（单张照片路径 `photoPath`），无法支持多照片。
- **应如何修复**：
  1. 新建 `PigeonPhotoEntity.kt`；
  2. 新建 `PigeonPhotoDao.kt`；
  3. 注册到 Database entities；
  4. 在 Repository 层替换单照片逻辑。

#### 问题 DATA-03 [P0] BackupManager.importBackup() 未真正执行导入
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/file/BackupManager.kt`
- **行号**：99-128
- **问题描述**：`importBackup()` 方法仅解压 ZIP 并检查 `data.json` 存在，最终返回 `Result.success("导入准备完成")`，完全没有将数据写入数据库。PRD 要求数据导入恢复为 P0 功能。
- **应如何修复**：按技术规格 6.2.2 实现完整的 REPLACE / MERGE 两种导入模式，解析 JSON 后调用各 DAO 插入数据，并复制照片到目标目录。

#### 问题 DATA-04 [P1] BackupManager.exportBackup() 未导出 family_relations 和 pigeon_photos
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/file/BackupManager.kt`
- **行号**：60-68
- **问题描述**：备份数据仅包含 `lofts` 和 `pigeons`，缺少 `family_relations` 和 `pigeon_photos`，导致备份不完整。
- **应如何修复**：注入 `FamilyRelationDao` 和 `PigeonPhotoDao`，将对应数据写入 JSON 的 `data` 字段。

#### 问题 DATA-05 [P1] Database exportSchema = false 与规范不符
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/local/database/PigeonNestDatabase.kt`
- **行号**：20
- **问题描述**：技术规格 4.6 明确要求 `exportSchema = true` 用于版本控制。
- **应如何修复**：改为 `exportSchema = true`，并创建 `app/schemas/` 目录存放自动导出的 JSON schema。

#### 问题 DATA-06 [P1] PigeonRepositoryImpl.deletePigeon() 未删除关联照片
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/repository/PigeonRepositoryImpl.kt`
- **行号**：84-91
- **问题描述**：技术规格 8.3 Repository 实现示例中，`deletePigeon()` 应先调用 `photoStorage.deletePigeonPhotos()` 再删除数据库记录。当前仅执行 `softDelete`。
- **应如何修复**：注入 `PhotoStorageManager`，在 `softDelete` 前调用 `deletePigeonPhotos(pigeonId)`。

#### 问题 DATA-07 [P1] PigeonRepositoryImpl 缺少 addPigeonPhoto / deletePigeonPhoto 方法
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/repository/PigeonRepositoryImpl.kt`
- **行号**：整体
- **问题描述**：技术规格 8.1 `PigeonRepository` 接口定义了照片操作方法，但当前实现类中完全缺失。
- **应如何修复**：补充 `addPigeonPhoto` 和 `deletePigeonPhoto` 实现（参考技术规格 8.3 示例）。

#### 问题 DATA-08 [P1] LocationHistoryEntity 无完整业务闭环
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/local/entity/LocationHistoryEntity.kt`
- **行号**：整体
- **问题描述**：PRD 4.4 流程四要求位置变更时记录历史。`LocationHistoryEntity` 和 `LocationHistoryDao` 已存在，但 `PigeonRepositoryImpl.updatePigeonLocation()` 中未插入位置历史记录。
- **应如何修复**：在 `updatePigeonLocation()` 成功前，向 `location_history` 表插入一条变更记录（含 fromLoftId / toLoftId / moveDate / note）。

#### 问题 DATA-09 [P2] PigeonEntity 缺少 `updated_at` 的自动维护触发
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/data/local/entity/PigeonEntity.kt`
- **行号**：79-80
- **问题描述**：`updatedAt` 默认值为 `System.currentTimeMillis()`，但仅在 Kotlin 构造时生效；通过 `@Update` 更新时不会自动刷新。
- **应如何修复**：在 Repository 层的 `savePigeon` / `updatePigeonLocation` 等方法中手动设置 `updatedAt = System.currentTimeMillis()`，或使用 Room 的 `@Update` 前触发逻辑。

---

### 2.3 领域层（Model / Repository Interface / UseCase）

#### 问题 DOMAIN-01 [P0] 缺少 FamilyRepository 接口与实现
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/repository/`
- **行号**：目录层级
- **问题描述**：技术规格 8.1 明确定义了 `FamilyRepository` 接口（含 `getFamilyRelation`、`getLineage`、`getChildren`、`updateParents`、`updateMate`、`getGraphData` 等方法），当前项目中完全缺失。导致家族关系业务逻辑散落在各 Fragment/ViewModel 中。
- **应如何修复**：
  1. 新建 `domain/repository/FamilyRepository.kt`；
  2. 新建 `data/repository/FamilyRepositoryImpl.kt`；
  3. 在 `RepositoryModule` 中绑定。

#### 问题 DOMAIN-02 [P0] 缺少家族关系相关 UseCase
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/usecase/`
- **行号**：目录层级
- **问题描述**：技术规格要求存在 `GetFamilyGraphUseCase`、`UpdateFamilyRelationUseCase`、`GetLineageUseCase`，当前均缺失。`FamilyGraphViewModel` 直接在 ViewModel 层调用 `GraphLayoutManager`，绕过领域层。
- **应如何修复**：按技术规格 8.2 新建 `domain/usecase/family/` 包及对应 UseCase。

#### 问题 DOMAIN-03 [P1] 缺少 BackupRepository 接口
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/repository/`
- **行号**：目录层级
- **问题描述**：技术规格 8.1 要求 `BackupRepository` 接口，当前 `BackupManager` 直接暴露在 SettingsViewModel 中，未经过 Repository 抽象层。
- **应如何修复**：新建 `BackupRepository` 接口，将 `BackupManager` 包装为 `BackupRepositoryImpl`。

#### 问题 DOMAIN-04 [P1] GetPigeonDetailUseCase 未加载家族关系
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/usecase/pigeon/GetPigeonDetailUseCase.kt`
- **行号**：8-14
- **问题描述**：技术规格 8.2 示例中，`GetPigeonDetailUseCase` 应同时加载 `familyRepository.getFamilyRelation()` 并 `copy` 到 Pigeon 对象。当前仅返回基础鸽子信息，导致详情页父母/配偶/后代信息无法展示。
- **应如何修复**：注入 `FamilyRepository`，在返回前填充 `familyRelation`。

#### 问题 DOMAIN-05 [P1] SavePigeonUseCase 未处理照片上传
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/usecase/pigeon/SavePigeonUseCase.kt`
- **行号**：10-64
- **问题描述**：技术规格 8.2 `SavePigeonUseCase.Params` 包含 `photoUri: Uri?`，且规范要求在保存鸽子后单独处理照片。当前 `Params` 缺少 `photoUri`，导致照片上传流程断裂。
- **应如何修复**：在 `Params` 中添加 `photoUri`，并在 `invoke()` 中保存鸽子后调用 `pigeonRepository.addPigeonPhoto()`。

#### 问题 DOMAIN-06 [P1] 缺少 PigeonPhoto 领域模型
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/model/`
- **行号**：目录层级
- **问题描述**：技术规格 4.3 要求存在 `PigeonPhoto` 领域模型，当前缺失。
- **应如何修复**：新建 `PigeonPhoto.kt` 数据类（含 id / pigeonId / photoPath / caption / takenDate / isPrimary）。

#### 问题 DOMAIN-07 [P2] 缺少 NoParamUseCase / FlowUseCase 基类
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/usecase/`
- **行号**：目录层级
- **问题描述**：技术规格 8.2 要求定义 `NoParamUseCase`、`UseCase`、`FlowUseCase` 基类以统一 UseCase 风格。当前各 UseCase 均为独立类，无统一基类。
- **应如何修复**：新建 `domain/usecase/base/` 包及三个抽象基类。

---

### 2.4 UI 核心层（Fragment / ViewModel / Adapter / Layout）

#### 问题 UI-01 [P0] 字体大小设置未全局生效
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/settings/SettingsViewModel.kt`
- **行号**：49-57
- **问题描述**：PRD 用户故事 6 与技术规格 9.3 均要求字体大小变更后**全局即时生效**，无需重启应用。当前 `setFontSize()` 仅保存到 SharedPreferences 并更新 UI 状态文本，没有任何机制通知各页面刷新字号。
- **应如何修复**：
  1. 实现 `Activity.recreate()` 或自定义 `ContextThemeWrapper` 动态换肤；
  2. 或使用 `LiveData/Flow` 广播字体变更事件，各 Fragment 观察后重新设置 TextSize；
  3. 更推荐方案：在 `Application` 或 `BaseActivity` 中根据 SharedPreferences 动态计算 `scaledDensity` 并应用到 `Resources`。

#### 问题 UI-02 [P0] 高对比度模式设置未生效
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/settings/SettingsViewModel.kt`
- **行号**：59-62
- **问题描述**：PRD 5.5.1 要求高对比度模式切换后全局生效。当前仅保存布尔值到 SharedPreferences，无任何 UI 层响应逻辑。
- **应如何修复**：实现主题切换机制（如 `AppCompatDelegate.setLocalNightMode()` 的自定义等价方案，或维护两套 Theme 并动态 `Activity.recreate()`）。

#### 问题 UI-03 [P1] 鸽子编辑页面非分步表单设计
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/pigeonedit/PigeonEditFragment.kt`
- **行号**：整体
- **问题描述**：PRD 4.1 / 设计规范 6.2 明确要求添加鸽子采用**三步流程**（基本信息 → 位置信息 → 家族关系），每步有进度指示器。当前页面为单页长表单，仅含基础信息输入，缺失鸽舍选择、父母/配偶选择步骤。
- **应如何修复**：按设计规范 6.2 重构为 Wizard/Stepper 形式（可用 ViewPager2 + Fragment 或单个 Fragment 内分步骤显示/隐藏）。

#### 问题 UI-04 [P1] 鸽子列表页缺少品种筛选标签栏
- **文件**：`PigeonNest/app/src/main/res/layout/fragment_pigeon_list.xml`
- **行号**：整体
- **问题描述**：设计规范 6.1.1 首页要求有横向滚动的品种筛选标签（全部/信鸽/赛鸽/肉鸽/观赏鸽）。当前布局仅有搜索框后直接接 RecyclerView。
- **应如何修复**：在搜索框下方添加 `RecyclerView` 或 `ChipGroup` 作为筛选标签栏。

#### 问题 UI-05 [P1] 鸽子详情页未显示真实父母/配偶/后代信息
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/pigeondetail/PigeonDetailFragment.kt`
- **行号**：123-139
- **问题描述**：当前详情页父母/配偶仅显示 `"已记录 (ID: xxx...)"` 或 `"未记录"`，未加载真实鸽子名称和照片。PRD 4.2 / 设计规范 6.4 要求显示 clickable 的卡片，点击可跳转到对应鸽子详情。
- **应如何修复**：ViewModel 加载 `FamilyRelation` 对象，绑定真实 `PigeonBrief` 数据到卡片布局。

#### 问题 UI-06 [P1] 鸽子详情页缺少后代列表展示
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/pigeondetail/PigeonDetailFragment.kt`
- **行号**：整体
- **问题描述**：设计规范 6.4.1 要求"后代"区域显示所有后代列表（如小小白♂ · 小小灰♀）。当前页面完全无此区域。
- **应如何修复**：在详情页布局添加后代列表区域，ViewModel 调用 `familyRepository.getChildren()` 获取数据。

#### 问题 UI-07 [P2] 搜索框使用系统 SearchView 而非适老化大输入框
- **文件**：`PigeonNest/app/src/main/res/layout/fragment_pigeon_list.xml`
- **行号**：22-33
- **问题描述**：PRD 用户故事 5 要求搜索框高度不低于 56dp、输入时实时显示结果。当前使用 `androidx.appcompat.widget.SearchView`，其默认样式在老年设备上可能过小，且搜索图标和文字布局不够突出。
- **应如何修复**：使用自定义 EditText（56dp 高，圆角 12dp，#FFFFFF 背景）替代 SearchView，配合 `TextWatcher` 实现实时搜索。

#### 问题 UI-08 [P2] 缺少"最近查看"区域
- **文件**：`PigeonNest/app/src/main/res/layout/fragment_pigeon_list.xml`
- **行号**：整体
- **问题描述**：PRD 用户故事 8 要求首页有"最近查看"区域，展示最近 10 只操作过的鸽子。当前布局无此区域。
- **应如何修复**：在搜索框/筛选栏与主列表之间添加横向 RecyclerView 作为最近查看区域；Repository 层已实现 `getRecentPigeons()`，只需 UI 层接入。

---

### 2.5 扩展 UI 层（设置 / 辅助功能 / 空状态 / 动画）

#### 问题 EXT-01 [P1] 设置页缺少"减少动画"选项
- **文件**：`PigeonNest/app/src/main/res/layout/fragment_settings.xml`
- **行号**：整体
- **问题描述**：设计规范 9.5 要求提供"减少动画"选项，开启后转场变为淡入淡出、按钮取消缩放、列表直接显示。当前设置页仅有字体大小和高对比度。
- **应如何修复**：在设置页添加 Switch 控件，保存到 SharedPreferences；各页面动画逻辑读取该标志进行条件判断。

#### 问题 EXT-02 [P1] 设置页缺少"使用指南"入口
- **文件**：`PigeonNest/app/src/main/res/layout/fragment_settings.xml`
- **行号**：整体
- **问题描述**：设计规范 6.6.1 / PRD 2.1 要求设置页包含使用引导教程入口。当前设置页仅有导出/导入/关于。
- **应如何修复**：在设置页"帮助与反馈"分类下添加"使用指南"入口（可先占位，后续填充图文教程页）。

#### 问题 EXT-03 [P1] 鸽子编辑/详情页缺少照片拍摄/选择功能
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/pigeonedit/PigeonEditFragment.kt`
- **行号**：整体
- **问题描述**：PRD 2.1 中"鸽子照片拍摄/上传"为 P1 功能，设计规范 6.2.1 要求有"拍照"和"从相册选"按钮。当前编辑页面无任何照片相关 UI。
- **应如何修复**：在编辑页添加照片区域（默认显示鸽子剪影占位图 + 拍照/相册按钮），调用系统相机/相册 Intent，保存时通过 `PhotoStorageManager` 压缩存储。

#### 问题 EXT-04 [P2] 缺少操作音效/震动反馈
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/`
- **行号**：整体
- **问题描述**：PRD 2.1 系统设置中要求"操作音效/震动"为 P2 功能。当前各按钮点击无任何震动反馈。
- **应如何修复**：在关键操作（保存成功、删除确认）处添加 `Vibrator.vibrate()` 调用。

#### 问题 EXT-05 [P2] 鸽子编辑页面缺少日期选择器
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/pigeonedit/PigeonEditFragment.kt`
- **行号**：整体
- **问题描述**：PRD 4.1 / 设计规范 6.2 要求有出生日期、入棚日期的日期选择器。当前表单无日期相关输入。
- **应如何修复**：添加日期选择触发按钮，使用 `MaterialDatePicker` 或原生 DatePickerDialog。

#### 问题 EXT-06 [P2] 鸽子编辑页面缺少羽色网格单选
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/pigeonedit/PigeonEditFragment.kt`
- **行号**：整体
- **问题描述**：设计规范 6.2.1 要求羽色选择使用 6 个 64x64dp 的网格单选按钮（雨点/灰/红/白/黑/花）。当前仅为文本输入框。
- **应如何修复**：将颜色输入框替换为 `RecyclerView` 网格或自定义 RadioGroup，提供预设羽色选项。

---

### 2.6 家族图谱层（FamilyGraphView / GraphLayoutManager / Fragment）

#### 问题 GRAPH-01 [P0] GraphLayoutManager 仅渲染单代关系，未递归构建多代
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/familygraph/GraphLayoutManager.kt`
- **行号**：10-46
- **问题描述**：PRD 4.3 / 设计规范 8.1 要求默认展示上下各 2-3 代祖先和后代。当前 `buildGraphFromPigeon()` 仅加载当前鸽子的**直接**父亲、母亲、配偶、子女，完全没有递归向上查询祖父祖母/外祖父外祖母，也没有递归向下查询孙辈。
- **应如何修复**：
  1. 实现递归函数 `buildAncestors(node, depth)` 和 `buildDescendants(node, depth)`；
  2. 使用 `depth` 参数控制代数（默认 3 代）；
  3. 注意循环引用检测（防止自循环）。

#### 问题 GRAPH-02 [P0] 缺少 FamilyRepository.getGraphData() 接口及数据聚合逻辑
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/repository/`
- **行号**：目录层级
- **问题描述**：技术规格 8.1 `FamilyRepository` 要求 `getGraphData(centerPigeonId, depth)` 方法返回 `GraphData` 对象。当前 `FamilyGraphViewModel` 直接在 ViewModel 层拼凑数据，违反分层架构。
- **应如何修复**：在 `FamilyRepository` / `FamilyRepositoryImpl` 中实现 `getGraphData()`，封装所有递归查询逻辑。

#### 问题 GRAPH-03 [P1] 缩放范围 0.3x~3.0x 不符合设计规范
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/familygraph/FamilyGraphView.kt`
- **行号**：55-56
- **问题描述**：设计规范 8.5 明确要求缩放范围 **0.5x ~ 2.0x**。当前 `minScale = 0.3f`、`maxScale = 3.0f`，缩放过大/过小都会导致老年用户迷失。
- **应如何修复**：改为 `minScale = 0.5f`、`maxScale = 2.0f`。

#### 问题 GRAPH-04 [P1] 节点绘制缺少头像图片
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/familygraph/FamilyGraphView.kt`
- **行号**：87-129
- **问题描述**：设计规范 8.3.1 要求节点卡片包含 36dp 圆形头像。当前 `drawSingleNode()` 仅绘制名称和性别标记，完全不绘制照片/占位图。
- **应如何修复**：在 `drawSingleNode()` 中使用 `Canvas.drawBitmap()` 或 Glide 的 `submit()` 预加载 Bitmap 后绘制到指定区域（圆形裁剪）。

#### 问题 GRAPH-05 [P1] 配偶连线未使用虚线效果
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/familygraph/FamilyGraphView.kt`
- **行号**：45-49
- **问题描述**：设计规范 8.4 要求配偶连线为"3dp 双线 #D4A03D（暖金黄）"。当前 `mateEdgePaint` 为实线，且未设置 `DashPathEffect`。
- **应如何修复**：给 `mateEdgePaint` 设置 `pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)`，并确保颜色为 `#D4A03D`。

#### 问题 GRAPH-06 [P1] 节点尺寸与设计规范不符
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/domain/model/FamilyGraph.kt`
- **行号**：19-20
- **问题描述**：设计规范 8.3.1 要求节点宽度 **104dp**、高度自适应（最小 80dp）。当前 `NODE_WIDTH = 200f`、`NODE_HEIGHT = 120f`，过大导致屏幕上可显示节点数减少。
- **应如何修复**：按设计规范调整为 `NODE_WIDTH = 104f`（或适配屏幕密度后的 px 值），`NODE_HEIGHT` 自适应。

#### 问题 GRAPH-07 [P2] 性别标记使用文字而非符号
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/familygraph/FamilyGraphView.kt`
- **行号**：117-128
- **问题描述**：设计规范 8.2 要求使用 ♂ / ♀ / ? 符号标识性别。当前代码使用"雄"/"雌"/"?"。
- **应如何修复**：改为 `"♂"`、`"♀"`、`"?"` 字符。

#### 问题 GRAPH-08 [P2] 双指缩放与单指拖拽可能存在手势冲突
- **文件**：`PigeonNest/app/src/main/java/com/pigeonnest/presentation/familygraph/FamilyGraphView.kt`
- **行号**：165-175
- **问题描述**：`onTouchEvent()` 同时向 `scaleGestureDetector` 和 `gestureDetector` 传递事件，未做区分。当双指缩放时，`GestureDetector.onScroll` 可能同时触发，导致拖拽与缩放冲突。
- **应如何修复**：在 `onTouchEvent` 中检测 `event.pointerCount`，双指时仅传递给 `scaleGestureDetector`，单指时仅传递给 `gestureDetector`；或参考技术规格 5.4 示例添加 `isScaling` 标志位进行互斥。

---

## 三、修复计划（按阶段排序）

### Phase 1：数据层 schema 修正（高优先级，阻塞后续开发）

| 编号 | 任务 | 影响文件 | 预估工时 |
|------|------|---------|---------|
| 1.1 | 新建 `FamilyRelationEntity`、`FamilyRelationDao`，从 `PigeonEntity` 移除家族关系字段 | entity/dao/database | 4h |
| 1.2 | 新建 `PigeonPhotoEntity`、`PigeonPhotoDao` | entity/dao/database | 3h |
| 1.3 | 更新 `PigeonNestDatabase` entities 与版本号，编写 Migration | database | 2h |
| 1.4 | 更新 `PigeonMapper` 以适配新 schema | mapper | 2h |
| 1.5 | 实现 `FamilyRepository` 接口与 `FamilyRepositoryImpl` | domain/data | 6h |
| 1.6 | 补全 `BackupManager.importBackup()` 真正导入逻辑 + 导出 family/photo 数据 | data/file | 4h |

### Phase 2：基础设施与架构补齐

| 编号 | 任务 | 影响文件 | 预估工时 |
| 2.1 | 降低 minSdk 至 26，检查并替换废弃 API（如外部存储） | build.gradle.kts | 3h |
| 2.2 | 集成 SQLCipher 加密数据库 | build.gradle.kts / DatabaseModule | 4h |
| 2.3 | 新建 `UseCaseModule`、补充 `BackupRepository` 接口 | di / domain | 2h |
| 2.4 | 补全 UseCase 基类及家族关系/备份相关 UseCase | domain/usecase | 4h |

### Phase 3：核心 UI 功能完善

| 编号 | 任务 | 影响文件 | 预估工时 |
| 3.1 | 实现字体大小动态切换机制（全局生效） | settings / base activity | 4h |
| 3.2 | 实现高对比度模式切换机制 | settings / theme | 3h |
| 3.3 | 重构鸽子编辑为三步分步表单 | pigeonedit | 8h |
| 3.4 | 鸽子详情页绑定真实家族关系数据（父母/配偶/后代卡片） | pigeondetail | 4h |
| 3.5 | 首页添加品种筛选标签栏 + 最近查看区域 | pigeonlist | 4h |

### Phase 4：家族图谱完善

| 编号 | 任务 | 影响文件 | 预估工时 |
| 4.1 | 实现递归多代图谱数据构建（ ancestors / descendants ） | familygraph / repository | 6h |
| 4.2 | 调整节点尺寸、缩放范围、连线样式以符合设计规范 | FamilyGraphView / GraphLayoutManager | 3h |
| 4.3 | 添加节点头像绘制 | FamilyGraphView | 3h |
| 4.4 | 修复双指/单指手势冲突 | FamilyGraphView | 2h |

### Phase 5：扩展功能与优化

| 编号 | 任务 | 影响文件 | 预估工时 |
| 5.1 | 照片拍摄/上传功能（编辑页 + 详情页头像点击） | pigeonedit / pigeondetail | 4h |
| 5.2 | 位置变更时记录 `LocationHistory` | locationset / repository | 2h |
| 5.3 | 添加"减少动画"开关 + 使用指南入口 | settings | 2h |
| 5.4 | 添加操作震动反馈 | 各 Fragment | 1h |
| 5.5 | 羽色网格单选、日期选择器 | pigeonedit | 2h |
| 5.6 | 代码审查、单元测试补全 | test/ | 4h |

---

## 四、符合项清单（当前已做到位的内容）

为公平起见，以下列出代码中已经**正确遵循**文档要求的部分：

1. **技术栈选型**：Kotlin + MVVM + Hilt + Room + XML Layout + Glide，与技术规格 2.1 完全一致。
2. **包结构**：`presentation / domain / data / di` 分层清晰，符合技术规格 3.3 / 7.1。
3. **配色系统**：`colors.xml` 中主色 `#E8751A`、背景 `#FAF7F2`、文字 `#2E2E28`、性别色等与设计规范 2.2 完全一致。
4. **基础尺寸**：`dimens.xml` 中按钮 64dp、输入框 56dp、正文 18sp、标题 32sp 等符合设计规范 3.2 / 7.1。
5. **软删除机制**：`PigeonEntity.isDeleted` 与 `LoftEntity.isDeleted` 已按 PRD 6.3 R06 实现。
6. **空状态设计**：`fragment_pigeon_list.xml` 空状态布局（128dp 图标 + 22sp 标题 + 18sp 说明）与设计规范 6.1.3 一致。
7. **底部导航**：3 Tab（我的鸽子/家族图谱/设置）与设计规范 5.6 / 6.1 推荐配置一致。
8. **搜索功能**：实时模糊搜索（环号/昵称/颜色/笼位）已按技术规格 4.5 实现。
9. **照片存储压缩**：`PhotoStorageManager` 最大边 1200px、单张 <500KB，与技术规格 6.3 一致。
10. **Hilt 注入**：`@HiltAndroidApp`、`@HiltViewModel`、`@AndroidEntryPoint` 使用正确。

---

*本报告由审计 Agent 生成，仅供开发团队参考。具体修复实施请交由后续循环 Agent 执行。*

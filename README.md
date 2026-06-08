# 放鸽子 (PigeonNest)

[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey)]()

专为养鸽爱好者设计的 Android 应用，帮助您记录和管理每一羽鸽子的信息、鸽舍、家族血统关系，并支持可视化家族图谱与 PDF 血统档案导出。

## 📱 应用截图与界面

应用采用底部标签导航，包含四个主 Tab：

| Tab | 功能 |
|-----|------|
| 🐦 我的鸽子 | 鸽子列表、搜索、最近查看、添加/编辑鸽子 |
| 🏠 鸽舍管理 | 鸽舍列表、添加/编辑/删除鸽舍 |
| 👨‍👩‍👧 家族图谱 | 家族列表、可视化血统图谱 |
| ⚙️ 设置 | 字体/主题、数据备份、关于 |

---

## ✨ 功能概览

### 🐦 我的鸽子

- **鸽子列表**：展示所有鸽子，支持按名字、脚环号进行单关键词实时模糊搜索
- **最近查看**：顶部横向快捷入口，自动记录最近浏览的鸽子（最多 10 只）
- **添加新鸽子**：3 步引导式表单
  - 第 1 步：基本信息（名字、脚环号、性别、羽色、出生日期、状态、全身照、眼砂照、备注）
  - 第 2 步：位置信息（选择鸽棚和笼号）
  - 第 3 步：家族关系（选择父亲、母亲、配偶）
- **编辑鸽子**：可修改所有字段，包括性别。修改性别后，父亲/母亲/配偶的候选列表会按新性别重新过滤（已有关联不会自动解除，请用户自行检查一致性）
- **鸽子详情页**：
  - 展示全身照、眼砂照（点击可放大查看）
  - 显示基本信息、年龄、羽色、性别、状态标签
  - 显示当前鸽棚和笼号
  - 展示家族关系（父亲、母亲、配偶、后代）
  - 支持快捷编辑家族关系
  - 支持一键生成 PDF 血统档案
  - 支持跳转家族图谱、变更位置
- **长按操作**：在列表中长按鸽子卡片可查看详情、编辑或删除
- **照片支持**：
  - 全身照：拍照或从相册选择
  - 眼砂照：拍照或从相册选择
  - 照片自动压缩至最大 1200px、500KB 后保存

#### 羽色选择

提供 6 种常用羽色快捷按钮：雨点、灰、红、白、黑、花，同时支持自定义羽色名称。

#### 鸽子状态

| 状态 | 含义 | 颜色 |
|------|------|------|
| 在养 | 当前正常饲养 | 绿色 |
| 已售 | 已经出售 | 蓝色 |
| 已故 | 已经死亡 | 灰色 |
| 赠送 | 赠送给他人 | 橙色 |

#### 家族关系性别校验

- **父亲**：候选列表仅显示雄鸽
- **母亲**：候选列表仅显示雌鸽
- **配偶**：若当前鸽子性别已知，自动排除同性候选

---

### 🏠 鸽舍管理

- **添加鸽舍**：输入名称快速创建
- **编辑鸽舍**：修改鸽舍名称
- **删除鸽舍**：若鸽舍中仍有鸽子，将无法删除
- **鸽舍详情**：查看该鸽棚下的所有鸽子列表，点击鸽子跳转详情，显示鸽舍位置与描述信息

> **注意**：当前版本鸽舍编辑仅支持修改名称。底层数据模型预留了位置、描述、容量、颜色标签字段，将在后续版本中开放编辑。

---

### 👨‍👩‍👧 家族图谱

- **家族列表**：
  - 自动将"无父无母"的鸽子识别为家族根节点
  - 分组展示各家族信息（家族名称、成员数量）
  - 长按家族条目可自定义家族名称
  - 支持"恢复默认"家族名称
- **家族图谱详情**：
  - 自定义 Canvas 渲染血缘关系树
  - 默认展示 ±3 代，支持调整显示深度（1 ~ 20 代，或"全部"）
  - 双指缩放 / 单指平移
  - 点击鸽子节点跳转详情页
  - 一键居中、重置视图
  - 自动从配偶关系中推断缺失的父母节点

---

### 📄 血统档案导出

在任意鸽子详情页点击"导出档案"，即可生成 A4 横向 PDF 血统档案，包含：

- 当前鸽子的全身照、眼砂照
- 基本信息（名字、脚环号、性别、羽色、出生日期、状态、笼位）
- 鸽舍信息卡片
- **三代血缘谱系**：父母、祖父母、曾祖父母，每代均含照片与基本信息
- 页脚标注生成来源

生成后进入 PDF 预览页，可通过系统分享发送到微信、邮件、打印等。

---

### ⚙️ 设置

- **字体大小**：标准 / 大 / 超大（切换后自动重启应用生效）
- **高对比度模式**：深色高对比主题，更适合视力不佳的老年用户
- **数据导出**：通过系统文件选择器将完整数据打包为 ZIP 备份
  - 备份内容：鸽子、鸽棚、家族关系、照片、位置历史、SharedPreferences（设置和家族名称）
- **数据导入**：选择备份 ZIP 恢复全部数据。导入采用**替换模式**（会先清空当前数据再写入备份），导入成功后建议重启应用
- **关于**：显示应用版本与 GitHub 项目链接 `https://github.com/Ender-William`

---

## 🚀 快速开始

### 首次使用

1. 安装应用后打开，进入**我的鸽子**页面
2. 点击右下角 ➕ 按钮，按向导填写鸽子信息
3. **基本信息页**：填写名字、脚环号、选择性别、羽色、出生日期、状态，可拍照或选照片
4. **鸽棚位置页**：选择所属鸽棚和笼号（无鸽棚可暂不选，后续在"鸽舍管理"中添加）
5. **家族关系页**：选择父亲、母亲、配偶（系统会自动按性别过滤候选列表）
6. 保存后返回列表，点击鸽子卡片查看详情

### 建立家族图谱

1. 先添加几只种鸽（不设置父母的鸽子）
2. 为它们的后代添加信息时，在第三步"家族关系"中选择对应的父母
3. 保存后，父辈详情页会自动显示后代列表
4. 进入**家族图谱** Tab，即可看到按家族分组的列表，点击进入查看可视化图谱

### 数据备份与恢复

1. 进入**设置** Tab
2. 点击"导出数据"，选择保存位置，备份文件将以 `pigeonnest_backup_YYYYMMDD_HHMMSS.zip` 格式保存
3. 需要恢复时，点击"导入数据"并选择备份 ZIP 文件
4. 导入成功后应用会提示重启，重启后即可完成恢复

### 导出血统档案

1. 在**我的鸽子**列表中点击任意鸽子进入详情页
2. 点击"导出档案"按钮，等待 PDF 生成
3. 在预览页确认内容，点击分享按钮发送

---

## 🛠 开发环境

| 项目 | 版本 |
|------|------|
| Android Studio | Hedgehog (2023.1.1) 或更高 |
| Gradle | 8.4 |
| Android Gradle Plugin | 8.2.2 |
| Kotlin | 1.9.22 |
| Java | 17 |
| compileSdk | 34 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 34 |
| versionName | 1.0.4 |
| versionCode | 3 |
| Test Runner | `com.pigeonnest.HiltTestRunner` |

---

## 📦 项目配置

### 1. 克隆项目

```bash
git clone <repository-url>
cd PigeonNest
```

### 2. 使用 Android Studio 打开

- 启动 Android Studio
- 选择 **Open**，选中 `PigeonNest` 目录
- 等待 Gradle 同步完成（首次同步可能需要下载依赖，请确保网络畅通）

### 3. 命令行编译

> 已配置 `packaging { excludes += "/META-INF/*.md" }` 以避免 JUnit 5 元数据文件与 Android 打包冲突。

在 Windows (PowerShell) 或 macOS/Linux 终端中：

```powershell
# 设置 JAVA_HOME（Windows 示例，使用 Android Studio 自带 JDK）
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# 编译 Debug 版本
./gradlew :app:assembleDebug
```

编译成功后，APK 位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

### 4. 运行测试

#### 单元测试（JVM，无需设备）

```powershell
# 运行全部单元测试（约 165 个）
./gradlew :app:testDebugUnitTest

# 运行指定测试类
./gradlew :app:testDebugUnitTest --tests "com.pigeonnest.presentation.pigeonlist.PigeonListViewModelTest"

# 运行指定包下所有测试
./gradlew :app:testDebugUnitTest --tests "com.pigeonnest.domain.usecase.pigeon.*"
```

单元测试报告位于：
```
app/build/reports/tests/testDebugUnitTest/index.html
```

#### 仪器测试（需要 Android 模拟器或真机）

```powershell
# 运行全部仪器测试
./gradlew :app:connectedDebugAndroidTest

# 运行指定仪器测试类
./gradlew :app:connectedDebugAndroidTest `
  -Pandroid.testInstrumentationRunnerArguments.class=com.pigeonnest.pigeonlist.PigeonListFragmentTest
```

仪器测试报告位于：
```
app/build/reports/androidTests/connected/debug/index.html
```

### 5. 安装到设备

确保手机已开启 USB 调试并连接电脑：

```powershell
./gradlew :app:installDebug
```

---

## 🏗 技术架构

```
┌─────────────────────────────────────────────┐
│            Presentation (UI)                │
│   Fragment + ViewModel + ViewBinding        │
│   Navigation Component + SafeArgs           │
├─────────────────────────────────────────────┤
│               Domain Layer                  │
│   UseCase + Repository Interface + Model    │
├─────────────────────────────────────────────┤
│                Data Layer                   │
│   Room + SQLCipher  │  PhotoStorage        │
│   Repository Impl   │  BackupManager       │
│                     │  PigeonPdfGenerator  │
└─────────────────────────────────────────────┘
```

### 主要依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| **Hilt** | 2.50 | 依赖注入 |
| **Room** | 2.6.1 | 本地数据库 + 内存数据库测试 |
| **SQLCipher** | 4.5.4 | 数据库加密 |
| **Navigation** | 2.7.6 | 单 Activity 多 Fragment 导航 |
| **Glide** | 4.16.0 | 图片加载与缓存 |
| **Coroutines** | 1.7.3 | 异步编程 |
| **Lifecycle** | 2.7.0 | ViewModel、StateFlow、repeatOnLifecycle |
| **Material Design 3** | 1.11.0 | UI 组件与主题 |
| **Gson** | 2.10.1 | 备份 JSON 序列化 |
| **CircleImageView** | 3.1.0 | 圆形头像 |
| **PhotoView** | 2.3.0 | 图片缩放查看 |
| **MockK** | 1.13.9 | 单元测试 Mock |
| **Turbine** | 1.0.0 | Flow 测试断言 |
| **Robolectric** | 4.11.1 | JVM 环境 Android 组件测试 |
| **Espresso** | 3.5.1 | UI 自动化测试 |

### 数据库结构

应用使用 **SQLCipher 加密** 的 SQLite 数据库，包含 6 张数据表：

| 表名 | 说明 |
|------|------|
| `pigeons` | 鸽子基本信息（名字、脚环号、性别、羽色、照片路径、眼砂照路径、状态、出生日期等） |
| `lofts` | 鸽棚/鸽舍信息（名称、位置、容量、颜色标签、排序顺序） |
| `family_relations` | 家族关系（父、母、配偶） |
| `location_history` | 鸽子位置变更历史（鸽棚、笼号、变更时间） |
| `pigeon_photos` | 鸽子多张照片记录（支持每只鸽子多张照片） |

### 核心设计亮点

1. **MVVM + Clean Architecture**：清晰的 Presentation / Domain / Data 分层
2. **依赖注入**：使用 Hilt 管理所有 Repository、UseCase、ViewModel 生命周期
3. **响应式数据流**：UI 层通过 `StateFlow` 观察数据变化，Repository 返回 `Flow`
4. **软删除机制**：鸽子和鸽舍均使用 `is_deleted` 标记，保留历史数据完整性
5. **性别校验与血缘推断**：
   - 父亲候选仅显示雄鸽，母亲候选仅显示雌鸽
   - 配偶选择自动排除同性
   - 家族图谱支持从配偶关系推断缺失的父母节点
6. **双向配偶关系**：更新配偶时自动同步双方的 `family_relations` 记录
7. **全量备份恢复**：ZIP 中包含数据库、照片、SharedPreferences，实现跨设备完整迁移
8. **适老化设计**：
   - 三种字体大小（标准 / 大 / 超大）
   - 高对比度模式
   - 大按钮、大间距、纸质化配色
   - 所有重要操作都有二次确认对话框

### 隐私与安全

- 所有数据存储在应用私有目录，数据库使用 **SQLCipher** AES-256 加密
- 首次启用加密时，应用会自动检测并清理旧的未加密数据库，避免启动崩溃
- 照片压缩后存储在应用内部存储，不暴露到系统相册
- 不收集任何用户个人信息，不连接远程服务器

---

## 📁 项目结构

```
app/src/main/java/com/pigeonnest/
├── PigeonNestApp.kt                 # Application 入口（Hilt + SQLCipher 迁移检测）
├── data/
│   ├── file/                        # 文件存储
│   │   ├── BackupManager.kt         # ZIP 备份/恢复（含照片 + SharedPreferences）
│   │   ├── PhotoStorageManager.kt   # 照片压缩、存储、加载（Glide）
│   │   └── PigeonPdfGenerator.kt    # PDF 血统档案生成（Android PdfDocument）
│   ├── local/                       # Room 数据库
│   │   ├── dao/                     # 数据访问对象
│   │   ├── database/                # Database + Migration + TypeConverter
│   │   ├── entity/                  # 实体定义
│   │   └── mapper/                  # 领域模型 ↔ 实体映射
│   └── repository/                  # 仓库实现（Pigeon / Loft / Family / Backup）
├── di/                              # Hilt 依赖注入模块
├── domain/
│   ├── model/                       # 领域模型
│   │   ├── Pigeon.kt                # 鸽子领域模型
│   │   ├── Loft.kt                  # 鸽舍领域模型
│   │   ├── FamilyRelation.kt        # 家族关系
│   │   ├── FamilyGroup.kt           # 家族分组
│   │   ├── FamilyGraph.kt           # 图谱数据结构
│   │   ├── Gender.kt                # 性别枚举（未知/雄/雌）
│   │   ├── PigeonStatus.kt          # 状态枚举（在养/已售/已故/赠送）
│   │   └── PigeonBrief.kt           # 鸽子简要信息
│   ├── repository/                  # 仓库接口
│   └── usecase/                     # 用例（按模块分组）
│       ├── pigeon/                  # 鸽子 CRUD、搜索、详情
│       ├── loft/                    # 鸽舍 CRUD
│       ├── family/                  # 家族关系、血缘、图谱
│       └── base/                    # UseCase 基类
└── presentation/
    ├── main/                        # MainActivity + 底部导航
    ├── pigeonlist/                  # 鸽子列表（搜索 + 最近查看）
    ├── pigeondetail/                # 鸽子详情（家族关系 + PDF 导出）
    ├── pigeonedit/                  # 添加/编辑鸽子（3 步向导）
    ├── loftmanage/                  # 鸽舍列表 + 鸽舍详情
    ├── familylist/                  # 家族列表（根节点分组）
    ├── familygraph/                 # 家族图谱（Canvas 自定义绘制）
    ├── locationset/                 # 位置变更（鸽棚/笼号选择）
    ├── pdfpreview/                  # PDF 预览与分享
    ├── settings/                    # 设置（字体/主题/备份/关于）
    └── common/                      # 通用组件（图片查看对话框）

app/src/test/java/com/pigeonnest/          # 单元测试（~165 个）
app/src/androidTest/java/com/pigeonnest/   # 仪器测试（Espresso + Hilt）
```

---

## 🧪 测试覆盖

### 单元测试

| 层级 | 覆盖内容 |
|------|---------|
| **Domain** | Gender、PigeonStatus、FamilyGroup、FamilyGraph 模型行为 |
| **UseCase** | Pigeon CRUD、Search、Loft、Family Relation、Lineage、Graph 等全部用例 |
| **Data** | 全部 DAO 的 CRUD、搜索、过滤、外键约束测试 |
| **Mapper** | PigeonMapper、LoftMapper 双向映射 |
| **Repository** | PigeonRepositoryImpl、LoftRepositoryImpl、FamilyRepositoryImpl |
| **Presentation** | 全部 ViewModel 的 State 变化、事件处理、异常路径 |

### 仪器测试

| 测试 | 内容 |
|------|------|
| `PigeonListFragmentTest` | 鸽子列表 Fragment 渲染与导航 |
| `SettingsFragmentTest` | 设置 Fragment 主题切换 |
| `MainActivityTest` | 底部导航与 Tab 回退栈 |
| `PigeonCrudFlowTest` | 端到端增删改查流程 |

### 测试基础设施

- `CoroutineTestRule`：统一替换 `Dispatchers.Main` 为 `StandardTestDispatcher`
- `TestFixtures`：集中化的测试数据工厂
- `DaoTestBase`：Room 内存数据库测试基类
- `HiltTestRunner`：仪器测试专用 Hilt 运行器
- `TestDatabaseModule` / `TestDispatcherModule`：测试环境 DI 模块

---

## 🤝 开源与联系

- **GitHub**: [https://github.com/Ender-William](https://github.com/Ender-William)
- 本项目为个人学习/实用项目，保留所有权利。

如有问题或建议，欢迎通过 GitHub 联系。

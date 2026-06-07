# 放鸽子 (PigeonNest)

专为养鸽爱好者设计的 Android 应用，帮助您记录和管理每一羽鸽子的信息、鸽舍、家族血统关系，并支持可视化家族图谱与血统档案导出。

## 功能概览

### 🐦 我的鸽子
- 列表展示所有在养鸽子，支持按名字、脚环号搜索
- **最近查看**：顶部横向快捷入口，展示最近浏览的鸽子（最多 10 只）
- 添加新鸽子：3 步引导式表单（基本信息 → 鸽棚位置 → 家族关系）
- 编辑已有鸽子：可修改名字、脚环号、羽色、照片、鸽棚、备注及家族关系
- 鸽子详情页：展示基本信息、位置、家族关系，支持快捷跳转家族图谱
- 支持拍照或从相册选择**全身照**和**眼砂照**作为鸽子档案
- 长按列表项可删除鸽子
- **鸽子状态**：在养 / 已售 / 已故 / 赠送，不同状态以颜色标签区分

> **注意**：已有鸽子的**性别**无法修改（涉及家族关系一致性），如需更改请删除后重新添加。

### 🏠 鸽舍管理
- 添加、编辑、删除鸽舍（鸽棚）
- 鸽舍详情页查看该鸽棚下的所有鸽子列表
- 添加鸽子时动态选择所属鸽棚和笼号
- 鸽舍支持颜色标签和容量设置

### 👨‍👩‍👧 家族图谱
- **家族列表**：按"无父无母"的鸽子自动识别为家族根，分组展示各家族信息
- 支持自定义家族名称（长按家族条目）
- **家族图谱详情**：自定义 Canvas 渲染 ±3 代血缘关系，支持：
  - 双指缩放 / 单指平移
  - 点击鸽子节点跳转详情页
  - 自动居中、重置视图
  - 从配偶关系自动推断缺失的血缘节点

### 📄 血统档案导出
- 在鸽子详情页点击"导出档案"，生成包含以下内容的 **PDF 血统档案**：
  - 当前鸽子的全身照、眼砂照、基本信息
  - 鸽舍信息卡片
  - **三代血缘谱系**（父母、祖父母、曾祖父母），含照片与基本信息
- 支持 PDF 预览后通过系统分享发送

### ⚙️ 设置
- **字体大小**：标准 / 大 / 超大（切换后自动重启应用生效）
- **高对比度模式**：更适合老年用户的深色高对比主题
- **数据导出**：将完整数据（鸽子、鸽棚、家族关系、照片、位置历史、设置）打包为 ZIP 备份
- **数据导入**：从备份 ZIP 恢复全部数据，导入后建议重启应用
- **关于**：查看应用信息与项目链接

## 软件使用方法

### 首次使用
1. 安装应用后打开，进入**我的鸽子**页面
2. 点击右下角添加按钮，按向导填写鸽子信息
3. 基本信息页：填写名字、脚环号、选择性别、羽色、出生日期、状态，可拍照或选照片（全身照 + 眼砂照）
4. 鸽棚位置页：选择所属鸽棚和笼号（无鸽棚可暂不选）
5. 家族关系页：选择父亲、母亲、配偶（可选，父亲必须为雄鸽、母亲必须为雌鸽、配偶必须为异性）
6. 保存后返回列表，点击鸽子卡片查看详情

### 建立家族图谱
1. 为几只没有父母关系的鸽子（种鸽）添加基本信息
2. 为它们的后代添加信息时，在第三步"家族关系"中选择对应的父母
3. 保存后，父辈的详情页会自动显示后代列表
4. 进入**家族图谱** Tab，即可看到按家族分组的列表，点击进入查看可视化图谱

### 数据备份
1. 进入**设置** Tab
2. 点击"导出数据"，选择保存位置，备份文件将以 ZIP 格式保存
3. 需要恢复时，点击"导入数据"并选择备份 ZIP 文件，恢复完成后应用会自动重启

### 导出血统档案
1. 在**我的鸽子**列表中点击任意鸽子进入详情页
2. 点击"导出档案"按钮，等待生成
3. 预览 PDF 内容，可通过系统分享发送到微信、邮件等

## 开发环境

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
| versionName | 1.0.3 |
| versionCode | 2 |

## 开发环境配置方式

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
# 运行全部单元测试
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
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.pigeonnest.pigeonlist.PigeonListFragmentTest
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

## 技术架构

```
┌─────────────────────────────────────────┐
│           Presentation (UI)             │
│  Fragment + ViewModel + ViewBinding     │
│  Navigation Component + SafeArgs        │
├─────────────────────────────────────────┤
│              Domain Layer               │
│  UseCase + Repository Interface + Model │
├─────────────────────────────────────────┤
│               Data Layer                │
│  Room + SQLCipher  │  PhotoStorage     │
│  Repository Impl   │  BackupManager    │
│                    │  PigeonPdfGenerator│
└─────────────────────────────────────────┘
```

### 主要依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| **Hilt** | 2.50 | 依赖注入 |
| **Room** | 2.6.1 | 本地数据库 |
| **SQLCipher** | 4.5.4 | 数据库加密 |
| **Navigation** | 2.7.6 | 页面导航与 SafeArgs |
| **Glide** | 4.16.0 | 图片加载 |
| **Coroutines** | 1.7.3 | 异步编程 |
| **Material Design 3** | 1.11.0 | UI 组件 |
| **Gson** | 2.10.1 | JSON 序列化（备份导出） |
| **CircleImageView** | 3.1.0 | 圆形头像 |
| **PhotoView** | 2.3.0 | 图片缩放查看 |
| **MockK** | 1.13.9 | 单元测试 Mock |
| **Turbine** | 1.0.0 | Flow 测试 |
| **Robolectric** | 4.11.1 | JVM 环境 Android 测试 |

### 数据库结构

应用使用 **SQLCipher 加密** 的 SQLite 数据库，包含 6 张表：

| 表名 | 说明 |
|------|------|
| `pigeons` | 鸽子基本信息（名字、脚环号、性别、羽色、照片路径、眼砂照路径、状态等） |
| `lofts` | 鸽棚/鸽舍信息（名称、位置、容量、颜色标签） |
| `family_relations` | 家族关系（父、母、配偶、后代列表） |
| `location_history` | 鸽子位置变更历史（鸽棚、笼号、变更时间） |
| `pigeon_photos` | 鸽子多张照片记录（支持每只鸽子多张照片） |

### 核心设计亮点

1. **软删除机制**：鸽子和鸽舍均使用 `is_deleted` 标记，非物理删除，保留历史数据完整性
2. **性别校验**：设置父亲时必须选雄鸽，母亲必须选雌鸽，配偶必须选异性
3. **双向配偶关系**：更新配偶时自动同步双方的 `family_relations` 记录
4. **家族根推断**：家族列表自动将"无父无母"的鸽子识别为家族根节点
5. **血缘推断**：家族图谱支持从配偶关系中推断缺失的父亲或母亲
6. **全量备份恢复**：不仅备份数据库，还包括照片和 SharedPreferences，实现完整迁移
7. **适老化优先**：大字体、高对比度、大按钮、纸质化 UI，专为老年用户设计

### 隐私与安全

- 所有数据存储在应用私有目录，数据库使用 **SQLCipher** 加密
- 首次启用加密时，应用会自动检测并清理旧的未加密数据库
- 照片压缩后存储在应用内部存储，不暴露到外部相册

## 项目结构

```
app/src/main/java/com/pigeonnest/
├── PigeonNestApp.kt              # Application 入口（Hilt + SQLCipher 迁移检测）
├── data/
│   ├── file/                     # 文件存储
│   │   ├── BackupManager.kt      # ZIP 备份/恢复（含照片 + SharedPreferences）
│   │   ├── PhotoStorageManager.kt# 照片压缩、存储、加载
│   │   └── PigeonPdfGenerator.kt # PDF 血统档案生成
│   ├── local/                    # Room 数据库
│   │   ├── dao/                  # 数据访问对象
│   │   ├── database/             # Database + Migration + TypeConverter
│   │   ├── entity/               # 实体定义
│   │   └── mapper/               # 领域模型 ↔ 实体映射
│   └── repository/               # 仓库实现
├── di/                           # Hilt 依赖注入模块
├── domain/
│   ├── model/                    # 领域模型（Pigeon, Loft, FamilyRelation, Gender, PigeonStatus 等）
│   ├── repository/               # 仓库接口
│   └── usecase/                  # 用例（按模块分组）
└── presentation/
    ├── main/                     # MainActivity + 底部导航
    ├── pigeonlist/               # 鸽子列表（搜索 + 最近查看）
    ├── pigeondetail/             # 鸽子详情（家族关系 + PDF 导出）
    ├── pigeonedit/               # 添加/编辑鸽子（3 步向导）
    ├── loftmanage/               # 鸽舍列表 + 鸽舍详情
    ├── familylist/               # 家族列表（根节点分组）
    ├── familygraph/              # 家族图谱（Canvas 自定义绘制）
    ├── locationset/              # 位置变更（鸽棚/笼号选择）
    ├── pdfpreview/               # PDF 预览
    ├── settings/                 # 设置（字体/主题/备份/关于）
    └── common/                   # 通用组件（图片查看对话框）

app/src/test/java/com/pigeonnest/          # 单元测试（~165 个）
app/src/androidTest/java/com/pigeonnest/   # 仪器测试（Espresso + Hilt）
```

## 测试覆盖

项目包含完整的单元测试和仪器测试套件：

### 单元测试覆盖
- **Domain 层**：Gender、PigeonStatus、FamilyGroup、FamilyGraph 模型测试
- **UseCase 层**：Pigeon CRUD、Search、Loft、Family Relation、Lineage、Graph 等全部用例
- **Data 层**：PigeonDao、LoftDao、FamilyRelationDao、LocationHistoryDao、PigeonPhotoDao（Room 内存数据库测试）
- **Mapper 层**：PigeonMapper、LoftMapper
- **Repository 层**：PigeonRepositoryImpl、LoftRepositoryImpl、FamilyRepositoryImpl
- **Presentation 层**：全部 ViewModel 测试（PigeonList、PigeonDetail、PigeonEdit、LoftList、LoftDetail、FamilyList、FamilyGraph、LocationSet、Settings）

### 仪器测试覆盖
- **Fragment 测试**：PigeonListFragment、SettingsFragment（FragmentScenario + Hilt）
- **Activity 测试**：MainActivity 导航验证
- **E2E 测试**：PigeonCrudFlowTest（完整的增删改查流程）

### 测试技术栈
| 工具 | 用途 |
|------|------|
| JUnit 4 | 测试框架 |
| MockK | Kotlin Mock / Stub |
| Turbine | Flow 断言 |
| Room Testing | 内存数据库测试 |
| Robolectric | JVM 环境 Android 组件测试 |
| Espresso | UI 自动化测试 |
| Fragment Testing | Fragment 隔离测试 |
| Hilt Android Testing | 依赖注入测试环境 |

## 许可证

本项目为个人学习/实用项目，保留所有权利。

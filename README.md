# 放鸽子 (PigeonNest)

专为养鸽爱好者设计的 Android 应用，帮助您记录和管理每一羽鸽子的信息、鸽舍、家族血统关系，并支持可视化家族图谱。

## 功能概览

### 🐦 我的鸽子
- 列表展示所有在养鸽子，支持按名字、脚环号搜索
- 添加新鸽子：3 步引导式表单（基本信息 → 鸽棚位置 → 家族关系）
- 编辑已有鸽子：可修改名字、脚环号、羽色、照片、鸽棚、备注及家族关系
- 鸽子详情页：展示基本信息、位置、家族关系，支持快捷跳转家族图谱
- 支持拍照或从相册选择照片作为鸽子头像
- 长按列表项可删除鸽子

> **注意**：已有鸽子的**性别**无法修改（涉及家族关系一致性），如需更改请删除后重新添加。

### 🏠 鸽舍管理
- 添加、编辑、删除鸽舍（鸽棚）
- 鸽舍详情页查看该鸽棚下的所有鸽子列表
- 添加鸽子时动态选择所属鸽棚和笼号

### 👨‍👩‍👧 家族图谱
- **家族列表**：按"无父无母"的鸽子自动识别为家族根，分组展示各家族信息
- 支持自定义家族名称（长按家族条目）
- **家族图谱详情**：Canvas 渲染 ±3 代血缘关系，支持：
  - 双指缩放 / 单指平移
  - 点击鸽子节点跳转详情页
  - 自动居中、重置视图

### ⚙️ 设置
- **字体大小**：标准 / 大 / 超大（切换后自动重启应用生效）
- **高对比度模式**：更适合老年用户的深色高对比主题
- **数据导出**：将完整数据（鸽子、鸽棚、家族关系、照片）打包为 ZIP 备份到 `Documents/PigeonNest/backups`
- **数据导入**：从备份 ZIP 恢复全部数据

## 软件使用方法

### 首次使用
1. 安装应用后打开，进入**我的鸽子**页面
2. 点击右下角添加按钮，按向导填写鸽子信息
3. 基本信息页：填写名字、脚环号、选择性别、羽色、出生日期，可拍照或选照片
4. 鸽棚位置页：选择所属鸽棚和笼号（无鸽棚可暂不选）
5. 家族关系页：选择父亲、母亲、配偶（可选）
6. 保存后返回列表，点击鸽子卡片查看详情

### 建立家族图谱
1. 为几只没有父母关系的鸽子（种鸽）添加基本信息
2. 为它们的后代添加信息时，在第三步"家族关系"中选择对应的父母
3. 保存后，父辈的详情页会自动显示后代列表
4. 进入**家族图谱** Tab，即可看到按家族分组的列表，点击进入查看可视化图谱

### 数据备份
1. 进入**设置** Tab
2. 点击"导出数据"，备份文件将保存到手机的 `Documents/PigeonNest/backups` 目录
3. 需要恢复时，点击"导入数据"并选择备份 ZIP 文件

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

在 Windows (Git Bash) 或 macOS/Linux 终端中：

```bash
# 设置 JAVA_HOME（Windows 示例，使用 Android Studio 自带 JDK）
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"

# 编译 Debug 版本
./gradlew :app:assembleDebug
```

编译成功后，APK 位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

### 4. 运行单元测试

```bash
./gradlew :app:testDebugUnitTest
```

### 5. 安装到设备

确保手机已开启 USB 调试并连接电脑：

```bash
./gradlew :app:installDebug
```

## 技术架构

```
┌─────────────────────────────────────────┐
│           Presentation (UI)             │
│  Fragment + ViewModel + DataBinding     │
│  Navigation Component + SafeArgs        │
├─────────────────────────────────────────┤
│              Domain Layer               │
│  UseCase + Repository Interface + Model │
├─────────────────────────────────────────┤
│               Data Layer                │
│  Room + SQLCipher  │  PhotoStorage     │
│  Repository Impl   │  BackupManager    │
└─────────────────────────────────────────┘
```

### 主要依赖

| 依赖 | 用途 |
|------|------|
| **Hilt** (2.50) | 依赖注入 |
| **Room** (2.6.1) | 本地数据库 |
| **SQLCipher** (4.5.4) | 数据库加密 |
| **Navigation** (2.7.6) | 页面导航 |
| **Glide** (4.16.0) | 图片加载 |
| **Coroutines** (1.7.3) | 异步编程 |
| **Material Design 3** | UI 组件 |

### 数据库结构

应用使用 **SQLCipher 加密** 的 SQLite 数据库，包含 5 张表：

| 表名 | 说明 |
|------|------|
| `pigeons` | 鸽子基本信息（名字、脚环号、性别、羽色、照片路径等） |
| `lofts` | 鸽棚/鸽舍信息 |
| `family_relations` | 家族关系（父、母、配偶） |
| `location_history` | 鸽子位置变更历史 |
| `pigeon_photos` | 鸽子照片记录 |

### 隐私与安全

- 所有数据存储在应用私有目录，数据库使用 **SQLCipher** 加密
- 首次启用加密时，应用会自动检测并清理旧的未加密数据库
- 照片压缩后存储在应用内部存储，不暴露到外部相册

## 项目结构

```
app/src/main/java/com/pigeonnest/
├── PigeonNestApp.kt              # Application 入口
├── data/
│   ├── file/                     # 文件存储（照片、备份）
│   ├── local/                    # Room 数据库
│   │   ├── dao/                  # 数据访问对象
│   │   ├── database/             # Database + Migration
│   │   ├── entity/               # 实体定义
│   │   └── mapper/               # 领域模型 ↔ 实体映射
│   └── repository/               # 仓库实现
├── di/                           # Hilt 依赖注入模块
├── domain/
│   ├── model/                    # 领域模型
│   ├── repository/               # 仓库接口
│   └── usecase/                  # 用例（按模块分组）
└── presentation/
    ├── main/                     # 主 Activity + 底部导航
    ├── pigeonlist/               # 鸽子列表
    ├── pigeondetail/             # 鸽子详情
    ├── pigeonedit/               # 添加/编辑鸽子
    ├── loftmanage/               # 鸽舍管理
    ├── familylist/               # 家族列表
    ├── familygraph/              # 家族图谱
    ├── locationset/              # 位置变更
    ├── settings/                 # 设置
    └── common/                   # 通用组件
```

## 许可证

本项目为个人学习/实用项目，保留所有权利。

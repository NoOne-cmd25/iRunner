# 跑步记录APP - 项目说明文档

##  项目概述

**项目名称**：跑步记录助手  
**项目类型**：Android移动应用  
**开发模式**：原生Android开发（Java + Kotlin混合）  
**主要功能**：GPS跑步轨迹记录、运动数据分析、历史记录管理

---

##  技术架构

### 架构模式：MVC (Model-View-Controller)
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    Model    │ ←→ │ Controller  │ ←→ │     View    │
│             │    │             │    │             │
│ - 数据实体   │    │ - Activity  │    │ - XML布局   │
│ - 数据库     │    │ - Service   │    │ - 界面控件  │
│ - 定位数据   │    │ - 业务逻辑  │    │ - 地图组件  │
└─────────────┘    └─────────────┘    └─────────────┘
```

### 技术栈
- **开发语言**：Java (主要业务) + Kotlin (数据层)
- **数据库**：Room (SQLite封装)
- **地图服务**：高德地图SDK
- **架构组件**：LiveData、ViewModel
- **权限管理**：EasyPermissions
- **后台服务**：Foreground Service

---

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/myapplication/
│   │   ├── MainActivity.java          # 主运行界面
│   │   ├── HistoryActivity.java       # 历史记录界面
│   │   ├── DetailActivity.java        # 详情展示界面
│   │   ├── RunForegroundService.java  # 后台定位服务
│   │   ├── RunRecordAdapter.java      # 列表适配器
│   │   └── RunRecord.java             # Java数据模型
│   ├── kotlin/com/example/myapplication/
│   │   ├── RunRecordEntity.kt         # 数据库实体
│   │   ├── RunRecordDao.kt            # 数据访问接口
│   │   └── AppDatabase.kt             # 数据库管理
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml      # 主界面布局
│       │   ├── activity_history.xml   # 历史界面布局
│       │   ├── activity_detail.xml    # 详情界面布局
│       │   └── item_run_record.xml    # 列表项布局
│       └── AndroidManifest.xml        # 应用配置
```

---

## 核心功能模块

### 实时跑步记录模块
**文件**：`MainActivity.java`
```java
功能列表：
├──  跑步状态管理（开始/暂停/继续/结束）
├──  实时GPS定位与轨迹记录
├──  跑步计时器
├──  实时距离计算
├──  实时配速计算
├──  地图轨迹绘制
└──  前后台定位切换
```

### 2后台定位服务
**文件**：`RunForegroundService.java`
```java
功能特点：
├──  前台服务保活
├──  后台持续定位
├──  广播数据通信
├──  状态通知管理
└──  电量优化处理
```

### 3. 数据存储模块
**Kotlin文件**：
- `RunRecordEntity.kt` - 数据实体定义
- `RunRecordDao.kt` - 数据库操作接口
- `AppDatabase.kt` - 数据库实例管理

**数据模型**：
```kotlin
@Entity(tableName = "run_records")
data class RunRecordEntity(
    @PrimaryKey val recordId: String,      // 记录ID
    val userId: String,                    // 用户ID
    val startTime: Long,                   // 开始时间
    val duration: Long,                    // 跑步时长(ms)
    val totalDistance: Double,             // 总距离(米)
    val averagePace: Double,               // 平均配速
    val trackPoints: String                // 轨迹点(JSON)
)
```

### 4. 历史记录模块
**文件**：`HistoryActivity.java` + `RunRecordAdapter.java`
```java
功能组成：
├──  记录列表展示
├──  RecyclerView适配器
├──  点击跳转详情
└──  数据格式化显示
```

### 5. 详情展示模块
**文件**：`DetailActivity.java`
```java
功能详情：
├──  运动数据统计
├──  轨迹地图回放
├──  卡路里计算
└──  配速分析
```

---

##  技术实现细节

### 定位系统实现
```java
// 高德定位配置
mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
mLocationOption.setInterval(2000);  // 2秒定位间隔
mLocationOption.setOnceLocation(false);  // 持续定位

// 距离计算算法
Location.distanceBetween(prevLat, prevLng, currLat, currLng, results);
float distance = results[0];
```

### 数据持久化策略
```kotlin
// Room数据库配置
@Database(entities = [RunRecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runRecordDao(): RunRecordDao
}
```

### 前后台通信机制
```java
// 广播通信
Intent i = new Intent("RUN_LOCATION");
i.putExtra("lat", location.getLatitude());
LocalBroadcastManager.getInstance(this).sendBroadcast(i);
```

---

##  配置要求

### 开发环境
- **Android Studio**：Arctic Fox 以上版本
- **Gradle**：7.0+
- **Android SDK**：API 33

### 设备要求
- **最小SDK**：21 (Android 5.0)
- **目标SDK**：33 (Android 13)
- **必需功能**：GPS定位支持

### 第三方服务
- **高德地图**：需要API Key
- **权限管理**：EasyPermissions库

---

## 编译与运行

### 1. 环境配置
```bash
# 确保已安装：
# - Android Studio
# - Android SDK 33
# - Java 8/11
```

### 2. 依赖安装
```bash
# 同步Gradle依赖
./gradlew build
```

### 3. 配置API Key
在 `AndroidManifest.xml` 中配置高德地图API Key：
```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="你的API_KEY" />
```

### 4. 编译运行
```bash
# 调试版本
./gradlew assembleDebug

# 发布版本  
./gradlew assembleRelease
```

---

##  权限说明

### 必需权限
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```

### 权限使用说明
- **定位权限**：仅用于跑步轨迹记录
- **网络权限**：地图数据加载和定位服务
- **前台服务**：保证后台持续定位

---

##  数据流说明

### 跑步记录流程
```
用户操作 → MainActivity → 定位服务 → 数据计算 → 数据库存储
    ↓           ↓           ↓           ↓           ↓
开始跑步 → 启动定位 → 收集坐标 → 计算距离 → 保存记录
```

### 数据展示流程  
```
数据库查询 → 列表适配器 → 历史界面 → 详情界面
    ↓           ↓           ↓         ↓
获取记录 → 绑定数据 → 显示列表 → 展示详情
```

---

##  常见问题解决

### 1. 定位权限问题
**症状**：无法获取定位，距离不更新
**解决**：检查权限是否授予，GPS是否开启

### 2. 后台距离不准确
**症状**：APP在后台时距离增加缓慢
**解决**：确保授予后台定位权限，检查省电设置

### 3. 地图显示问题
**症状**：地图空白或加载失败
**解决**：检查网络连接，验证高德API Key

### 4. 数据库操作失败
**症状**：历史记录无法保存或读取
**解决**：检查Room数据库配置，验证实体类定义

##  版本更新记录

### v1.0.0 - 基础版本
- ✅ 实时跑步轨迹记录
- ✅ 运动数据统计
- ✅ 历史记录管理
- ✅ 详情页面展示

### 后续规划
- 🔄 社交分享功能
- 🔄 运动目标设定
- 🔄 数据导出功能
- 🔄 语音播报指导

---


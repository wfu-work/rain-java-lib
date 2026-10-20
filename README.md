# Rain Java SDK

面向 Java / Spring Boot 的降雨预测 SDK，通过 JNA 调用 `libRainpredict`，提供事后预测与实时预测接口，输出未来 **1、12、24 小时累计降雨量**。

![Java](https://img.shields.io/badge/Java-20%2B-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-brightgreen)
![JNA](https://img.shields.io/badge/JNA-5.12.1-blue)

[功能特性](#功能特性) · [快速开始](#快速开始) · [实时预测](#实时预测) · [事后预测](#事后预测) · [接口说明](#接口说明) · [运行环境](#运行环境)

## 功能特性

- **事后预测**：输入单站 24 条连续逐小时气象观测及 PWV，返回降雨预测。
- **实时预测**：输入 RTCM 与六要素两路原始 TCP 地址，原生库内部完成采集、PWV 解算、小时窗口维护和自动预测。
- **小时数据持久化**：按站点保存最近 24 条小时观测，重启后恢复仍有效的连续窗口。
- **定量与预警结果**：返回 1 / 12 / 24 小时累计雨量，12 / 24 小时附带独立预警结果。
- **Spring Boot 集成**：自动注册 `RainLibService`，也支持普通 Java 工程直接创建 Service。
- **统一异常与内存管理**：保留原生错误码，Service 自动释放每次调用返回的原生响应。

## 快速开始

### 1. 构建并安装

需要 JDK 20+、Maven 3.6.3+：

```bash
mvn clean verify
mvn install
```

构建产物为普通 JAR：`target/rain-java-lib-1.0.0.jar`。安装到本地 Maven 仓库后，在业务工程添加依赖：

```xml
<dependency>
    <groupId>com.navfirst</groupId>
    <artifactId>rain-java-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

请先安装到本地 Maven 仓库，或发布到团队 Maven 仓库后使用上述坐标。

### 2. 获取 Service

Spring Boot 3 工程可通过构造器注入：

```java
import com.navfirst.rain.lib.services.RainLibService;
import org.springframework.stereotype.Service;

@Service
public class RainService {
    private final RainLibService rainLibService;

    public RainService(RainLibService rainLibService) {
        this.rainLibService = rainLibService;
    }
}
```

如果业务工程已提供 `RainLibService` Bean，自动配置使用业务提供的实例。普通 Java 工程可以直接创建：

```java
import com.navfirst.rain.lib.services.RainLibService;
import com.navfirst.rain.lib.services.impl.RainLibServiceImpl;

RainLibService rainLibService = new RainLibServiceImpl();
```

### 3. 配置预测实例

模型目录、Python 解释器及其依赖需要单独部署。初始化时，原生库会实际加载模型并检查运行环境。

```java
import com.navfirst.rain.lib.domains.RainConfig;

RainConfig config = RainConfig.builder()
        .modelDir("/opt/rain/models")
        .pythonExecutable("/opt/rain/runtime/bin/python")
        .workDir("/opt/rain/work")
        .build();

long handle = rainLibService.create(config);
try {
    // 使用同一 handle 调用事后预测或实时预测接口。
} finally {
    rainLibService.close(handle);
}
```

| 配置 | 必填 | 默认值 / 说明 |
| --- | --- | --- |
| `modelDir` | 是 | 模型资源目录 |
| `pythonExecutable` | 否 | PATH 中的 `python3` |
| `workDir` | 否 | 系统临时目录；生产环境建议显式设置 |
| `device` | 否 | `cpu`，也支持 `cuda:<index>` |
| `timeoutSeconds` | 否 | 120，范围 1～3600，包含预测排队时间 |
| `threads` | 否 | 1，范围 1～64 |

## 实时预测

实时模式目前支持 **Linux ARM64 / glibc**。可先通过 `getVersionInfo().getRealtimeSupported()` 判断动态库是否包含当前平台的实时程序；这个字段不代表数据已经就绪。

以下示例在已创建的 `handle` 上启动实时任务，并查询一次结果。业务工程可根据需要定时调用状态和结果接口。

```java
import com.navfirst.rain.lib.domains.RainPrediction;
import com.navfirst.rain.lib.domains.RainRealtimeConfig;
import com.navfirst.rain.lib.domains.RainRealtimeStatus;
import com.navfirst.rain.lib.exceptions.RainLibErrorCode;
import com.navfirst.rain.lib.exceptions.RainLibException;

RainRealtimeConfig realtimeConfig = RainRealtimeConfig.builder()
        .sncode("站点-001")
        .rtcmAddress("192.168.1.10:9001")
        .weatherAddress("192.168.1.11:9002")
        .weatherMode(2)
        .dataDir("/opt/rain/data")
        .build();

RainRealtimeStatus started = rainLibService.startRealtime(handle, realtimeConfig);
System.out.println("小时数据文件：" + started.getHourlyDataFile());

RainRealtimeStatus status = rainLibService.getRealtimeStatus(handle);
System.out.printf("状态：%s，小时进度：%d/%d%n",
        status.getState(), status.getHoursCollected(), status.getRequiredHours());
if (status.getLastError() != null) {
    System.out.println(status.getLastError().getCode() + ": " + status.getLastError().getMessage());
}

try {
    RainPrediction prediction = rainLibService.predictRealtime(handle);
    System.out.println(prediction);
} catch (RainLibException e) {
    if (!RainLibErrorCode.NOT_READY.name().equals(e.getCode())) {
        throw e;
    }
    // 小时数据不足或当前小时的预测尚不可用，稍后再次查询。
}

// 需要暂停实时任务时调用；以后也可以使用同一 handle 再次启动。
// rainLibService.stopRealtime(handle);
```

### 输入配置

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `sncode` | 是 | 站点编码，1～256 个 UTF-8 字节，用于关联结果和持久文件 |
| `rtcmAddress` | 是 | 原始 RTCM 数据服务地址，`host:port` |
| `weatherAddress` | 是 | 六要素原始数据服务地址，`host:port` |
| `weatherMode` | 否 | 默认 2；支持原生程序的 1 / 2 / 3 协议模式 |
| `dataDir` | 否 | 持久目录，默认 `workDir/realtime` |

地址支持 IPv4 或 ASCII 域名，最长 90 字节，不带 `tcp://` 前缀。模式 1 接收 `$QX` 主动上报数据；模式 2 / 3 沿用原生程序的采集命令，TCP 服务需要支持双向通信，并与所选协议一致。

### 状态与结果

| `state` | 含义 |
| --- | --- |
| `waiting_data` | 正在等待连接或有效气象 / PWV 数据 |
| `warming_up` | 正在积累连续小时数据 |
| `predicting` | 正在执行预测 |
| `ready` | 当前小时已有成功预测结果 |

`RainRealtimeStatus` 同时包含 `running`、`pwvConnected`、`hoursCollected`、`requiredHours`、`lastSampleTime`、`lastHourTime`、`hourlyDataFile`、`latestPrediction` 和 `lastError`。

`running` 表示后台任务已启动；`pwvConnected` 表示已连接原生输出。后台错误通过 `lastError` 返回，不会直接导致状态查询抛出异常。`latestPrediction` 保留最近成功结果，可能已过期；读取当前小时结果请使用 `predictRealtime(handle)`，不可用时抛出 `NOT_READY`。

### 小时数据持久化

原生库每小时取该小时最后一条有效观测，以结束整点标记；连续积满 24 条后自动预测。首次启动且没有可恢复数据时，需要先积累窗口。

- 文件名为 `SHA256(sncode).hourly.json`，每个站点保存最近 24 条已完成的小时观测。
- 每次小时更新后同步落盘并原子替换，停止任务和关闭实例均保留文件。
- 使用相同 `dataDir` 和 `sncode` 再次启动时恢复数据；窗口仍连续且结束于当前整点，满 24 条即可重新预测。
- 缺小时不插值、不补零；当前小时尚未完成的原始采样不持久化。
- 文件损坏返回 `IO_ERROR` 并保留原文件；运行时写入失败通过 `lastError` 报告，停止 / 关闭时重试，仍失败则返回异常。

生产环境请将 `dataDir` 配置到持久磁盘目录。原生程序固定使用 2024、2101、2026 端口，同一主机仅支持一条内置实时链路。

## 事后预测

调用方提供同一站点的 24 条真实逐小时观测，包含气象变量和已计算的 PWV：

```java
import com.navfirst.rain.lib.domains.RainRequest;

// observations 为 List<RainObservation>，按时间升序排列的24条实际观测。
RainRequest request = RainRequest.builder()
        .sncode("站点-001")
        .observations(observations)
        .build();

RainPrediction prediction = rainLibService.predict(handle, request);
```

| `RainObservation` 字段 | 单位与要求 |
| --- | --- |
| `time` | Unix 毫秒，相邻记录严格间隔 3600000 毫秒 |
| `temperatureC` | 摄氏度，-100～70 |
| `humidityPercent` | 百分数，0～100 |
| `pressureHPa` | hPa，300～1100 |
| `windSpeedMps` | m/s，0～150 |
| `windDirectionDeg` | 气象风来向，0～360 度 |
| `pwvMm` | 可降水量，mm，0～300，必填 |
| `rainfallMm` | 实测降雨，mm，非负，可选；当前模型不使用 |

数值使用包装类型区分缺失值与零值。参数校验由原生库执行；缺字段、非法数值或时间缺口会报错。

## 预测输出

实时和事后预测均返回 `RainPrediction`：

| 字段 | 说明 |
| --- | --- |
| `sncode` | 站点编码 |
| `issueTime` | 起报时刻，窗口最后一条小时观测的 Unix 毫秒时间戳 |
| `modelVersion` | 模型资源内容指纹 |
| `forecasts` | 1 / 12 / 24 小时的预测列表 |

每个 `RainForecast` 包含 `hours`、`validTime` 和累计降雨量 `rainfallMm`。12 / 24 小时还包含 `alertLevel`、`alertName`、`alertEvidenceMm`；1 小时预测的预警字段为空。预警等级来自独立预警轨，预警佐证雨量不代替定量预报值。

## 接口说明

| 方法 | 用途 |
| --- | --- |
| `getVersion()` | 查询动态库版本号 |
| `getVersionInfo()` | 查询版本、API 版本、平台和实时能力 |
| `create(config)` | 创建实例，返回非零 64 位句柄 |
| `predict(handle, request)` | 事后预测 |
| `startRealtime(handle, config)` | 启动实时采集、窗口恢复和自动预测 |
| `getRealtimeStatus(handle)` | 查询实时状态和持久文件路径 |
| `predictRealtime(handle)` | 读取当前小时已完成的预测 |
| `stopRealtime(handle)` | 停止实时任务，保留小时数据 |
| `close(handle)` | 停止任务并释放实例，保留小时数据 |

调用方负责复用和关闭句柄。同一实例的预测串行执行，不同实例可以并行；`close` 会取消排队和正在执行的预测并等待退出。`stopRealtime` 对有效句柄可重复调用；关闭后的句柄及重复关闭返回 `INVALID_HANDLE`。

## 异常处理

调用失败抛出 `RainLibException`，`getCode()` 保留原生字符串错误码，`getMessage()` / `getMsg()` 返回错误消息。错误码枚举见 [RainLibErrorCode](src/main/java/com/navfirst/rain/lib/exceptions/RainLibErrorCode.java)。

| 错误码 | 常见原因 |
| --- | --- |
| `NOT_READY` | 连续小时数据不足，或当前小时结果尚不可用 |
| `INVALID_ARGUMENT` | 配置或预测输入无效，实时任务重复启动 / 尚未启动 |
| `INVALID_HANDLE` / `CLOSED` | 句柄不存在或实例已关闭 |
| `ENVIRONMENT_ERROR` | 平台不支持、程序无法启动或本机端口被占用 |
| `MODEL_ERROR` / `PYTHON_ERROR` | 模型资源或 Python 执行失败 |
| `IO_ERROR` | 数据文件读写或流读取失败 |
| `TIMEOUT` / `CANCELED` | 请求超时或已取消 |
| `PROTOCOL_ERROR` / `PREDICTION_ERROR` / `INTERNAL_ERROR` | 响应协议、预测或内部错误 |

Service 统一通过 `JsonUtils` 读取响应，并在成功和异常路径释放原生内存。直接使用底层 `RainLibrary` 时，需自行在 `finally` 中调用一次 `rainFree(pointer)`。

## 运行环境

JAR 按 JNA 标准资源路径携带原生库：

| 平台 | 资源路径 | 事后预测 | 实时模式 |
| --- | --- | --- | --- |
| Linux ARM64 | `linux-aarch64/libRainpredict.so` | 支持 | 包含实时程序 |
| Linux AMD64 | `linux-x86-64/libRainpredict.so` | 支持 | 暂不支持 |
| macOS ARM64 | `darwin-aarch64/libRainpredict.dylib` | 支持 | 暂不支持 |

JVM 与原生库架构必须匹配；Linux 版本需要兼容的 glibc 环境。也可通过 `-Djna.library.path=/opt/rain/lib` 加载外部目录中的库，外部库须包含当前 SDK 对应的实时导出接口。

模型权重、Python 解释器及 NumPy / PyTorch / LightGBM 不包含在 JAR 内。实时模式的 `stream_c`、`pwv_c`、`pwv_f` 及解算资源已嵌入 Linux ARM64 库，运行时释放到工作目录，该目录需要允许执行程序并容纳约 60 MB 的资源。

原生程序 TCP 输入兼容性、真实 RTCM 解算及现场 PWV 收敛需进行现场联调；构建与接口验证不代表已完成连续 24 小时在线验证。预测适用区域和精度沿用原模型。

## 项目结构

```text
src/main/java/com/navfirst/rain/lib/
├── configurations/   Spring Boot 自动配置
├── domains/          初始化配置、气象观测、实时状态及预测结果
├── exceptions/       统一异常和错误码
├── library/          JNA 原生接口
├── services/         对外 Service 及实现
└── utils/            JSON 响应解析和工具方法
src/main/resources/
├── META-INF/spring/  自动配置注册
├── linux-aarch64/    Linux ARM64 原生库
├── linux-x86-64/     Linux AMD64 原生库
└── darwin-aarch64/   macOS ARM64 原生库
```

package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时采集与预测状态；可通过getRealtimeStatus主动查询。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainRealtimeStatus {

    private String sncode;

    /** 实时后台任务是否已启动，不表示数据已经就绪。 */
    private Boolean running;

    /** waiting_data、warming_up、predicting或ready。 */
    private String state;

    /** 是否已连接原生QX_DATA输出，不表示上游数据或PWV解算已有效。 */
    private Boolean pwvConnected;

    private Integer hoursCollected;

    /** 当前模型需要24条连续小时数据。 */
    private Integer requiredHours;

    /** 最近有效原始采样时间，Unix毫秒；尚无数据时为空。 */
    private Long lastSampleTime;

    /** 小时窗口的结束整点，Unix毫秒；窗口为空时此字段为空。 */
    private Long lastHourTime;

    /** 原生库使用的小时数据文件绝对路径。 */
    private String hourlyDataFile;

    /** 最近成功结果，可能已过期；调用方须检查issueTime。 */
    private RainPrediction latestPrediction;

    /** 后台采集、预测或持久化错误；存在此字段不会导致状态查询抛出异常。 */
    private RainError lastError;
}

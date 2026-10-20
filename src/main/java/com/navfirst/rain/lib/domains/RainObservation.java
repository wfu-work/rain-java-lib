package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 逐小时气象观测及PWV，除rainfallMm外均必填；使用包装类型区分缺失值与零值。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainObservation {

    /**
     * Unix毫秒时间戳。
     */
    private Long time;

    /**
     * 温度，摄氏度，-100..70。
     */
    private Double temperatureC;

    /**
     * 相对湿度，百分数，0..100。
     */
    private Double humidityPercent;

    /**
     * 气压，hPa，300..1100。
     */
    private Double pressureHPa;

    /**
     * 风速，m/s，0..150。
     */
    private Double windSpeedMps;

    /**
     * 气象风来向，度，0..360。
     */
    private Double windDirectionDeg;

    /**
     * 可降水量，mm，0..300，由上游提供。
     */
    private Double pwvMm;

    /**
     * 可选实测降雨，mm，非负；当前模型不使用该字段。
     */
    private Double rainfallMm;
}

package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未来指定时长的累计降雨预测；1小时预测的三个alert字段为空。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainForecast {

    /**
     * 预测时长，1、12或24小时。
     */
    private Integer hours;

    /**
     * 有效时刻，Unix毫秒。
     */
    private Long validTime;

    /**
     * 未来hours小时累计降雨，mm。
     */
    private Double rainfallMm;

    /**
     * 12/24小时独立预警等级，0..4。
     */
    private Integer alertLevel;

    private String alertName;

    /**
     * 预警佐证雨量，mm，不代替常规定量预报值。
     */
    private Double alertEvidenceMm;
}

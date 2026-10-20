package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 同一站点的1/12/24小时累计降雨预测结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainPrediction {

    private String sncode;

    /**
     * 起报时刻，为最后一条观测的Unix毫秒时间戳。
     */
    private Long issueTime;

    private String modelVersion;

    private List<RainForecast> forecasts;
}

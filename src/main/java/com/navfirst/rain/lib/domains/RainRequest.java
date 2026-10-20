package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单站预测请求，observations必须恰好24条，按时间升序且相邻间隔一小时。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainRequest {

    /**
     * 站点编码，1..256个UTF-8字节。
     */
    private String sncode;

    private List<RainObservation> observations;
}

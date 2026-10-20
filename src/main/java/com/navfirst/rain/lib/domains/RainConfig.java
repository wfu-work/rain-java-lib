package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预测实例配置，只有modelDir必填；其余空值使用动态库默认配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainConfig {

    private String modelDir;

    private String pythonExecutable;

    private String workDir;

    /**
     * cpu或cuda:<index>，默认cpu。
     */
    private String device;

    /**
     * 包含排队时间的超时秒数，1..3600，默认120。
     */
    private Integer timeoutSeconds;

    /**
     * Python计算线程数，1..64，默认1。
     */
    private Integer threads;
}

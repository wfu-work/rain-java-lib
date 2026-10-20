package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 动态库版本及平台能力，由rainVersion返回。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainVersionInfo {

    private String version;

    private Integer apiVersion;

    private String os;

    private String arch;

    /** 仅表示库包含此平台的实时解算程序，不表示TCP连接或PWV已就绪。 */
    private Boolean realtimeSupported;
}

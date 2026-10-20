package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实时采集配置，sncode和两路TCP地址必填；原生实时解算目前支持Linux ARM64。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainRealtimeConfig {

    /** 站点编码，1..256个UTF-8字节，也是持久数据的站点标识。 */
    private String sncode;

    /** 原始RTCM数据服务地址，host:port，IPv4或ASCII域名，最长90字节。 */
    private String rtcmAddress;

    /** 六要素原始数据服务地址，host:port；轮询协议需要双向TCP通信。 */
    private String weatherAddress;

    /** 沿用原生程序的协议模式1/2/3，省略或0使用默认值2。 */
    private Integer weatherMode;

    /** 小时数据持久目录，默认workDir/realtime，停止或关闭实例后保留。 */
    private String dataDir;
}

package com.navfirst.rain.lib.services;

import com.navfirst.rain.lib.domains.RainConfig;
import com.navfirst.rain.lib.domains.RainPrediction;
import com.navfirst.rain.lib.domains.RainRequest;
import com.navfirst.rain.lib.domains.RainRealtimeConfig;
import com.navfirst.rain.lib.domains.RainRealtimeStatus;
import com.navfirst.rain.lib.domains.RainVersionInfo;

/**
 * 创建：馥溪凝
 * 日期：2026/10/19 15:39
 * 描述：com.navfirst.rain.lib.services
 */
public interface RainLibService {

    /** 返回动态库版本号。 */
    String getVersion();

    /** 返回动态库版本、平台及是否包含实时解算程序，不代表实时数据已就绪。 */
    RainVersionInfo getVersionInfo();

    /** 创建预测实例，成功后返回非零64位句柄；使用完成后必须调用close。 */
    long create(RainConfig config);

    /** 输入同一站点24条连续逐小时气象观测及PWV，返回1/12/24小时降雨预测。 */
    RainPrediction predict(long handle, RainRequest request);

    /** 启动两路原始TCP数据采集和小时预测，恢复dataDir中的有效小时窗口。 */
    RainRealtimeStatus startRealtime(long handle, RainRealtimeConfig config);

    /** 查询采集状态、窗口进度、持久文件路径及最近错误。 */
    RainRealtimeStatus getRealtimeStatus(long handle);

    /** 读取当前小时已完成的实时预测；尚未就绪或结果过期时抛出NOT_READY。 */
    RainPrediction predictRealtime(long handle);

    /** 停止实时任务并等待退出，保留小时数据文件；同一有效句柄可重复停止。 */
    void stopRealtime(long handle);

    /** 停止实时任务、取消预测并释放实例资源，保留小时数据文件；重复关闭返回INVALID_HANDLE。 */
    void close(long handle);

}

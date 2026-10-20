package com.navfirst.rain.lib.services.impl;

import com.google.gson.JsonElement;
import com.navfirst.rain.lib.exceptions.RainLibErrorCode;
import com.navfirst.rain.lib.exceptions.RainLibException;
import com.navfirst.rain.lib.library.RainLibrary;
import com.navfirst.rain.lib.domains.RainConfig;
import com.navfirst.rain.lib.domains.RainPrediction;
import com.navfirst.rain.lib.domains.RainRequest;
import com.navfirst.rain.lib.domains.RainRealtimeConfig;
import com.navfirst.rain.lib.domains.RainRealtimeStatus;
import com.navfirst.rain.lib.domains.RainVersionInfo;
import com.navfirst.rain.lib.services.RainLibService;
import com.navfirst.rain.lib.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 创建：馥溪凝
 * 日期：2026/10/19 15:40
 * 描述：com.navfirst.rain.lib.services.impl
 */
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RainLibServiceImpl implements RainLibService {

    private final RainLibrary rainLibrary = RainLibrary.INSTANCE;

    @Override
    public String getVersion() {
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainVersion(),
                data -> JsonUtils.requiredString(data, "version"));
    }

    @Override
    public RainVersionInfo getVersionInfo() {
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainVersion(), RainVersionInfo.class);
    }

    @Override
    public long create(RainConfig config) {
        String configJson = JsonUtils.toJson(config, "config");
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainCreate(configJson), data -> {
            JsonElement value = data.get("handle");
            if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "native response is missing handle");
            }
            long handle = value.getAsLong();
            if (handle == 0L) {
                throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "native response contains a zero handle");
            }
            return handle;
        });
    }

    @Override
    public RainPrediction predict(long handle, RainRequest request) {
        String requestJson = JsonUtils.toJson(request, "request");
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainPredict(handle, requestJson),
                RainPrediction.class);
    }

    @Override
    public RainRealtimeStatus startRealtime(long handle, RainRealtimeConfig config) {
        String configJson = JsonUtils.toJson(config, "realtime config");
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainStartRealtime(handle, configJson),
                RainRealtimeStatus.class);
    }

    @Override
    public RainRealtimeStatus getRealtimeStatus(long handle) {
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainRealtimeStatus(handle),
                RainRealtimeStatus.class);
    }

    @Override
    public RainPrediction predictRealtime(long handle) {
        return JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainPredictRealtime(handle),
                RainPrediction.class);
    }

    @Override
    public void stopRealtime(long handle) {
        JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainStopRealtime(handle), data -> null);
    }

    @Override
    public void close(long handle) {
        JsonUtils.readResult(this.rainLibrary, this.rainLibrary.rainClose(handle), data -> null);
    }

}

package com.navfirst.rain.lib.library;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 创建：馥溪凝
 * 日期：2022/04/09 15:23
 * 描述：com.navfirst.dmonitor.lib.library
 */
public interface RainLibrary extends Library {

    String os = System.getProperty("os.name");
    boolean isWindowOs = os != null && os.startsWith("Windows");
    boolean isMacOs = os != null && os.startsWith("Mac");

    String RAIN_NAME = "Rainpredict";

    RainLibrary INSTANCE = Native.load(RAIN_NAME, RainLibrary.class,
            Map.of(Library.OPTION_STRING_ENCODING, StandardCharsets.UTF_8.name()));

    RainLibrary INSTANTCES = (RainLibrary) Native.synchronizedLibrary(
            INSTANCE
    );

    Pointer rainVersion();

    Pointer rainCreate(String configJson);

    Pointer rainPredict(long handle, String requestJson);

    Pointer rainStartRealtime(long handle, String configJson);

    Pointer rainRealtimeStatus(long handle);

    Pointer rainPredictRealtime(long handle);

    Pointer rainStopRealtime(long handle);

    Pointer rainClose(long handle);

    void rainFree(Pointer pointer);

}

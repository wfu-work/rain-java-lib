package com.navfirst.rain.lib.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.navfirst.rain.lib.exceptions.RainLibErrorCode;
import com.navfirst.rain.lib.exceptions.RainLibException;
import com.navfirst.rain.lib.library.RainLibrary;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 创建：馥溪凝
 * 日期：2026/10/19 17:11
 * 描述：com.navfirst.rain.lib.utils
 */
public final class JsonUtils {

    private static final Gson GSON = new Gson();

    private JsonUtils() {
    }

    /** 序列化SDK请求，拒绝空对象及NaN、Infinity等非法JSON数值。 */
    public static String toJson(Object value, String name) {
        if (value == null) {
            throw new RainLibException(RainLibErrorCode.INVALID_ARGUMENT, name + " must not be null");
        }
        try {
            return GSON.toJson(value);
        } catch (IllegalArgumentException e) {
            throw new RainLibException(RainLibErrorCode.INVALID_ARGUMENT, "cannot encode " + name + " JSON", e);
        }
    }

    /** 将原生响应的data对象转换为指定类型，读取结束后释放响应内存。 */
    public static <T> T readResult(RainLibrary rainLibrary, Pointer pointer, Class<T> type) {
        return readResult(rainLibrary, pointer, data -> {
            if (type == null) {
                throw new RainLibException(RainLibErrorCode.INVALID_ARGUMENT, "response type must not be null");
            }
            return GSON.fromJson(data, type);
        });
    }

    /**
     * 校验原生响应并读取data，原生错误保留其code和message。
     * rainLibrary必须是分配该pointer的库；成功、解析失败或reader抛出异常时均释放一次内存。
     */
    public static <T> T readResult(RainLibrary rainLibrary, Pointer pointer, Function<JsonObject, T> reader) {
        if (rainLibrary == null) {
            throw new RainLibException(RainLibErrorCode.INVALID_ARGUMENT, "rainLibrary must not be null");
        }
        if (pointer == null) {
            throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "native response pointer is null");
        }
        try {
            if (reader == null) {
                throw new RainLibException(RainLibErrorCode.INVALID_ARGUMENT, "response reader must not be null");
            }
            String responseJson = pointer.getString(0, StandardCharsets.UTF_8.name());
            JsonObject response = JsonParser.parseString(responseJson).getAsJsonObject();
            JsonElement ok = response.get("ok");
            if (ok == null || !ok.isJsonPrimitive() || !ok.getAsJsonPrimitive().isBoolean()) {
                throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "native response is missing ok");
            }
            if (!ok.getAsBoolean()) {
                JsonObject error = response.getAsJsonObject("error");
                throw new RainLibException(requiredString(error, "code"), requiredString(error, "message"));
            }
            JsonElement error = response.get("error");
            JsonElement data = response.get("data");
            if ((error != null && !error.isJsonNull()) || data == null || !data.isJsonObject()) {
                throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "native success response is incomplete");
            }
            return reader.apply(data.getAsJsonObject());
        } catch (RainLibException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "invalid native response", e);
        } finally {
            rainLibrary.rainFree(pointer);
        }
    }

    /** 读取响应中的必填非空字符串，缺失、null或类型错误均转换为协议错误。 */
    public static String requiredString(JsonObject object, String name) {
        JsonElement value = object == null ? null : object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
                || value.getAsString().isEmpty()) {
            throw new RainLibException(RainLibErrorCode.PROTOCOL_ERROR, "native response is missing " + name);
        }
        return value.getAsString();
    }

}

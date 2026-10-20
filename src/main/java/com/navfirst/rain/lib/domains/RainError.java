package com.navfirst.rain.lib.domains;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 原生后台任务错误，对应状态响应中的lastError。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RainError {

    private String code;

    private String message;
}

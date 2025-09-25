package com.mazen.wfm.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse")
public class ErrorResponse {
    private boolean success = false;
    private String message;
    private Object data = new Object();
    @Schema(type = "string", format = "date-time", example = "2025-09-22T13:47:15.123")
    private String timestamp;
}

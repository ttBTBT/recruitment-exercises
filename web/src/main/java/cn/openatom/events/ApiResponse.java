package cn.openatom.events;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(String message, T data) { return new ApiResponse<>(0, message, data); }
    public static ApiResponse<Void> error(int code, String message) { return new ApiResponse<>(code, message, null); }
}

package cn.openatom.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> business(BusinessException e) { return failure(e.getStatus(), e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(x -> x.getField() + ": " + x.getDefaultMessage()).orElse("参数不合法");
        return failure(400, message);
    }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiResponse<Void>> badInput(Exception e) { return failure(400, "参数或 JSON 格式不合法"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> conflict(Exception e) { return failure(409, "数据冲突：用户名或报名记录已存在"); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        LOG.error("Unhandled request failure", e);
        return failure(500, "服务器暂时无法处理，请稍后重试");
    }
    private ResponseEntity<ApiResponse<Void>> failure(int status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(status, message));
    }
}

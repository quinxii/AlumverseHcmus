package hcmus.alumni.news.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import hcmus.alumni.news.exception.AppException;
import hcmus.alumni.news.exception.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        System.out.println(e.getMessage());
        ErrorResponse error = new ErrorResponse(500, "Lỗi hệ thống, vui lòng thử lại sau");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(403, "Quyền truy cập bị từ chối");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        ErrorResponse error = new ErrorResponse(40000, "Tham số " + name + " bị thiếu");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingParts(MissingServletRequestPartException ex) {
        String name = ex.getRequestPartName();
        ErrorResponse error = new ErrorResponse(40001, "File " + name + " bị thiếu");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ErrorResponse error = new ErrorResponse(40002, "Dung lượng file không được vượt quá " + maxFileSize);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchParams(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String requiredType = ex.getRequiredType().getSimpleName();
        ErrorResponse error = new ErrorResponse(40003,
                "Tham số " + name + " phải là " + requiredType);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        ErrorResponse error = new ErrorResponse(ex.getCode(), ex.getMessage());

        return new ResponseEntity<>(error, ex.getHttpStatusCode());
    }
}

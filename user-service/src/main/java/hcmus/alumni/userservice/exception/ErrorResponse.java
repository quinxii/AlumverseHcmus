package hcmus.alumni.userservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail {
        private int code;
        private String message;
    }

    private ErrorDetail error;

    public ErrorResponse(int code, String message) {
        this.error = new ErrorDetail(code, message);
    }
}

package hcmus.alumni.halloffame.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	@JsonIgnore
	private HttpStatus httpStatusCode;

	public AppException(int code, String message, HttpStatus httpStatusCode) {
		super(message);
		this.code = code;
		this.httpStatusCode = httpStatusCode;
	}
}

package com.movie.theatre.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 5149415867769345440L;

	public UnauthorizedException(String message) {
        super(message);
    }
}


package digital.container.exception;

import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import io.gumga.presentation.validation.FieldErrorResource;
import org.springframework.http.HttpStatus;

import java.util.List;

public class FileNotFoundException extends GumgaRunTimeException {
    public FileNotFoundException(HttpStatus httpStatus) {
        super(httpStatus);
    }

    public FileNotFoundException(HttpStatus httpStatus, List<FieldErrorResource> fieldErrors) {
        super(httpStatus, fieldErrors);
    }

    public FileNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}

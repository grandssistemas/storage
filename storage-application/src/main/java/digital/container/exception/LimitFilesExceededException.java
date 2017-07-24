package digital.container.exception;

import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import io.gumga.presentation.validation.FieldErrorResource;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Created by gumgait on 24/07/17.
 */
public class LimitFilesExceededException extends GumgaRunTimeException {

    public LimitFilesExceededException(HttpStatus httpStatus) {
        super(httpStatus);
    }

    public LimitFilesExceededException(HttpStatus httpStatus, List<FieldErrorResource> fieldErrors) {
        super(httpStatus, fieldErrors);
    }

    public LimitFilesExceededException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}

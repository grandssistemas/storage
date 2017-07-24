package digital.container.exception;


import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import io.gumga.presentation.validation.FieldErrorResource;
import org.springframework.http.HttpStatus;

import java.util.List;

public class KeyWasNotRegisteredInStorageYetException extends GumgaRunTimeException {
    public KeyWasNotRegisteredInStorageYetException(HttpStatus httpStatus) {
        super(httpStatus);
    }

    public KeyWasNotRegisteredInStorageYetException(HttpStatus httpStatus, List<FieldErrorResource> fieldErrors) {
        super(httpStatus, fieldErrors);
    }

    public KeyWasNotRegisteredInStorageYetException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}

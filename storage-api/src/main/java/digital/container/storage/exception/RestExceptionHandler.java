package digital.container.storage.exception;

import digital.container.exception.StorageError;
import digital.container.exception.StorageException;
import digital.container.service.cause_problem.CauseProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);
    @Autowired
    private CauseProblemService causeProblemService;

    @ResponseBody
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<StorageError> handleStorageException(StorageException ex) {
        StorageError error = ex.getStorageError();
        String message = error.getCodeError().concat(" - ").concat(error.getMessage());
        LOGGER.error(error.getCodeError().concat(" - ").concat(error.getMessage()), ex);
        this.causeProblemService.create(message);
        return ResponseEntity.status(error.getStatus()).body(error);
    }
}

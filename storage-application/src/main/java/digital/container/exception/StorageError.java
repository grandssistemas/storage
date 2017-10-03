package digital.container.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

public class StorageError implements Serializable {
    private String codeError;
    private String message;
    private String details;
    private HttpStatus status;

    private StorageError() {
    }

    private StorageError(String codeError, String message, String details, HttpStatus status) {
        this.codeError = codeError;
        this.message = message;
        this.details = details;
        this.status = status;
    }

    public String getCodeError() {
        return codeError;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    @JsonIgnore
    public HttpStatus getStatus() {
        return status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String codeError;
        private String message;
        private String details;
        private HttpStatus status;


        public Builder codeError(Long codeError) {
            this.codeError = "DASH-" + codeError;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder httpStatus(HttpStatus httpStatus) {
            this.status = httpStatus;
            return this;
        }

        public StorageError unlabeledError(String details) {
            codeError(10000L);
            message("Unlabeled error");
            details(details);
            httpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return new StorageError(codeError, message, this.details, this.status);
        }

        public StorageError build() {
            return new StorageError(codeError, message, details, this.status);
        }
    }


    @Override
    public String toString() {
        return "StorageError{" +
                "codeError='" + codeError + '\'' +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                ", status=" + status +
                '}';
    }
}

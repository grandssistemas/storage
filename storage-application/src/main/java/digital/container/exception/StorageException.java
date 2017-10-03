package digital.container.exception;

public class StorageException extends RuntimeException {
    private StorageError storageError;

    public StorageException(StorageError storageError) {
        super(storageError.getMessage());
        this.storageError = storageError;
    }

    public StorageError getStorageError() {
        return storageError;
    }

    public void setStorageError(StorageError storageError) {
        this.storageError = storageError;
    }
}

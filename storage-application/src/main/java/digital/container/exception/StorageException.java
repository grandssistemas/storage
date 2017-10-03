package digital.container.exception;

import digital.container.storage.domain.model.file.CauseProblem;
import digital.container.storage.domain.model.file.SituationCauseProblem;

public class StorageException extends RuntimeException {
    private StorageError storageError;
    private SituationCauseProblem situationCauseProblem;

    public StorageException(StorageError storageError, SituationCauseProblem situationCauseProblem) {
        super(storageError.getMessage());
        this.storageError = storageError;
        this.situationCauseProblem = situationCauseProblem;
    }

    public StorageError getStorageError() {
        return storageError;
    }

    public void setStorageError(StorageError storageError) {
        this.storageError = storageError;
    }

    public SituationCauseProblem getSituationCauseProblem() {
        return situationCauseProblem;
    }

    public void setSituationCauseProblem(SituationCauseProblem situationCauseProblem) {
        this.situationCauseProblem = situationCauseProblem;
    }
}

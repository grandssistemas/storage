package digital.container.storage.domain.model.file;

public enum FileStatus {
    IS_IN_ERROR,
    DO_NOT_SYNC,
    NOT_SYNC,
    SYNCHRONIZED,
    PROCESSED,
    FAILED_SYNC,
    FAILED_SYNC_IN_CONSUMER,
    FAILED_SYNC_IN_SEND_TO_MOM;

}

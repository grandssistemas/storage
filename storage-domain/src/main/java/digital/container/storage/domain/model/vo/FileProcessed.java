package digital.container.storage.domain.model.vo;


import digital.container.storage.domain.model.AbstractFile;

import java.io.Serializable;
import java.util.List;

public class FileProcessed implements Serializable {

    private final AbstractFile file;
    private final List<String> errors;

    public FileProcessed(AbstractFile file, List<String> errors) {
        this.file = file;
        this.errors = errors;
    }
    public FileProcessed() {
        this.file = null;
        this.errors = null;
    }

    public AbstractFile getFile() {
        return file;
    }

    public List<String> getErrors() {
        return errors;
    }
}

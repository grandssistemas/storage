package digital.container.vo;


import digital.container.storage.domain.model.file.AbstractFile;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

public class FileProcessed implements Serializable {

    private final AbstractFile file;
    private final List<String> errors;
    @Transient
    private final MultipartFile multipartFile;
    @Transient
    private String xml;

    public FileProcessed(AbstractFile file, List<String> errors, MultipartFile multipartFile) {
        this.file = file;
        this.errors = errors;
        this.multipartFile = multipartFile;
    }

    public FileProcessed(AbstractFile file, List<String> errors) {
        this.file = file;
        this.errors = errors;
        this.multipartFile = null;
    }

    public FileProcessed(AbstractFile file, List<String> errors, String xml) {
        this.file = file;
        this.errors = errors;
        this.multipartFile = null;
        this.xml = xml;
    }

    public FileProcessed() {
        this.file = null;
        this.errors = null;
        this.multipartFile = null;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public AbstractFile getFile() {
        return file;
    }

    public List<String> getErrors() {
        return errors;
    }

    public MultipartFile getMultipartFile() {
        return multipartFile;
    }

    public String getXml() {
        return xml;
    }
}

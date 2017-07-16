package digital.container.storage.domain.model.file;

import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.*;

@Entity
@Table(name = "local_file",
        indexes = {
                @Index(name = "local_file_index_oi", columnList = "oi"),
                @Index(name = "local_file_index_hash", columnList = "hash")
        })
@GumgaMultitenancy
@SequenceGenerator(name = GumgaSharedModel.SEQ_NAME, sequenceName = "seq_local_file")
public class LocalFile extends AbstractFile {
    public static int BUFFER_SIZE = 4096;

    @Column(name = "relative_path")
    private String relativePath;

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}
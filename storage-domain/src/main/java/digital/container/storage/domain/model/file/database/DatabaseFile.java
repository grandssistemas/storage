package digital.container.storage.domain.model.file.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import digital.container.storage.domain.model.file.AbstractFile;
import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "database_file",
        indexes = {
            @Index(name = "database_file_index_oi", columnList = "oi"),
            @Index(name = "database_file_index_hash", columnList = "hash"),
            @Index(name = "database_file_index_hash_public", columnList = "hash, file_public"),
            @Index(name = "database_file_index_oi_detailone_filetype", columnList = "oi, detail_one, file_type")
        })
@GumgaMultitenancy
@SequenceGenerator(name = GumgaSharedModel.SEQ_NAME, sequenceName = "seq_database_file")
public class DatabaseFile extends AbstractFile {

    @JsonIgnore
    @OneToMany(mappedBy = "databaseFile", fetch = FetchType.LAZY)
    @OrderBy("id")
    private List<DatabaseFilePart> parts;

    @Column(name = "relative_path")
    private String relativePath;

    public DatabaseFile() {
        this.parts = new ArrayList<>();
    }

    public void addDatabaseFilePart(DatabaseFilePart databaseFilePart) {
        this.parts.add(databaseFilePart);
    }

    public List<DatabaseFilePart> getParts() {
        return parts;
    }

    public void setParts(List<DatabaseFilePart> parts) {
        this.parts = parts;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}

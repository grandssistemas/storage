package digital.container.storage.domain.model.file.database;

import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.GumgaSharedModelUUID;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.*;

@Entity
@Table(name = "database_file_part")
@GumgaMultitenancy
public class DatabaseFilePart extends GumgaSharedModelUUID {

    public final static int PART_SIZE = 4096;

    @ManyToOne
    @JoinColumn(name = "database_file_id")
    private DatabaseFile databaseFile;

    @Lob
    @Column(name = "raw_bytes", length = PART_SIZE)
    private byte[] rawBytes;

    public DatabaseFile getDatabaseFile() {
        return databaseFile;
    }

    public void setDatabaseFile(DatabaseFile databaseFile) {
        this.databaseFile = databaseFile;
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public void setRawBytes(byte[] rawBytes) {
        this.rawBytes = rawBytes;
    }
}

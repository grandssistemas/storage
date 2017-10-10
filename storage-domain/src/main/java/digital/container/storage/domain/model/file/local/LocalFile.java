package digital.container.storage.domain.model.file.local;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.util.GenerateHash;
import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.shared.GumgaSharedModel;
import javax.persistence.*;

@Entity
@Table(name = "local_file",
        indexes = {
            @Index(name = "local_file_index_oi", columnList = "oi"),
            @Index(name = "local_file_index_hash", columnList = "hash"),
            @Index(name = "local_file_index_hash_public", columnList = "hash, file_public"),
            @Index(name = "local_file_index_oi_detailone_filetype", columnList = "oi, detail_one, file_type")
        })
@GumgaMultitenancy
@SequenceGenerator(name = GumgaSharedModel.SEQ_NAME, sequenceName = "seq_local_file")
public class LocalFile extends AbstractFile {
    public static final int BUFFER_SIZE = 4096;

    @Override
    protected String getHashFile() {
        return GenerateHash.generateLocalFile();
    }

}

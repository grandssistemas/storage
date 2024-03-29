package digital.container.storage.domain.model.file.amazon;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.util.GenerateHash;
import digital.container.storage.domain.model.util.LocalFileUtil;
import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Calendar;

@Entity
@Table(name = "amazons3_file",
        indexes = {
                @Index(name = "amazons3_file_index_oi", columnList = "oi"),
                @Index(name = "amazons3_file_index_hash", columnList = "hash"),
                @Index(name = "amazons3_file_index_hash_public", columnList = "hash, file_public"),
                @Index(name = "amazons3_file_index_oi_detailone_filetype", columnList = "oi, detail_one, file_type")
        })
@GumgaMultitenancy
@SequenceGenerator(name = GumgaSharedModel.SEQ_NAME, sequenceName = "seq_amazons3_file")
public class AmazonS3File extends AbstractFile {

    @Override
    protected String getHashFile() {
        return GenerateHash.generateAmazonS3();
    }
}

package digital.container.storage.domain.model.download;

import digital.container.storage.domain.model.util.SearchScheduling;
import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "link_download",
        indexes = {
                @Index(name = "link_download_index_oi", columnList = "oi"),
        })
@GumgaMultitenancy
@SequenceGenerator(name = GumgaSharedModel.SEQ_NAME, sequenceName = "seq_link_download")
public class LinkDownload extends GumgaSharedModel<Long> {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ld_created")
    private Date created = new Date();
    @Column(name = "hash")
    private String hash;
    @Column(name = "description")
    private String description;
    @Column(name = "relative_path")
    private String relativePath;
    @Column(name = "name")
    private String name;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void generateDescription(SearchScheduling searchScheduling) {
        this.description = "Download dos arquivos "+searchScheduling.getTypes().toString()+
                " do periodo data aqui "+
                " dos cnpjs " + searchScheduling.getCnpjs().toString();
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

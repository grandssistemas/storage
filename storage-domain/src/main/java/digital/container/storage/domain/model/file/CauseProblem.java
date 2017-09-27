package digital.container.storage.domain.model.file;

import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "cause_problem",
        indexes = {
                @Index(name = "cause_problem_index_oi", columnList = "oi")
        })
@GumgaMultitenancy
@SequenceGenerator(name = GumgaSharedModel.SEQ_NAME, sequenceName = "seq_cause_problem")
public class CauseProblem extends GumgaSharedModel<Long> {

    @Column(name = "reason")
    private String reason;
    @Column(name = "hash")
    private String hash;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "cp_created")
    private Date created = new Date();

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}

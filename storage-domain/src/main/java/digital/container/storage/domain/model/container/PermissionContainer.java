package digital.container.storage.domain.model.container;

import io.gumga.domain.GumgaModel;
import io.gumga.domain.GumgaMultitenancy;
import io.gumga.domain.GumgaSharedModelUUID;
import io.gumga.domain.shared.GumgaSharedModel;

import javax.persistence.*;

@Entity
@Table(name = "permission_container",
        indexes = {
            @Index(name = "permission_container_index_oi", columnList = "oi")
        })
@GumgaMultitenancy
public class PermissionContainer extends GumgaSharedModelUUID{

    @Column(name = "container_key")
    private String containerKey;

    public String getContainerKey() {
        return containerKey;
    }

    public void setContainerKey(String containerKey) {
        this.containerKey = containerKey;
    }

}

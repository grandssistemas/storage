package digital.container.service.container;

import digital.container.repository.PermissionContainerRepository;
import digital.container.storage.domain.model.container.PermissionContainer;
import io.gumga.application.GumgaService;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionContainerService extends GumgaService<PermissionContainer, Long> {

    private PermissionContainerRepository permissionContainerRepository;

    @Autowired
    public PermissionContainerService(GumgaCrudRepository<PermissionContainer, Long> repository) {
        super(repository);
        this.permissionContainerRepository = PermissionContainerRepository.class.cast(repository);
    }

    public Boolean containerKeyValid(String containerKey) {
        return this.permissionContainerRepository
                .getByContainerKey(containerKey, new GumgaOi(GumgaThreadScope.organizationCode.get() + "%"))
                .isPresent();
    }
}

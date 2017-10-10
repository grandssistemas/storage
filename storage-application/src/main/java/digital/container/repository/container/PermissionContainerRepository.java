package digital.container.repository.container;

import digital.container.storage.domain.model.container.PermissionContainer;
import io.gumga.domain.domains.GumgaOi;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionContainerRepository extends GumgaCrudRepository<PermissionContainer, String> {

    @Query("from PermissionContainer as pc where pc.containerKey = :containerKey and (pc.oi like :oi or pc.oi is null)")
    Optional<PermissionContainer> getByContainerKey(@Param("containerKey") String containerKey, @Param("oi")GumgaOi oi);
}

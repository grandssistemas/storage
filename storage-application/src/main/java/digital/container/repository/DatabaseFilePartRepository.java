package digital.container.repository;

import digital.container.storage.domain.model.file.DatabaseFilePart;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseFilePartRepository extends GumgaCrudRepository<DatabaseFilePart, Long> {
}

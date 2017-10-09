package digital.container.repository.file;

import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseFilePartRepository extends GumgaCrudRepository<DatabaseFilePart, Long> {
}

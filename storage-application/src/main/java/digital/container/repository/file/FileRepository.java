package digital.container.repository.file;

import digital.container.storage.domain.model.file.AbstractFile;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FileRepository extends GumgaCrudRepository<AbstractFile, String> {



}

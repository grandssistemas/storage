package digital.container.repository.file;

import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmazonS3FileRepository extends GumgaCrudRepository<AmazonS3File, Long> {
}

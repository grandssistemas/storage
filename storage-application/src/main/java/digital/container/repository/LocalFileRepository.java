package digital.container.repository;

import digital.container.storage.domain.model.file.LocalFile;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocalFileRepository extends GumgaCrudRepository<LocalFile, Long> {

    @Query(value = "from LocalFile df where df.hash = :hash and df.filePublic = :shared")
    Optional<LocalFile> getByHash(@Param("hash") String hash, @Param("shared") Boolean shared);

    @Query(value = "from LocalFile df where df.hash = :hash")
    Optional<LocalFile> getByHash(@Param("hash") String hash);

}

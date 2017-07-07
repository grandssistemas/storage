package digital.container.repository;

import digital.container.storage.domain.model.file.DatabaseFile;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatabaseFileRepository extends GumgaCrudRepository<DatabaseFile, Long> {

    @Query(value = "from DatabaseFile df where df.hash = :hash and df.filePublic = :shared")
    Optional<DatabaseFile> getByHash(@Param("hash") String hash, @Param("shared") Boolean shared);

    @Query(value = "from DatabaseFile df where df.hash = :hash")
    Optional<DatabaseFile> getByHash(@Param("hash") String hash);
}

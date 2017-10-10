package digital.container.repository.download;

import digital.container.storage.domain.model.download.LinkDownload;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkDownloadRepository extends GumgaCrudRepository<LinkDownload, String> {

    @Query("from LinkDownload ld where ld.hash = :hash")
    LinkDownload getLinkDownloadByHash(@Param("hash") String hash);
}

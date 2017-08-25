package digital.container.repository;

import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.LocalFile;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
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

    @Query(value = "from LocalFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE' or df.fileType = 'NFCE')")
    Optional<LocalFile> getFileByGumgaOIAndChNFeAndNF(@Param("oi") GumgaOi oi, @Param("chNFe") String chNFe);

    @Query(value = "from LocalFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE_CANCELED' or df.fileType = 'NFCE_CANCELED')")
    Optional<LocalFile> getFileByGumgaOIAndChNFeAndNFCanceled(@Param("oi") GumgaOi oi, @Param("chNFe") String chNFe);

    @Query(value = "from LocalFile df where df.oi like :oi and df.detailOne = :nprot and (df.fileType = 'NFE_DISABLE' or df.fileType = 'NFCE_DISABLE')")
    Optional<LocalFile> getFileByGumgaOIAndNProtAndNFDisable(@Param("oi")GumgaOi oi, @Param("nprot") String nprot);
}

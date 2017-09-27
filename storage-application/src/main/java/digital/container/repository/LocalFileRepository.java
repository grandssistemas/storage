package digital.container.repository;

import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.LocalFile;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = "from LocalFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE_LETTER_CORRECTION' or df.fileType = 'NFCE_LETTER_CORRECTION')")
    Optional<LocalFile> getFileByGumgaOIAndNProtAndNFLetterCorrection(@Param("oi") GumgaOi oi, @Param("chNFe") String chNFe);

    @Query(value = "from LocalFile df where df.hash = :hash and df.fileType != 'ANYTHING'")
    Optional<LocalFile> getTaxDocumentByHash(@Param("hash") String hash);

    @Query(value = "from LocalFile df where df.detailOne = :detailOne and df.oi like :gumgaOi and df.fileType != 'ANYTHING'")
    Optional<LocalFile> getTaxDocumentByDetailOneAndGumgaOI(@Param("detailOne") String detailOne, @Param("gumgaOi") GumgaOi gumgaOi);

    @Query(value = "from LocalFile df where df.oi like :oi and df.fileType in :types and df.containerKey in :cnpjs and (df.detailTwo is not null and (to_date(df.detailTwo, 'YYYY-MM-DD') >= to_date('2017-06-09', 'YYYY-MM-DD') and to_date(df.detailTwo, 'YYYY-MM-DD') <= to_date('2017-06-09', 'YYYY-MM-DD')))")
    List<LocalFile> getTaxDocumentBySearchScheduling(@Param("oi") GumgaOi oi, @Param("types") List<FileType> types, @Param("cnpjs") List<String> cnpjs);
}

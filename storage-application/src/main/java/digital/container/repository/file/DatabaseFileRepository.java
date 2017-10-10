package digital.container.repository.file;

import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.FileType;
import io.gumga.domain.domains.GumgaOi;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatabaseFileRepository extends GumgaCrudRepository<DatabaseFile, String> {

    @Query(value = "from DatabaseFile df where df.hash = :hash and df.filePublic = :shared")
    Optional<DatabaseFile> getByHash(@Param("hash") String hash, @Param("shared") Boolean shared);

    @Query(value = "from DatabaseFile df where (df.oi like :oi or df.gumgaOrganizations like :sharedOi) and df.hash = :hash")
    Optional<DatabaseFile> getByHash(@Param("hash") String hash, @Param("oi") GumgaOi oi, @Param("sharedOi") String sharedOi);

    @Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE' or df.fileType = 'NFCE')")
    Optional<DatabaseFile> getFileByGumgaOIAndChNFeAndNF(@Param("oi") GumgaOi oi, @Param("chNFe") String chNFe);

    @Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE_CANCELED' or df.fileType = 'NFCE_CANCELED')")
    Optional<DatabaseFile> getFileByGumgaOIAndChNFeAndNFCanceled(@Param("oi") GumgaOi oi, @Param("chNFe") String chNFe);

    @Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :nprot and (df.fileType = 'NFE_DISABLE' or df.fileType = 'NFCE_DISABLE')")
    Optional<DatabaseFile> getFileByGumgaOIAndNProtAndNFDisable(@Param("oi")GumgaOi oi, @Param("nprot") String nprot);

    @Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE_LETTER_CORRECTION' or df.fileType = 'NFCE_LETTER_CORRECTION')")
    Optional<DatabaseFile> getFileByGumgaOIAndNProtAndNFLetterCorrection(@Param("oi") GumgaOi oi, @Param("chNFe") String chNFe);

    @Query(value = "from DatabaseFile df where df.hash = :hash and df.fileType != 'ANYTHING'")
    Optional<DatabaseFile> getTaxDocumentByHash(@Param("hash") String hash);

    @Query(value = "from DatabaseFile df where df.detailOne = :detailOne and df.oi like :gumgaOi and df.fileType != 'ANYTHING'")
    Optional<DatabaseFile> getTaxDocumentByDetailOneAndGumgaOI(@Param("detailOne") String detailOne, @Param("gumgaOi") GumgaOi gumgaOi);

    @Query(value = "from DatabaseFile df where df.oi like :oi and df.fileType in :types and df.containerKey in :cnpjs and (df.detailTwo is not null and (to_date(df.detailTwo, 'YYYY-MM-DD') >= to_date(:startDate, 'YYYY-MM-DD') and to_date(df.detailTwo, 'YYYY-MM-DD') <= to_date(:endDate, 'YYYY-MM-DD')))")
    List<DatabaseFile> getTaxDocumentBySearchScheduling(@Param("oi") GumgaOi oi, @Param("types") List<FileType> types, @Param("cnpjs") List<String> cnpjs, @Param("startDate") String startDate, @Param("endDate") String endDate);


}

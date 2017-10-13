package digital.container.service.taxdocument;

import digital.container.repository.file.DatabaseFileRepository;
import digital.container.repository.file.FileRepository;
import digital.container.repository.file.LocalFileRepository;
import digital.container.service.file.amazons3.AmazonS3FileService;
import digital.container.service.file.databasefile.DatabaseFileService;
import digital.container.service.file.localfile.LocalFileService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.util.SearchScheduling;
import digital.container.storage.domain.model.util.TaxDocumentScheduling;
import io.gumga.core.QueryObject;
import io.gumga.core.SearchResult;
import io.gumga.core.gquery.ComparisonOperator;
import io.gumga.core.gquery.Criteria;
import io.gumga.core.gquery.CriteriaField;
import io.gumga.core.gquery.GQuery;
import io.gumga.domain.domains.GumgaOi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class SearchTaxDocumentService {

    private final LocalFileRepository localFileRepository;
    private final DatabaseFileRepository databaseFileRepository;
    private final FileRepository fileRepository;

    private final AmazonS3FileService amazonS3FileService;
    private final DatabaseFileService databaseFileService;
    private final LocalFileService localFileService;


    @Autowired
    public SearchTaxDocumentService(LocalFileRepository localFileRepository,
                                    DatabaseFileRepository databaseFileRepository,
                                    FileRepository fileRepository,
                                    AmazonS3FileService amazonS3FileService,
                                    DatabaseFileService databaseFileService,
                                    LocalFileService localFileService) {
        this.localFileRepository = localFileRepository;
        this.databaseFileRepository = databaseFileRepository;
        this.fileRepository = fileRepository;


        this.amazonS3FileService = amazonS3FileService;
        this.databaseFileService = databaseFileService;
        this.localFileService = localFileService;
    }


    public AbstractFile getFileByGumgaOIAndChNFeAndNF(GumgaOi oi, String chNFe) {
        //@Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE' or df.fileType = 'NFCE')")
        GQuery where = new GQuery(new Criteria("obj.detailOne", ComparisonOperator.EQUAL, chNFe))
                .and(new Criteria("obj.fileType", ComparisonOperator.IN, Arrays.asList(FileType.NFE, FileType.NFCE)));

        return findOneAbstractFile(where);
//        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndChNFeAndNF(new GumgaOi(oi), chNFe);
//        if(dbDocument.isPresent()) {
//            return dbDocument.get();
//        }
//
//        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndChNFeAndNF(new GumgaOi(oi), chNFe);
//        if(lfDocument.isPresent()) {
//            return lfDocument.get();
//        }
//
//        return null;
    }


    public AbstractFile getFileByGumgaOIAndChNFeAndNFCanceled(GumgaOi oi, String chNFe) {
        //@Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE_CANCELED' or df.fileType = 'NFCE_CANCELED')")
        GQuery where = new GQuery(new Criteria("obj.detailOne", ComparisonOperator.EQUAL, chNFe))
                .and(new Criteria("obj.fileType", ComparisonOperator.IN, Arrays.asList(FileType.NFE_CANCELED, FileType.NFCE_CANCELED)));

        return findOneAbstractFile(where);
//        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndChNFeAndNFCanceled(oi, chNFe);
//        if(dbDocument.isPresent()) {
//            return dbDocument.get();
//        }
//
//        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndChNFeAndNFCanceled(oi, chNFe);
//        if(lfDocument.isPresent()) {
//            return lfDocument.get();
//        }
//
//        return null;
    }

    public AbstractFile getFileByGumgaOIAndNProtAndNFDisable(GumgaOi oi, String nprot) {
        //@Query(value = "from DatabaseFile df where df.oi like :oi and df.detailOne = :nprot and (df.fileType = 'NFE_DISABLE' or df.fileType = 'NFCE_DISABLE')")
        GQuery where = new GQuery(new Criteria("obj.detailOne", ComparisonOperator.EQUAL, nprot))
                .and(new Criteria("obj.fileType", ComparisonOperator.IN, Arrays.asList(FileType.NFCE_DISABLE, FileType.NFE_DISABLE)));

        return findOneAbstractFile(where);

//        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndNProtAndNFDisable(oi, nprot);
//        if(dbDocument.isPresent()) {
//            return dbDocument.get();
//        }
//
//        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndNProtAndNFDisable(oi, nprot);
//        if(lfDocument.isPresent()) {
//            return lfDocument.get();
//        }


    }

    public AbstractFile getFileByGumgaOIAndNProtAndNFLetterCorrection(GumgaOi oi, String chNFe) {
        //from DatabaseFile df where df.oi like :oi and df.detailOne = :chNFe and (df.fileType = 'NFE_LETTER_CORRECTION' or df.fileType = 'NFCE_LETTER_CORRECTION')
        GQuery where = new GQuery(new Criteria("obj.detailOne", ComparisonOperator.EQUAL, chNFe))
                .and(new Criteria("obj.fileType", ComparisonOperator.IN, Arrays.asList(FileType.NFE_LETTER_CORRECTION, FileType.NFCE_LETTER_CORRECTION)));

        return findOneAbstractFile(where);

//        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndNProtAndNFLetterCorrection(oi, chNFe);
//        if(dbDocument.isPresent()) {
//            return dbDocument.get();
//        }
//
//        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndNProtAndNFLetterCorrection(oi, chNFe);
//        if(lfDocument.isPresent()) {
//            return lfDocument.get();
//        }
//
//        return null;
    }

    public AbstractFile getTaxDocumentByHash(String hash) {
//        @Query(value = "from DatabaseFile df where df.hash = :hash and df.fileType != 'ANYTHING'")
        GQuery where = new GQuery(new Criteria("obj.hash", ComparisonOperator.EQUAL, hash))
                .and(new Criteria("obj.fileType", ComparisonOperator.NOT_EQUAL, FileType.ANYTHING));

        return findOneAbstractFile(where);
//        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getTaxDocumentByHash(hash);
//        if(dbDocument.isPresent()) {
//            return dbDocument.get();
//        }
//
//        Optional<LocalFile> lfDocument = this.localFileRepository.getTaxDocumentByHash(hash);
//        if(lfDocument.isPresent()) {
//            return lfDocument.get();
//        }
//
//        return null;
    }

    public AbstractFile getTaxDocumentByDetailOneAndFileTypes(String detailOne, List<FileType> fileTypes) {
//        @Query(value = "from DatabaseFile df where df.detailOne = :detailOne and df.oi like :gumgaOi and df.fileType != 'ANYTHING'")
        GQuery where = new GQuery(new Criteria("obj.detailOne", ComparisonOperator.EQUAL, detailOne))
                .and(new Criteria("obj.fileType", ComparisonOperator.IN, fileTypes));

        return findOneAbstractFile(where);

//        String oi = GumgaThreadScope.organizationCode.get();
//        GumgaOi gumgaOi = new GumgaOi(oi + "%");
//        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getTaxDocumentByDetailOneAndGumgaOI(detailOne, gumgaOi);
//        if(dbDocument.isPresent()) {
//            return dbDocument.get();
//        }
//
//        Optional<LocalFile> lfDocument = this.localFileRepository.getTaxDocumentByDetailOneAndGumgaOI(detailOne, gumgaOi);
//        if(lfDocument.isPresent()) {
//            return lfDocument.get();
//        }
//
//        return null;
    }

    public List<AbstractFile> getTaxDocumentBySearchScheduling(SearchScheduling searchScheduling) {
        String oi = searchScheduling.getOrganizationCode();
        GumgaOi gumgaOi = new GumgaOi(oi + "%");
        List<FileType> fileTypes = new ArrayList<>();
        List<AbstractFile> files = new ArrayList<>();

        String startDate = null;
        String endDate = null;
        if(!StringUtils.isEmpty(searchScheduling.getStartDate()) && !StringUtils.isEmpty(searchScheduling.getEndDate())) {
            startDate = searchScheduling.getStartDate();
            endDate = searchScheduling.getEndDate();
        } else {
            Calendar instance = Calendar.getInstance();
            instance.set(Calendar.MONTH, instance.get(Calendar.MONTH)-1);
            instance.set(Calendar.DAY_OF_MONTH, instance.getActualMinimum(Calendar.DAY_OF_MONTH));

            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
            startDate = sdf.format(instance.getTime());

            instance.set(Calendar.DAY_OF_MONTH, instance.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = sdf.format(instance.getTime());

        }

        searchScheduling.getTypes().forEach(types -> {
            if(TaxDocumentScheduling.NFE.equals(types)) {
                fileTypes.add(FileType.NFE);
                fileTypes.add(FileType.NFE_CANCELED);
                fileTypes.add(FileType.NFE_DISABLE);
                fileTypes.add(FileType.NFE_LETTER_CORRECTION);
            }

            if(TaxDocumentScheduling.NFCE.equals(types)) {
                fileTypes.add(FileType.NFCE);
                fileTypes.add(FileType.NFCE_CANCELED);
                fileTypes.add(FileType.NFCE_DISABLE);
                fileTypes.add(FileType.NFE_LETTER_CORRECTION);
            }
        });

//        if(searchScheduling.getCnpjs().size() > 0) {
//            List<DatabaseFile> taxDocumentBySearchScheduling = this.databaseFileRepository.getTaxDocumentBySearchScheduling(gumgaOi, fileTypes, searchScheduling.getCnpjs(), startDate, endDate);
//            files.addAll(taxDocumentBySearchScheduling);
//
//            List<LocalFile> lfs = this.localFileRepository.getTaxDocumentBySearchScheduling(gumgaOi, fileTypes, searchScheduling.getCnpjs(), startDate, endDate);
//            files.addAll(lfs);
//        }


        QueryObject queryObject = new QueryObject();
        queryObject.setPageSize(Integer.MAX_VALUE);

        GQuery where = new GQuery(new Criteria("obj.fileType", ComparisonOperator.IN, fileTypes))
                .and(new Criteria("obj.containerKey", ComparisonOperator.IN, searchScheduling.getCnpjs()))
                .and(new Criteria("obj.detailTwo", ComparisonOperator.NOT_EQUAL, ""))
                .and(new Criteria("to_date(obj.detailTwo, 'YYYY-MM-DD')", ComparisonOperator.GREATER_EQUAL, new CriteriaField("to_date('" + startDate + "', 'YYYY-MM-DD')")))
                .and(new Criteria("to_date(obj.detailTwo, 'YYYY-MM-DD')", ComparisonOperator.LOWER_EQUAL, new CriteriaField("to_date('" + endDate + "', 'YYYY-MM-DD')")));


        queryObject.setgQuery(where);

        SearchResult<AmazonS3File> pesquisa = this.amazonS3FileService.pesquisa(queryObject);
        files.addAll(pesquisa.getValues());
        SearchResult<DatabaseFile> pesquisa1 = this.databaseFileService.pesquisa(queryObject);
        files.addAll(pesquisa1.getValues());
        SearchResult<LocalFile> pesquisa2 = this.localFileService.pesquisa(queryObject);
        files.addAll(pesquisa2.getValues());


        return files;
    }

    private AbstractFile findOneAbstractFile(GQuery where) {
        QueryObject queryObject = new QueryObject();
        queryObject.setgQuery(where);

        SearchResult<AmazonS3File> pesquisaAmazonS3 = this.amazonS3FileService.pesquisa(queryObject);
        if(!pesquisaAmazonS3.getValues().isEmpty()) {
            return pesquisaAmazonS3.getValues().get(0);
        }

        SearchResult<DatabaseFile> pesquisaDatabase = this.databaseFileService.pesquisa(queryObject);
        if(!pesquisaDatabase.getValues().isEmpty()) {
            return pesquisaDatabase.getValues().get(0);
        }

        SearchResult<LocalFile> pesquisaLocalfile = this.localFileService.pesquisa(queryObject);
        if(!pesquisaLocalfile.getValues().isEmpty()) {
            return pesquisaLocalfile.getValues().get(0);
        }

        return null;
    }

    private List<AbstractFile> findAllAbstractFile(GQuery where) {
        List<AbstractFile> files = new ArrayList<>();
        QueryObject queryObject = new QueryObject();
        queryObject.setgQuery(where);


        SearchResult<AmazonS3File> pesquisaAmazonS3 = this.amazonS3FileService.pesquisa(queryObject);
        if(!pesquisaAmazonS3.getValues().isEmpty()) {
            files.addAll(pesquisaAmazonS3.getValues());
        }

        SearchResult<DatabaseFile> pesquisaDatabase = this.databaseFileService.pesquisa(queryObject);
        if(!pesquisaDatabase.getValues().isEmpty()) {
            files.addAll(pesquisaDatabase.getValues());
        }

        SearchResult<LocalFile> pesquisaLocalfile = this.localFileService.pesquisa(queryObject);
        if(!pesquisaLocalfile.getValues().isEmpty()) {
            files.addAll(pesquisaLocalfile.getValues());
        }

        return files;
    }
}

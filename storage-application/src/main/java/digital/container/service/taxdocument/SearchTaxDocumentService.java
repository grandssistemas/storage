package digital.container.service.taxdocument;

import digital.container.repository.file.DatabaseFileRepository;
import digital.container.repository.file.LocalFileRepository;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.util.SearchScheduling;
import digital.container.storage.domain.model.util.TaxDocumentScheduling;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SearchTaxDocumentService {

    private final LocalFileRepository localFileRepository;
    private final DatabaseFileRepository databaseFileRepository;

    @Autowired
    public SearchTaxDocumentService(LocalFileRepository localFileRepository,
                                    DatabaseFileRepository databaseFileRepository) {
        this.localFileRepository = localFileRepository;
        this.databaseFileRepository = databaseFileRepository;
    }


    public AbstractFile getFileByGumgaOIAndChNFeAndNF(GumgaOi oi, String chNFe) {
        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndChNFeAndNF(new GumgaOi(oi), chNFe);
        if(dbDocument.isPresent()) {
            return dbDocument.get();
        }

        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndChNFeAndNF(new GumgaOi(oi), chNFe);
        if(lfDocument.isPresent()) {
            return lfDocument.get();
        }

        return null;
    }


    public AbstractFile getFileByGumgaOIAndChNFeAndNFCanceled(GumgaOi oi, String chNFe) {
        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndChNFeAndNFCanceled(oi, chNFe);
        if(dbDocument.isPresent()) {
            return dbDocument.get();
        }

        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndChNFeAndNFCanceled(oi, chNFe);
        if(lfDocument.isPresent()) {
            return lfDocument.get();
        }

        return null;
    }

    public AbstractFile getFileByGumgaOIAndNProtAndNFDisable(GumgaOi oi, String nprot) {
        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndNProtAndNFDisable(oi, nprot);
        if(dbDocument.isPresent()) {
            return dbDocument.get();
        }

        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndNProtAndNFDisable(oi, nprot);
        if(lfDocument.isPresent()) {
            return lfDocument.get();
        }

        return null;

    }

    public AbstractFile getFileByGumgaOIAndNProtAndNFLetterCorrection(GumgaOi oi, String chNFe) {
        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getFileByGumgaOIAndNProtAndNFLetterCorrection(oi, chNFe);
        if(dbDocument.isPresent()) {
            return dbDocument.get();
        }

        Optional<LocalFile> lfDocument = this.localFileRepository.getFileByGumgaOIAndNProtAndNFLetterCorrection(oi, chNFe);
        if(lfDocument.isPresent()) {
            return lfDocument.get();
        }

        return null;
    }

    public AbstractFile getTaxDocumentByHash(String hash) {
        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getTaxDocumentByHash(hash);
        if(dbDocument.isPresent()) {
            return dbDocument.get();
        }

        Optional<LocalFile> lfDocument = this.localFileRepository.getTaxDocumentByHash(hash);
        if(lfDocument.isPresent()) {
            return lfDocument.get();
        }

        return null;
    }

    public AbstractFile getTaxDocumentByDetailOneAndGumgaOI(String detailOne) {
        String oi = GumgaThreadScope.organizationCode.get();
        GumgaOi gumgaOi = new GumgaOi(oi + "%");
        Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getTaxDocumentByDetailOneAndGumgaOI(detailOne, gumgaOi);
        if(dbDocument.isPresent()) {
            return dbDocument.get();
        }

        Optional<LocalFile> lfDocument = this.localFileRepository.getTaxDocumentByDetailOneAndGumgaOI(detailOne, gumgaOi);
        if(lfDocument.isPresent()) {
            return lfDocument.get();
        }

        return null;
    }

    public List<AbstractFile> getTaxDocumentBySearchScheduling(SearchScheduling searchScheduling) {
        String oi = searchScheduling.getOrganizationCode();
        GumgaOi gumgaOi = new GumgaOi(oi + "%");
        List<FileType> fileTypes = new ArrayList<>();
        List<AbstractFile> files = new ArrayList<>();

        String startDate = null;
        String endDate = null;
        if(!StringUtils.isEmpty(searchScheduling.getStartDate()) && !StringUtils.isEmpty(searchScheduling.getEndDate())) {

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

        if(searchScheduling.getCnpjs().size() > 0) {
            List<DatabaseFile> taxDocumentBySearchScheduling = this.databaseFileRepository.getTaxDocumentBySearchScheduling(gumgaOi, fileTypes, searchScheduling.getCnpjs(), startDate, endDate);
            files.addAll(taxDocumentBySearchScheduling);

            List<LocalFile> lfs = this.localFileRepository.getTaxDocumentBySearchScheduling(gumgaOi, fileTypes, searchScheduling.getCnpjs(), startDate, endDate);
            files.addAll(lfs);
        } else {

        }



        return files;
    }
}

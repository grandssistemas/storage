package digital.container.service.taxdocument;

import digital.container.repository.DatabaseFileRepository;
import digital.container.repository.LocalFileRepository;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.util.SearchScheduling;
import digital.container.util.TaxDocumentScheduling;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SearchTaxDocumentService {

    @Autowired
    private LocalFileRepository localFileRepository;

    @Autowired
    private DatabaseFileRepository databaseFileRepository;


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
        String oi = GumgaThreadScope.organizationCode.get();
        GumgaOi gumgaOi = new GumgaOi(oi + "%");
        List<FileType> fileTypes = new ArrayList<>();
        List<AbstractFile> files = new ArrayList<>();

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


        List<DatabaseFile> taxDocumentBySearchScheduling = this.databaseFileRepository.getTaxDocumentBySearchScheduling(gumgaOi, fileTypes, searchScheduling.getCnpjs());
        files.addAll(taxDocumentBySearchScheduling);

        return files;
    }
}

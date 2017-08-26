package digital.container.service.taxdocument;

import digital.container.repository.DatabaseFileRepository;
import digital.container.repository.LocalFileRepository;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.LocalFile;
import io.gumga.domain.domains.GumgaOi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

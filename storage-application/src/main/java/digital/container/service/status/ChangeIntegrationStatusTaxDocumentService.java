package digital.container.service.status;

import digital.container.repository.file.DatabaseFileRepository;
import digital.container.repository.file.LocalFileRepository;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.local.LocalFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChangeIntegrationStatusTaxDocumentService {

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private LocalFileRepository localFileRepository;
    @Autowired
    private DatabaseFileRepository databaseFile;


    public Boolean changeStatusTaxDocumentByHash(String hash, FileStatus status) {
        AbstractFile taxDocumentByHash = searchTaxDocumentService.getTaxDocumentByHash(hash);

        if(taxDocumentByHash == null) {
            return Boolean.FALSE;
        }

        taxDocumentByHash.setFileStatus(status);

        if(taxDocumentByHash instanceof DatabaseFile) {
            DatabaseFile db = (DatabaseFile) taxDocumentByHash;
            this.databaseFile.saveAndFlush(db);
        } else {
            LocalFile lf = (LocalFile) taxDocumentByHash;
            this.localFileRepository.saveAndFlush(lf);
        }


        return Boolean.TRUE;
    }
}

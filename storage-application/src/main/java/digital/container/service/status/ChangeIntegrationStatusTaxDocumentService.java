package digital.container.service.status;

import digital.container.repository.file.AmazonS3FileRepository;
import digital.container.repository.file.DatabaseFileRepository;
import digital.container.repository.file.LocalFileRepository;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.local.LocalFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChangeIntegrationStatusTaxDocumentService {

    private final SearchTaxDocumentService searchTaxDocumentService;
    private final LocalFileRepository localFileRepository;
    private final DatabaseFileRepository databaseFile;
    private final AmazonS3FileRepository amazonS3FileRepository;

    @Autowired
    public ChangeIntegrationStatusTaxDocumentService(SearchTaxDocumentService searchTaxDocumentService, LocalFileRepository localFileRepository, DatabaseFileRepository databaseFile, AmazonS3FileRepository amazonS3FileRepository) {
        this.searchTaxDocumentService = searchTaxDocumentService;
        this.localFileRepository = localFileRepository;
        this.databaseFile = databaseFile;
        this.amazonS3FileRepository = amazonS3FileRepository;
    }


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
            if(taxDocumentByHash instanceof LocalFile) {
                LocalFile lf = (LocalFile) taxDocumentByHash;
                this.localFileRepository.saveAndFlush(lf);
            } else {
                AmazonS3File amazonS3File = (AmazonS3File) taxDocumentByHash;
                this.amazonS3FileRepository.saveAndFlush(amazonS3File);
            }
        }


        return Boolean.TRUE;
    }
}

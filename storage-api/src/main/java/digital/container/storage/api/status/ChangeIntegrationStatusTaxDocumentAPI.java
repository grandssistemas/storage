package digital.container.storage.api.status;

import digital.container.repository.file.FileRepository;
import digital.container.service.file.databasefile.DatabaseFileService;
import digital.container.service.status.ChangeIntegrationStatusTaxDocumentService;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.util.SaveLocalFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.File;

@RestController
@RequestMapping
public class ChangeIntegrationStatusTaxDocumentAPI {

    private static final String TOKEN = "3cb73f59eb02-479b-b859-797e29eb8256-90703973edf5aa2d";
    private static final String URI_BASE = "api/public/integration-status-tax-document";

    @Autowired
    private ChangeIntegrationStatusTaxDocumentService changeIntegrationStatusTaxDocumentService;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private DatabaseFileService databaseFileService;

    @RequestMapping(path = URI_BASE + "/synchronized/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusTo(@PathVariable String hash, @PathVariable String token) {
        return changeStatus(hash, token, FileStatus.SYNCHRONIZED);

    }

    @RequestMapping(path = URI_BASE + "/failed-sync-in-consumer/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusToFailedSync(@PathVariable String hash, @PathVariable String token) {
        return changeStatus(hash, token, FileStatus.FAILED_SYNC_IN_CONSUMER);

    }

    @RequestMapping(path = URI_BASE + "/failed-sync-in-send-awss3/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusTofailedSyncInSendAwss3(@PathVariable String hash, @PathVariable String token) {
        return changeStatus(hash, token, FileStatus.FAILED_SYNC_IN_SEND_TO_AWSS3);

    }

    @RequestMapping(path = URI_BASE + "/failed-sync-in-send-container/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusTofailedSyncInSendContainer(@PathVariable String hash, @PathVariable String token) {
        return changeStatus(hash, token, FileStatus.FAILED_SYNC_IN_SEND_TO_CONTAINER);

    }

    @RequestMapping(path = URI_BASE + "/was-sent-container/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusToWasSentToContainer(@PathVariable String hash, @PathVariable String token) {
        return changeStatus(hash, token, FileStatus.WAS_SENT_TO_CONTAINER);

    }

    @RequestMapping(path = URI_BASE + "/not-found-tax-document-by-consumer/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusToNotFoundTaxDocumentByCOnsumer(@PathVariable String hash, @PathVariable String token) {
        return changeStatus(hash, token, FileStatus.NOT_FOUND_TAX_DOCUMENT_BY_CONSUMER);
    }

    /*
    NOT_FOUND_TAX_DOCUMENT_BY_CONSUMER_BUT_WAS_SAVED_CONTINGENCY,
    FAILED_SYNC_BUT_WAS_SAVED_CONTINGENCY,
    FAILED_SYNC_IN_CONSUMER_BUT_WAS_SAVED_CONTINGENCY,
    FAILED_SYNC_IN_SEND_TO_MOM_BUT_WAS_SAVED_CONTINGENCY,
    FAILED_SYNC_IN_SEND_TO_AWSS3_BUT_WAS_SAVED_CONTINGENCY,
    FAILED_SYNC_IN_SEND_TO_CONTAINER_BUT_WAS_SAVED_CONTINGENCY;
    */

    @RequestMapping(path = URI_BASE + "/change-status/hash/{hash}/{token}", method = RequestMethod.POST)
    public ResponseEntity<String> changeStatusToFailedSyncButWasSavedLocal(@PathVariable String hash, @PathVariable String token, @RequestBody FileAmazonS3 fileAmazonS3) {
        ResponseEntity<String> result = changeStatus(hash, token, fileAmazonS3.getFileStatus());

        DatabaseFile databaseFile = new DatabaseFile();
        databaseFile.setName(fileAmazonS3.getName());
        databaseFile.setHash(hash);
        databaseFile.setContentType("text/xml");
        databaseFile.setFileType(FileType.TAX_DOCUMENT_IN_CONTINGENCY);
        databaseFile.setSize(Long.valueOf(fileAmazonS3.getSize()));
        databaseFileService.saveDatabaseFile(databaseFile, fileAmazonS3.getXml());

        return result;
    }


    private ResponseEntity<String> changeStatus(@PathVariable String hash, @PathVariable String token, FileStatus notFoundTaxDocumentByConsumer) {
        if (!TOKEN.equals(token)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Token invalido.");
        }
        this.fileRepository.flush();

        Boolean result = this.changeIntegrationStatusTaxDocumentService.changeStatusTaxDocumentByHash(hash, notFoundTaxDocumentByConsumer);
        if (result) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Alterado com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado.");
    }

}

class FileAmazonS3 {
    String xml;
    String relativePath;
    String name;
    FileStatus fileStatus;
    String size;

    public FileAmazonS3() {

    }

    public FileAmazonS3(String xml, String relativePath, String name, FileStatus fileStatus, String size) {
        this.xml = xml;
        this.relativePath = relativePath;
        this.name = name;
        this.fileStatus = fileStatus;
        this.size = size;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}

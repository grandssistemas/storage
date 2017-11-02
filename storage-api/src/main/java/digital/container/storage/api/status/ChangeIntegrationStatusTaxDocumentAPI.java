package digital.container.storage.api.status;

import digital.container.service.status.ChangeIntegrationStatusTaxDocumentService;
import digital.container.storage.domain.model.file.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ChangeIntegrationStatusTaxDocumentAPI {

    private static final String TOKEN = "3cb73f59eb02-479b-b859-797e29eb8256-90703973edf5aa2d";
    private static final String URI_BASE = "api/public/integration-status-tax-document";

    @Autowired
    private ChangeIntegrationStatusTaxDocumentService changeIntegrationStatusTaxDocumentService;

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

    private ResponseEntity<String> changeStatus(@PathVariable String hash, @PathVariable String token, FileStatus notFoundTaxDocumentByConsumer) {
        if (!TOKEN.equals(token)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Token invalido.");
        }

        Boolean result = this.changeIntegrationStatusTaxDocumentService.changeStatusTaxDocumentByHash(hash, notFoundTaxDocumentByConsumer);
        if (result) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Alterado com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado.");
    }
}

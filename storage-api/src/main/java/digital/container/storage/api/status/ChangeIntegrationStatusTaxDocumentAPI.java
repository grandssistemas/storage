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
        if(!TOKEN.equals(token)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Token invalido.");
        }

        Boolean result = this.changeIntegrationStatusTaxDocumentService.changeStatusTaxDocumentByHash(hash, FileStatus.SYNCHRONIZED);
        if(result) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Alterado com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado.");

    }

    @RequestMapping(path = URI_BASE + "/failed-sync/hash/{hash}/{token}")
    public ResponseEntity<String> changeStatusToFailedSync(@PathVariable String hash, @PathVariable String token) {
        if(!TOKEN.equals(token)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Token invalido.");
        }

        Boolean result = this.changeIntegrationStatusTaxDocumentService.changeStatusTaxDocumentByHash(hash, FileStatus.FAILED_SYNC);
        if(result) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Alterado com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Arquivo não encontrado.");

    }



}

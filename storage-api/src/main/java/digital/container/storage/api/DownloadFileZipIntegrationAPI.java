package digital.container.storage.api;

import digital.container.service.taxdocument.DownloadTaxDocumentService;
import digital.container.storage.domain.model.util.SearchScheduling;
import io.gumga.core.GumgaThreadScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/public/download-integration")
public class DownloadFileZipIntegrationAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTaxDocumentService.class);

    @Autowired
    private DownloadTaxDocumentService downloadTaxDocumentService;
    private static final String TOKEN = "2a33d41fd55246c54187c4d30d4b0aa55b6eb43d4ae0951886c1d34d95e5ce78";

    @RequestMapping(method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<Object> generateLinkToDownload(@RequestBody SearchScheduling searchScheduling,
                                                         @RequestParam(name = "integrationToken", defaultValue = "NO_TOKEN") String integrationToken){
        if(StringUtils.isEmpty(searchScheduling.getOrganizationCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O organization token não está preenchido.");
        }
        if(!TOKEN.equals(integrationToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token está invalido.");
        }
        GumgaThreadScope.organizationCode.set(searchScheduling.getOrganizationCode());
//        SearchScheduling searchScheduling = new SearchScheduling();
//        searchScheduling.addCnpj("01632317000103");
//        searchScheduling.addTaxDocumentScheduling(TaxDocumentScheduling.NFE);

        return ResponseEntity.status(HttpStatus.CREATED).body(downloadTaxDocumentService.generateLinkToDownload(searchScheduling));
    }
}

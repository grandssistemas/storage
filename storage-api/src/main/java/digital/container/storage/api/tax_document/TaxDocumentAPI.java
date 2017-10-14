package digital.container.storage.api.tax_document;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.util.SendDataFileHttpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@RestController
@RequestMapping(path = "/api/tax-document")
public class TaxDocumentAPI {

    private final SearchTaxDocumentService searchTaxDocumentService;
    private final SendDataFileHttpServlet sendDataFileHttpServlet;

    @Autowired
    public TaxDocumentAPI(SearchTaxDocumentService searchTaxDocumentService,
                          SendDataFileHttpServlet sendDataFileHttpServlet) {
        this.searchTaxDocumentService = searchTaxDocumentService;
        this.sendDataFileHttpServlet = sendDataFileHttpServlet;
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/access-key/{accessKey}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-accessKey", notes = "Visualizar taxdocument pelo accessKey")
    public void searchAccessKey(@ApiParam(value = "accessKey", required = true) @PathVariable String accessKey, HttpServletResponse httpServletResponse) {
        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(accessKey, Arrays.asList(FileType.NFE, FileType.NFCE));
        this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI,  httpServletResponse);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/access-key/canceled/{accessKey}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-accessKey-canceled", notes = "Visualizar taxdocument pelo accessKey")
    public void searchAccessKeyCanceled(@ApiParam(value = "accessKey", required = true) @PathVariable String accessKey, HttpServletResponse httpServletResponse) {
        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(accessKey, Arrays.asList(FileType.NFCE_CANCELED, FileType.NFE_CANCELED));
        this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI,  httpServletResponse);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/access-key/letter-correction/{accessKey}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-accessKey-letter-correction", notes = "Visualizar taxdocument pelo accessKey")
    public void searchAccessKeyLetterCorrection(@ApiParam(value = "accessKey", required = true) @PathVariable String accessKey, HttpServletResponse httpServletResponse) {
        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(accessKey, Arrays.asList(FileType.NFCE_LETTER_CORRECTION, FileType.NFE_LETTER_CORRECTION));
        this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI,  httpServletResponse);
    }
}

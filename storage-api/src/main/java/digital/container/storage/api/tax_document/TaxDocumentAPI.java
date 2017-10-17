package digital.container.storage.api.tax_document;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.util.SendDataFileHttpServlet;
import io.gumga.core.GumgaThreadScope;
import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@RestController
@RequestMapping(path = "/api/tax-document")
public class TaxDocumentAPI {

    private static final String TOKEN = "3dd4a8dbb0e8457cb273a05aea24ba61e894d1736181b0e8";
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




    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/access-key/{accessKey}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "public-file-accessKey", notes = "Visualizar taxdocument pelo accessKey")
    public void searchAccessKeyPulblic(@ApiParam(value = "accessKey", required = true) @PathVariable String accessKey,
                                       @ApiParam(value = "token", required = false) @RequestParam(name = "token", required = false) String token,
                                       HttpServletResponse httpServletResponse) {
        if(!TOKEN.equals(token)) {
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
            AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(accessKey, Arrays.asList(FileType.NFE, FileType.NFCE));
            this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI,  httpServletResponse);
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.FALSE);
        } else {
            throw new GumgaRunTimeException(HttpStatus.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/access-key/canceled/{accessKey}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "public-file-accessKey-canceled", notes = "Visualizar taxdocument pelo accessKey")
    public void searchAccessKeyCanceledPulblic(@ApiParam(value = "accessKey", required = true) @PathVariable String accessKey,
                                               @ApiParam(value = "token", required = false) @RequestParam(name = "token", required = false) String token,
                                               HttpServletResponse httpServletResponse) {
        if(!TOKEN.equals(token)) {
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
            AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(accessKey, Arrays.asList(FileType.NFCE_CANCELED, FileType.NFE_CANCELED));
            this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI, httpServletResponse);
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.FALSE);
        } else {
            throw new GumgaRunTimeException(HttpStatus.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/access-key/letter-correction/{accessKey}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "public-file-accessKey-letter-correction", notes = "Visualizar taxdocument pelo accessKey")
    public void searchAccessKeyLetterCorrectionPulblic(@ApiParam(value = "accessKey", required = true) @PathVariable String accessKey,
                                                       @ApiParam(value = "token", required = false) @RequestParam(name = "token", required = false) String token,
                                                       HttpServletResponse httpServletResponse) {
        if(!TOKEN.equals(token)) {
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
            AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(accessKey, Arrays.asList(FileType.NFCE_LETTER_CORRECTION, FileType.NFE_LETTER_CORRECTION));
            this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI, httpServletResponse);
            GumgaThreadScope.ignoreCheckOwnership.set(Boolean.FALSE);
        } else {
            throw new GumgaRunTimeException(HttpStatus.FORBIDDEN);
        }
    }
}

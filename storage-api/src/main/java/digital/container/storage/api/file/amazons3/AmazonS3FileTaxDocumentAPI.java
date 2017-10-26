package digital.container.storage.api.file.amazons3;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.service.file.amazons3.AmazonS3FileTaxDocumentService;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.api.ApiDocumentation;
import digital.container.storage.domain.model.file.FileType;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenUtil;
import digital.container.storage.util.SendDataFileHttpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping
@Transactional
public class AmazonS3FileTaxDocumentAPI {

    private static final String URI_BASE = "/api/amazons3-file/tax-document";


    private final AmazonS3FileTaxDocumentService amazonS3FileTaxDocumentService;
    private final SearchTaxDocumentService  searchTaxDocumentService;
    private final SendDataFileHttpServlet sendDataFileHttpServlet;

    @Autowired
    public AmazonS3FileTaxDocumentAPI(AmazonS3FileTaxDocumentService amazonS3FileTaxDocumentService,
                                      SearchTaxDocumentService searchTaxDocumentService,
                                      SendDataFileHttpServlet sendDataFileHttpServlet) {
        this.amazonS3FileTaxDocumentService = amazonS3FileTaxDocumentService;
        this.searchTaxDocumentService = searchTaxDocumentService;
        this.sendDataFileHttpServlet = sendDataFileHttpServlet;
    }


    @Transactional
    @RequestMapping(
            path = URI_BASE + "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "amazons3-file-tax-document-upload", notes = "Upload de arquivos que serão salvo na amazons3.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = ApiDocumentation.PARAM_CONTAINER_KEY, required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "file", value = ApiDocumentation.PARAM_FILE, required = true) @RequestPart(name = "file") MultipartFile multipartFile,
                                                @ApiParam(name = "tokenSoftwareHouse", value = ApiDocumentation.PARAM_TOKEN_SOFTWARE_HOUSE, required = false) @RequestParam(name = "tokenSoftwareHouse", required = false, defaultValue = TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN) String tokenSoftwareHouse,
                                                @ApiParam(name = "tokenAccountant", value = ApiDocumentation.PARAM_TOKEN_ACCOUNTANT, required = false) @RequestParam(name = "tokenAccountant", required = false, defaultValue = TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN) String tokenAccountant) {
        FileProcessed fileProcessed = this.amazonS3FileTaxDocumentService.processUpload(containerKey, multipartFile, tokenSoftwareHouse, tokenAccountant);
        if(!fileProcessed.getErrors().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fileProcessed);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(fileProcessed);
    }

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/uploads/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "amazons3-file-tax-document-uploads", notes = "Uploads de arquivos que serão salvo na amazons3. Limite maximo de 500.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<List<FileProcessed>> upload(@ApiParam(name = "containerKey", value = ApiDocumentation.PARAM_CONTAINER_KEY, required = true)@PathVariable String containerKey,
                                                      @ApiParam(name = "files", value = ApiDocumentation.PARAM_FILES, required = true)@RequestPart(name = "files") List<MultipartFile> multipartFiles,
                                                      @ApiParam(name = "tokenSoftwareHouse", value = ApiDocumentation.PARAM_TOKEN_SOFTWARE_HOUSE, required = false) @RequestParam(name = "tokenSoftwareHouse", required = false, defaultValue = TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN) String tokenSoftwareHouse,
                                                      @ApiParam(name = "tokenAccountant", value = ApiDocumentation.PARAM_TOKEN_ACCOUNTANT, required = false) @RequestParam(name = "tokenAccountant", required = false, defaultValue = TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN) String tokenAccountant) {
        List<FileProcessed> filesProcessed = this.amazonS3FileTaxDocumentService.processUpload(containerKey, multipartFiles, tokenSoftwareHouse, tokenAccountant);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = URI_BASE + "/detail-one/{detailOne}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-detailOne", notes = "Visualizar qualquer tipo de arquivo pelo detailOne")
    public void searchDetailOne(@ApiParam(value = "detailOne", required = true) @PathVariable String detailOne, HttpServletResponse httpServletResponse) {

        sendDataFileHttpServlet.send(this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(detailOne, Arrays.asList(FileType.NFE, FileType.NFCE)),
                httpServletResponse);
//        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndGumgaOI(detailOne);
//        if(taxDocumentByDetailOneAndGumgaOI != null) {
//            if(taxDocumentByDetailOneAndGumgaOI instanceof DatabaseFile) {
//                DatabaseFile df = (DatabaseFile) taxDocumentByDetailOneAndGumgaOI;
//                SendDataDatabaseFileHttpServlet.send(df, httpServletResponse, Boolean.FALSE);
//            } else {
//                if(taxDocumentByDetailOneAndGumgaOI instanceof DatabaseFile) {
//                    LocalFile lf = (LocalFile) taxDocumentByDetailOneAndGumgaOI;
//                    SendDataLocalFileHttpServlet.send(lf, httpServletResponse, Boolean.FALSE);
//                }
//            }
//        }
    }
}

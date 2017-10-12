package digital.container.storage.api.file.databasefile;

import com.wordnik.swagger.annotations.*;
import digital.container.service.file.databasefile.DatabaseFileTaxDocumentDisableService;
import digital.container.storage.api.ApiDocumentation;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
public class DatabaseFileTaxDocumentDisableAPI {
    private static final String URI_BASE = "/api/database-file/tax-document-disable";

    @Autowired
    private DatabaseFileTaxDocumentDisableService service;

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "database-file-tax-document-disable-upload", notes = "Upload de arquivos que serão salvo no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = FileProcessed.class),
            @ApiResponse(code = 400, message = "", response = FileProcessed.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = ApiDocumentation.PARAM_CONTAINER_KEY, required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "file", value = ApiDocumentation.PARAM_FILE, required = true) @RequestPart(name = "file") MultipartFile multipartFile,
                                                @ApiParam(name = "tokenSoftwareHouse", value = ApiDocumentation.PARAM_TOKEN_SOFTWARE_HOUSE, required = false) @RequestParam(name = "tokenSoftwareHouse", required = false, defaultValue = TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN) String tokenSoftwareHouse,
                                                @ApiParam(name = "tokenAccountant", value = ApiDocumentation.PARAM_TOKEN_ACCOUNTANT, required = false) @RequestParam(name = "tokenAccountant", required = false, defaultValue = TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN) String tokenAccountant) {
        FileProcessed fileProcessed = this.service.upload(containerKey, multipartFile, tokenSoftwareHouse, tokenAccountant);
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
    @ApiOperation(value = "database-file-tax-document-disable-uploads", notes = "Uploads de arquivos que serão salvo no banco de dados. Limite maximo de 500.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<List<FileProcessed>> upload(@ApiParam(name = "containerKey", value = ApiDocumentation.PARAM_CONTAINER_KEY, required = true)@PathVariable String containerKey,
                                                      @ApiParam(name = "files", value = ApiDocumentation.PARAM_FILES, required = true)@RequestPart(name = "files") List<MultipartFile> multipartFiles,
                                                      @ApiParam(name = "tokenSoftwareHouse", value = ApiDocumentation.PARAM_TOKEN_SOFTWARE_HOUSE, required = false) @RequestParam(name = "tokenSoftwareHouse", required = false, defaultValue = TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN) String tokenSoftwareHouse,
                                                      @ApiParam(name = "tokenAccountant", value = ApiDocumentation.PARAM_TOKEN_ACCOUNTANT, required = false) @RequestParam(name = "tokenAccountant", required = false, defaultValue = TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN) String tokenAccountant) {
        List<FileProcessed> filesProcessed = this.service.upload(containerKey, multipartFiles, tokenSoftwareHouse, tokenAccountant);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }
}

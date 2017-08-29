package digital.container.storage.api.databasefile;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.service.databasefile.DatabaseFileTaxDocumentAnyService;
import digital.container.service.databasefile.DatabaseFileTaxDocumentCanceledService;
import digital.container.storage.domain.model.file.vo.FileProcessed;
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
public class DatabaseFileTaxDocumentAnyAPI {

    private final static String URI_BASE = "/api/database-file/tax-document-any";

    @Autowired
    private DatabaseFileTaxDocumentAnyService service;

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "database-file-tax-document-any-upload", notes = "Upload de arquivos que serão salvo no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = FileProcessed.class),
            @ApiResponse(code = 400, message = "", response = FileProcessed.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "file", value = "O arquivo que será salvo no storage", required = true) @RequestPart(name = "file") MultipartFile multipartFile) {
        FileProcessed fileProcessed = this.service.upload(containerKey, multipartFile);
        if(fileProcessed.getErrors().size() > 0) {
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
    @ApiOperation(value = "database-file-tax-document-any-uploads", notes = "Uploads de arquivos que serão salvo no banco de dados. Limite maximo de 500.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<List<FileProcessed>> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true)@PathVariable String containerKey,
                                                      @ApiParam(name = "files", value = "Os arquivos que serão salvos no storage", required = true)@RequestPart(name = "files") List<MultipartFile> multipartFiles) {
        List<FileProcessed> filesProcessed = this.service.upload(containerKey, multipartFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }
}

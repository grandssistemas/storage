package digital.container.storage.api.localfile;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.service.localfile.LocalFileTaxDocumentService;
import digital.container.service.localfile.LocalFileTaxDocumentoCanceledService;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class LocalFileTaxDocumentoCanceledAPI {
    private final static String URI_BASE = "/api/local-file/tax-document-canceled";

    @Autowired
    private LocalFileTaxDocumentoCanceledService localFileService;

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "local-file-tax-document-upload", notes = "Upload de arquivo que será salvo localmente.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = FileProcessed.class),
            @ApiResponse(code = 400, message = "", response = FileProcessed.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "file", value = "O arquivo que será salvo no storage", required = true) @RequestPart(name = "file") MultipartFile multipartFile) {
        FileProcessed fileProcessed = this.localFileService.upload(containerKey, multipartFile);
        if(fileProcessed.getErrors().size() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fileProcessed);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(fileProcessed);
    }
}

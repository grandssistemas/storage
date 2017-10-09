package digital.container.storage.api.file.amazons3;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.service.file.amazons3.AmazonS3FileService;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/amazons3-file")
@Transactional
public class AmazonS3FileAPI {

    private final AmazonS3FileService amazonS3FileService;

    @Autowired
    public AmazonS3FileAPI(AmazonS3FileService amazonS3FileService) {
        this.amazonS3FileService = amazonS3FileService;
    }

    @Transactional
    @RequestMapping(
            path = "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "amazons3-file-upload", notes = "Upload de arquivo que será salvo na amazonS3.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = FileProcessed.class),
            @ApiResponse(code = 400, message = "", response = FileProcessed.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "shared", value = "Para enviar o arquivo como publico setar esse parametro como true", defaultValue = "false") @RequestParam(name = "shared", defaultValue = "false") boolean shared,
                                                @ApiParam(name = "file", value = "O arquivo que será salvo no storage", required = true) @RequestPart(name = "file") MultipartFile multipartFile) {
        this.amazonS3FileService.processUpload(containerKey, multipartFile, shared);
//        FileProcessed fileProcessed = this.localFileService.upload(containerKey, multipartFile, shared);
//
//        if(fileProcessed.getErrors().size() > 0) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fileProcessed);
//        }
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(fileProcessed);
        return null;
    }
}

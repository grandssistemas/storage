package digital.container.storage.api.localfile;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.util.SendDataLocalFileHttpServlet;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.service.localfile.LocalFileTaxDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping
public class LocalFileTaxDocumentAPI {

    private final static String URI_BASE = "/api/local-file/tax-document";

    @Autowired
    private LocalFileTaxDocumentService localFileService;

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

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/uploads/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "local-file-tax-document-uploads", notes = "Uploads de arquivos que serão salvos localmente.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = FileProcessed.class),
            @ApiResponse(code = 400, message = "", response = FileProcessed.class)
    })
    public ResponseEntity<List<FileProcessed>> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                      @ApiParam(name = "files", value = "Os arquivos que serão salvos no storage", required = true) @RequestPart(name = "files") List<MultipartFile> multipartFiles) {
        List<FileProcessed> filesProcessed = this.localFileService.upload(containerKey, multipartFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = URI_BASE + "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "local-file-hash", notes = "Visualizar o arquivo salvo no storage pelo hash.")
    public void download(@ApiParam(name = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        LocalFile result = this.localFileService.getFileHash(hash);
        SendDataLocalFileHttpServlet.send(result, httpServletResponse);
    }

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/hash/{hash}",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "local-file-remove-hash", notes = "Remover o arquivo salvo no storage pelo hash.")
    public ResponseEntity<Void> delete(@ApiParam(name = "hash", required = true) @PathVariable String hash) {
        if(this.localFileService.deleteFileByHash(hash)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/{id}",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "local-file-remove-hash", notes = "Remover o arquivo salvo no storage pelo id.")
    public ResponseEntity<Void> delete(@ApiParam(name = "id", required = true) @PathVariable Long id) {
        if(this.localFileService.deleteFileById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

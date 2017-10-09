package digital.container.storage.api.file.localfile;

import com.wordnik.swagger.annotations.*;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.util.SendDataLocalFileHttpServlet;
import digital.container.service.file.localfile.LocalFileService;
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
@RequestMapping(path = "/api/local-file")
public class LocalFileAPI {

    private final LocalFileService localFileService;

    @Autowired
    public LocalFileAPI(LocalFileService localFileService) {
        this.localFileService = localFileService;
    }

    @Transactional
    @RequestMapping(
            path = "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "local-file-upload", notes = "Upload de arquivo que será salvo localmente.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = FileProcessed.class),
            @ApiResponse(code = 400, message = "", response = FileProcessed.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "shared", value = "Para enviar o arquivo como publico setar esse parametro como true", defaultValue = "false") @RequestParam(name = "shared", defaultValue = "false") boolean shared,
                                                @ApiParam(name = "file", value = "O arquivo que será salvo no storage", required = true) @RequestPart(name = "file") MultipartFile multipartFile) {
        FileProcessed fileProcessed = this.localFileService.upload(containerKey, multipartFile, shared);

        if(fileProcessed.getErrors().size() > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(fileProcessed);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(fileProcessed);
    }

    @Transactional
    @RequestMapping(
            path = "/uploads/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "local-file-uploads", notes = "Uploads de arquivos que serão salvos localmente. Limite maximo de 500.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<List<FileProcessed>> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                      @ApiParam(name = "shared", value = "Para enviar o arquivo como publico setar esse parametro como true", defaultValue = "false") @RequestParam(name = "shared", defaultValue = "false") boolean shared,
                                                      @ApiParam(name = "files", value = "Os arquivos que serão salvos no storage", required = true) @RequestPart(name = "files") List<MultipartFile> multipartFiles) {
        List<FileProcessed> filesProcessed = this.localFileService.upload(containerKey, multipartFiles, shared);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "local-file-hash", notes = "Visualizar o arquivo salvo no storage pelo hash.")
    public void view(@ApiParam(name = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        LocalFile result = this.localFileService.getFileHash(hash);
        SendDataLocalFileHttpServlet.send(result, httpServletResponse, Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/download/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "download-local-file-hash", notes = "Download o arquivo salvo no storage pelo hash.")
    public void download(@ApiParam(name = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        LocalFile result = this.localFileService.getFileHash(hash);
        SendDataLocalFileHttpServlet.send(result, httpServletResponse, Boolean.TRUE);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "local-file-public-hash", notes = "Visualizar o arquivo publico salvo no storage pelo hash.")
    public void downloadShared(@ApiParam(name = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        LocalFile result = this.localFileService.getFileHash(hash, Boolean.TRUE);
        SendDataLocalFileHttpServlet.send(result, httpServletResponse, Boolean.FALSE);
    }

    @Transactional
    @RequestMapping(
            path = "/hash/{hash}",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "local-file-remove-hash", notes = "Remover o arquivo salvo no storage pelo hash.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 404, message = "")
    })
    public ResponseEntity<Void> delete(@ApiParam(name = "id", required = true) @PathVariable String hash) {
        if(this.localFileService.deleteFileByHash(hash)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    @RequestMapping(
            path = "/{id}",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "local-file-remove-id", notes = "Remover o arquivo salvo no storage pelo id.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 404, message = "")
    })
    public ResponseEntity<Void> delete(@ApiParam(name = "id", required = true) @PathVariable Long id) {
        if(this.localFileService.deleteFileById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

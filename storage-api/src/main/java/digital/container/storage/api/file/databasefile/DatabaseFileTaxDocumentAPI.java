package digital.container.storage.api.file.databasefile;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.util.SendDataDatabaseFileHttpServlet;
import digital.container.service.file.databasefile.DatabaseFileTaxDocumentService;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.TokenUtil;
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
public class DatabaseFileTaxDocumentAPI {

    private final static String URI_BASE = "/api/database-file/tax-document";

    @Autowired
    private DatabaseFileTaxDocumentService databaseFileService;

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "database-file-tax-document-upload", notes = "Upload de arquivos que serão salvo no banco de dados.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<FileProcessed> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true) @PathVariable String containerKey,
                                                @ApiParam(name = "file", value = "O arquivo que será salvo no storage", required = true) @RequestPart(name = "file") MultipartFile multipartFile,
                                                @ApiParam(name = "tokenSoftwareHouse", required = false) @RequestParam(name = "tokenSoftwareHouse", required = false, defaultValue = TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN) String tokenSoftwareHouse,
                                                @ApiParam(name = "tokenAccountant", required = false) @RequestParam(name = "tokenAccountant", required = false, defaultValue = TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN) String tokenAccountant) {
        FileProcessed fileProcessed = this.databaseFileService.upload(containerKey, multipartFile, tokenSoftwareHouse, tokenAccountant);
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
    @ApiOperation(value = "database-file-tax-document-uploads", notes = "Uploads de arquivos que serão salvo no banco de dados. Limite maximo de 500.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "", response = List.class),
            @ApiResponse(code = 400, message = "", response = List.class)
    })
    public ResponseEntity<List<FileProcessed>> upload(@ApiParam(name = "containerKey", value = "A chave cadastrada no storage no endpoint: /api/permission-container", required = true)@PathVariable String containerKey,
                                                      @ApiParam(name = "files", value = "Os arquivos que serão salvos no storage", required = true)@RequestPart(name = "files") List<MultipartFile> multipartFiles,
                                                      @ApiParam(name = "tokenSoftwareHouse", required = false) @RequestParam(name = "tokenSoftwareHouse", required = false, defaultValue = TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN) String tokenSoftwareHouse,
                                                      @ApiParam(name = "tokenAccountant", required = false) @RequestParam(name = "tokenAccountant", required = false, defaultValue = TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN) String tokenAccountant) {
        List<FileProcessed> filesProcessed = this.databaseFileService.upload(containerKey, multipartFiles, tokenSoftwareHouse, tokenAccountant);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = URI_BASE + "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "database-file-tax-document-hash", notes = "Visualizar o arquivo salvo no storage pelo hash.")
    public void view(@ApiParam(name = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        DatabaseFile result = this.databaseFileService.getFileHash(hash);
        SendDataDatabaseFileHttpServlet.send(result, httpServletResponse, Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = URI_BASE + "/download/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "download-database-file-tax-document-hash", notes = "Download o arquivo salvo no storage pelo hash.")
    public void download(@ApiParam(name = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        DatabaseFile result = this.databaseFileService.getFileHash(hash);
        SendDataDatabaseFileHttpServlet.send(result, httpServletResponse, Boolean.TRUE);
    }

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/hash/{hash}",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "database-file-tax-document-remove-hash", notes = "Remover o arquivo salvo no storage pelo hash.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 404, message = "")
    })
    public ResponseEntity<Void> delete(@ApiParam(name = "hash", required = true) @PathVariable String hash) {
        if(this.databaseFileService.deleteFileByHash(hash)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/{id}",
            method = RequestMethod.DELETE
    )
    @ApiOperation(value = "database-file-tax-document-remove-id", notes = "Remover o arquivo salvo no storage pelo id.")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = ""),
            @ApiResponse(code = 404, message = "")
    })
    public ResponseEntity<Void> delete(@ApiParam(name = "id", required = true) @PathVariable Long id) {
        if(this.databaseFileService.deleteFileById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

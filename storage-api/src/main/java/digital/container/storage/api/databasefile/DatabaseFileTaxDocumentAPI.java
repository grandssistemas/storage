package digital.container.storage.api.databasefile;

import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.util.SendDataDatabaseFileHttpServlet;
import digital.container.service.databasefile.DatabaseFileTaxDocumentService;
import digital.container.storage.domain.model.file.vo.FileProcessed;
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
    public ResponseEntity<FileProcessed> upload(@PathVariable String containerKey, @RequestPart(name = "file") MultipartFile multipartFile) {
        FileProcessed fileProcessed = this.databaseFileService.upload(containerKey, multipartFile);
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
    public ResponseEntity<List<FileProcessed>> upload(@PathVariable String containerKey, @RequestPart(name = "files") List<MultipartFile> multipartFiles) {
        List<FileProcessed> filesProcessed = this.databaseFileService.upload(containerKey, multipartFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = URI_BASE + "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void download(@PathVariable String hash, HttpServletResponse httpServletResponse) {
        DatabaseFile result = this.databaseFileService.getFileHash(hash);
        SendDataDatabaseFileHttpServlet.send(result, httpServletResponse);
    }

    @Transactional
    @RequestMapping(
            path = URI_BASE + "/hash/{hash}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> delete(@PathVariable String hash) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if(this.databaseFileService.deleteFileById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

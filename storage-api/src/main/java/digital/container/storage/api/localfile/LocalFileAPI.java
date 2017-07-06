package digital.container.storage.api.localfile;

import digital.container.storage.domain.model.LocalFile;
import digital.container.storage.domain.model.vo.FileProcessed;
import digital.container.storage.util.SendDataLocalFileHttpServlet;
import digital.container.service.localfile.LocalFileService;
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

    @Autowired
    private LocalFileService localFileService;

    @Transactional
    @RequestMapping(
            path = "/upload/{containerKey}",
            method = RequestMethod.POST,
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<FileProcessed> upload(@PathVariable String containerKey,
                                                @RequestParam(name = "shared", defaultValue = "false") boolean shared,
                                                @RequestPart(name = "file") MultipartFile multipartFile) {
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
    public ResponseEntity<List<FileProcessed>> upload(@PathVariable String containerKey,
                                                      @RequestParam(name = "shared", defaultValue = "false") boolean shared,
                                                      @RequestPart(name = "files") List<MultipartFile> multipartFiles) {
        List<FileProcessed> filesProcessed = this.localFileService.upload(containerKey, multipartFiles, shared);
        return ResponseEntity.status(HttpStatus.CREATED).body(filesProcessed);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void download(@PathVariable String hash, HttpServletResponse httpServletResponse) {
        LocalFile result = this.localFileService.getFileHash(hash);
        SendDataLocalFileHttpServlet.send(result, httpServletResponse);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void downloadShared(@PathVariable String hash, HttpServletResponse httpServletResponse) {
        LocalFile result = this.localFileService.getFileHash(hash, Boolean.TRUE);
        SendDataLocalFileHttpServlet.send(result, httpServletResponse);
    }

    @Transactional
    @RequestMapping(
            path = "/hash/{hash}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> delete(@PathVariable String hash) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if(this.localFileService.deleteFileById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

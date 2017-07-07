package digital.container.storage.api;

import digital.container.repository.DatabaseFileRepository;
import digital.container.repository.LocalFileRepository;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.util.SendDataDatabaseFileHttpServlet;
import digital.container.storage.util.SendDataLocalFileHttpServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/file-hash")
public class HashAPI {

    @Autowired
    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    private LocalFileRepository localFileRepository;

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void download(@PathVariable String hash, HttpServletResponse httpServletResponse) {
        Optional<DatabaseFile> df = this.databaseFileRepository.getByHash(hash);
        if(df.isPresent()) {
            SendDataDatabaseFileHttpServlet.send(df.get(), httpServletResponse);
        } else {
            Optional<LocalFile> lf = this.localFileRepository.getByHash(hash);
            if(lf.isPresent()){
                SendDataLocalFileHttpServlet.send(lf.get(), httpServletResponse);
            }
        }
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void downloadPublic(@PathVariable String hash, HttpServletResponse httpServletResponse) {
        Optional<DatabaseFile> df = this.databaseFileRepository.getByHash(hash, Boolean.TRUE);
        if(df.isPresent()) {
            SendDataDatabaseFileHttpServlet.send(df.get(), httpServletResponse);
        } else {
            Optional<LocalFile> lf = this.localFileRepository.getByHash(hash, Boolean.TRUE);
            if(lf.isPresent()){
                SendDataLocalFileHttpServlet.send(lf.get(), httpServletResponse);
            }
        }
    }

}

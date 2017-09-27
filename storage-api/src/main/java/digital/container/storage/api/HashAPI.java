package digital.container.storage.api;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import digital.container.repository.DatabaseFileRepository;
import digital.container.repository.LocalFileRepository;
import digital.container.service.taxdocument.DownloadTaxDocumentService;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.file.*;
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
import java.util.*;

@RestController
@RequestMapping(path = "/api/file-hash")
public class HashAPI {

//    @Autowired
//    JmsTemplate jmsTemplate;
    private static final String TOKEN = "3cb73f59eb02-479b-b859-797e29eb8256-90703973edf5aa2d";

    @Autowired
    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    private LocalFileRepository localFileRepository;

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private DownloadTaxDocumentService downloadTaxDocumentService;

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-hash", notes = "Visualizar qualquer tipo de arquivo pelo hash")
    public void download(@ApiParam(value = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        sendFile(hash, httpServletResponse);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-hash-public", notes = "Visualizar qualquer tipo de arquivo publico pelo hash")
    public void downloadPublic(@ApiParam(value = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
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

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/public-integrate/{hash}/{token}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "public-integrate", notes = "Visualizar qualquer tipo de arquivo pelo hash e token")
    public void integrate(@ApiParam(value = "hash", required = true) @PathVariable String hash, @PathVariable String token, HttpServletResponse httpServletResponse) {
        if(!TOKEN.equals(token)) {
            throw new RuntimeException("Token invalido.");
        }

        sendFile(hash, httpServletResponse);
    }

    private void sendFile(String hash, HttpServletResponse httpServletResponse) {
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
            path = "/detail-one/{detailOne}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-detailOne", notes = "Visualizar qualquer tipo de arquivo pelo detailOne")
    public void searchDetailOne(@ApiParam(value = "detailOne", required = true) @PathVariable String detailOne, HttpServletResponse httpServletResponse) {
        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndGumgaOI(detailOne);
        if(taxDocumentByDetailOneAndGumgaOI != null) {
            if(taxDocumentByDetailOneAndGumgaOI instanceof DatabaseFile) {
                DatabaseFile df = (DatabaseFile) taxDocumentByDetailOneAndGumgaOI;
                SendDataDatabaseFileHttpServlet.send(df, httpServletResponse);
            } else {
                LocalFile lf = (LocalFile) taxDocumentByDetailOneAndGumgaOI;
                SendDataLocalFileHttpServlet.send(lf, httpServletResponse);
            }
        }
    }







//    @RequestMapping
//    public void teste() {
//        Map map = new HashMap<>();
//        map.put("model", "55");
//        map.put("hash", "1500213312d3c3fac497df4521a7cf9f454e05db02DF");
//        this.jmsTemplate.convertAndSend(map);
//    }

}

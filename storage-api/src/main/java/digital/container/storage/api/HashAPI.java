package digital.container.storage.api;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import digital.container.repository.file.DatabaseFileRepository;
import digital.container.repository.file.LocalFileRepository;
import digital.container.service.download.LinkDownloadService;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.download.LinkDownload;
import digital.container.storage.domain.model.file.*;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.util.IntegrationTokenUtil;
import digital.container.storage.util.SendDataDatabaseFileHttpServlet;
import digital.container.storage.util.SendDataLocalFileHttpServlet;
import digital.container.storage.domain.model.util.TokenUtil;
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

    private final DatabaseFileRepository databaseFileRepository;
    private final LocalFileRepository localFileRepository;
    private final SearchTaxDocumentService searchTaxDocumentService;
    private final LinkDownloadService linkDownloadService;

    @Autowired
    public HashAPI(LinkDownloadService linkDownloadService,
                   SearchTaxDocumentService searchTaxDocumentService,
                   LocalFileRepository localFileRepository,
                   DatabaseFileRepository databaseFileRepository) {
        this.linkDownloadService = linkDownloadService;
        this.searchTaxDocumentService = searchTaxDocumentService;
        this.localFileRepository = localFileRepository;
        this.databaseFileRepository = databaseFileRepository;
    }


    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "file-hash", notes = "Visualizar qualquer tipo de arquivo pelo hash")
    public void view(@ApiParam(value = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        sendFile(hash, httpServletResponse, Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    @RequestMapping(
            path = "/download/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "download-file-hash", notes = "Download qualquer tipo de arquivo pelo hash")
    public void download(@ApiParam(value = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        sendFile(hash, httpServletResponse, Boolean.TRUE);
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
            SendDataDatabaseFileHttpServlet.send(df.get(), httpServletResponse, Boolean.FALSE);
        } else {
            Optional<LocalFile> lf = this.localFileRepository.getByHash(hash, Boolean.TRUE);
            if(lf.isPresent()){
                SendDataLocalFileHttpServlet.send(lf.get(), httpServletResponse, Boolean.FALSE);
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
        if(!IntegrationTokenUtil.PUBLIC_TOKEN_INTEGRATION.equals(token)) {
            throw new RuntimeException("Token invalido.");
        }

        sendFile(hash, httpServletResponse, Boolean.FALSE);
    }

    private void sendFile(String hash, HttpServletResponse httpServletResponse, Boolean download) {
        Optional<DatabaseFile> df = this.databaseFileRepository.getByHash(hash, TokenUtil.getEndWithOi(), TokenUtil.getContainsSharedOi());
        if(df.isPresent()) {
            SendDataDatabaseFileHttpServlet.send(df.get(), httpServletResponse, download);
        } else {
            Optional<LocalFile> lf = this.localFileRepository.getByHash(hash, TokenUtil.getEndWithOi(), TokenUtil.getContainsSharedOi());
            if(lf.isPresent()){
                SendDataLocalFileHttpServlet.send(lf.get(), httpServletResponse, download);
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
        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(detailOne, Arrays.asList(FileType.NFE, FileType.NFCE));
        if(taxDocumentByDetailOneAndGumgaOI != null) {
            if(taxDocumentByDetailOneAndGumgaOI instanceof DatabaseFile) {
                DatabaseFile df = (DatabaseFile) taxDocumentByDetailOneAndGumgaOI;
                SendDataDatabaseFileHttpServlet.send(df, httpServletResponse, Boolean.FALSE);
            } else {
                if(taxDocumentByDetailOneAndGumgaOI instanceof DatabaseFile) {
                    LocalFile lf = (LocalFile) taxDocumentByDetailOneAndGumgaOI;
                    SendDataLocalFileHttpServlet.send(lf, httpServletResponse, Boolean.FALSE);
                }
            }
        }
    }


    @Transactional
    @RequestMapping(
            path = "/public-download/{hash}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiOperation(value = "public-download", notes = "Baixa o zip do agendamento do container digital")
    public void downloadFileZip(@ApiParam(value = "hash", required = true) @PathVariable String hash, HttpServletResponse httpServletResponse) {
        LinkDownload linkDownload = linkDownloadService.getLinkDownloadByHash(hash);
        if(linkDownload != null) {
            SendDataLocalFileHttpServlet.send(linkDownload, httpServletResponse);
        }
    }

}

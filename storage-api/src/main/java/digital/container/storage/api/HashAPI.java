package digital.container.storage.api;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import digital.container.repository.file.DatabaseFileRepository;
import digital.container.repository.file.LocalFileRepository;
import digital.container.service.download.LinkDownloadService;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.download.LinkDownload;
import digital.container.storage.domain.model.file.*;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import digital.container.storage.domain.model.util.IntegrationTokenUtil;
import digital.container.storage.util.SendDataFileHttpServlet;
import digital.container.storage.util.SendDataLocalFileHttpServlet;
import io.gumga.core.GumgaThreadScope;
import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import okhttp3.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/api/file-hash")
public class HashAPI {

    private final SearchTaxDocumentService searchTaxDocumentService;
    private final LinkDownloadService linkDownloadService;
    private final SendDataFileHttpServlet sendDataFileHttpServlet;
    @Autowired
    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    public HashAPI(LinkDownloadService linkDownloadService,
                   SearchTaxDocumentService searchTaxDocumentService,
                   LocalFileRepository localFileRepository,
                   DatabaseFileRepository databaseFileRepository,
                   SendDataFileHttpServlet sendDataFileHttpServlet) {
        this.linkDownloadService = linkDownloadService;
        this.searchTaxDocumentService = searchTaxDocumentService;
        this.sendDataFileHttpServlet = sendDataFileHttpServlet;
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
        AbstractFile fileByHashAndPublic = this.searchTaxDocumentService.getFileByHashAndPublic(hash);
        sendDataFileHttpServlet.send(fileByHashAndPublic, httpServletResponse);
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
        GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
        sendFile(hash, httpServletResponse, Boolean.FALSE);
        GumgaThreadScope.ignoreCheckOwnership.set(Boolean.FALSE);
    }

    private void sendFile(String hash, HttpServletResponse httpServletResponse, Boolean download) {
        AbstractFile fileByHash = searchTaxDocumentService.getFileByHash(hash);
        if(fileByHash != null) {
            this.sendDataFileHttpServlet.send(fileByHash, httpServletResponse);
        } else {
            throw new GumgaRunTimeException("File not found with this hash: " + hash, HttpStatus.NOT_FOUND);
        }
    }

//    @Transactional(readOnly = true)
//    @RequestMapping(
//            path = "/detail-one/{detailOne}",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
//    )
//    @ApiOperation(value = "file-detailOne", notes = "Visualizar qualquer tipo de arquivo pelo detailOne")
//    public void searchDetailOne(@ApiParam(value = "detailOne", required = true) @PathVariable String detailOne, HttpServletResponse httpServletResponse) {
//        AbstractFile taxDocumentByDetailOneAndGumgaOI = this.searchTaxDocumentService.getTaxDocumentByDetailOneAndFileTypes(detailOne, Arrays.asList(FileType.NFE, FileType.NFCE));
//        this.sendDataFileHttpServlet.send(taxDocumentByDetailOneAndGumgaOI,  httpServletResponse);
//    }


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


    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .build();

    @Transactional
    @RequestMapping(path = "/public/migration")
    public Boolean integrate() throws IOException {
        GumgaThreadScope.ignoreCheckOwnership.set(Boolean.TRUE);
        for (DatabaseFile databaseFile : databaseFileRepository.findAll()) {
            if(FileType.ANYTHING.equals(databaseFile.getFileType()) && (databaseFile.getMigration() == null || !databaseFile.getMigration())) {
                Hibernate.initialize(databaseFile.getParts());
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

                File file = new File(databaseFile.getName());
                FileOutputStream fos = new FileOutputStream(file);
                for (DatabaseFilePart databaseFilePart : databaseFile.getParts()) {
                    fos.write(databaseFilePart.getRawBytes());
                }
                fos.close();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", databaseFile.getName(), RequestBody.create(okhttp3.MediaType.parse(MediaType.IMAGE_PNG_VALUE), file))
                        .build();
                Request build = new Request.Builder()
                        .url(String.format("http://localhost:8085/storage-api/api/v2/file/public/upload/%s/migration/%s/%s/%s?shared=true&storageType=DATABASE", databaseFile.getContainerKey(), databaseFile.getId(), databaseFile.getOi(), databaseFile.getHash()))
                        .post(requestBody)
                        .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                        .build();

                Response execute = client.newCall(build).execute();
                execute.close();
            }
            databaseFile.setMigration(Boolean.TRUE);


//            filesGroup.forEach(file -> builder.addFormDataPart("files", file.getName(), RequestBody.create(MediaType.parse(mediaType), file)));

        }
        GumgaThreadScope.ignoreCheckOwnership.set(Boolean.FALSE);
        return Boolean.TRUE;
    }

}

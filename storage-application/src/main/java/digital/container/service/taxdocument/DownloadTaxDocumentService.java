package digital.container.service.taxdocument;

import digital.container.service.download.LinkDownloadService;
import digital.container.service.file.amazons3.AmazonS3Service;
import digital.container.storage.domain.model.download.LinkDownload;
import digital.container.storage.domain.model.file.*;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.util.AmazonS3Util;
import digital.container.storage.domain.model.util.GenerateHash;
import digital.container.storage.domain.model.util.SearchScheduling;
import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class DownloadTaxDocumentService {

    public static final String DIRECTORY_PATH = System.getProperty("storage.foldertemp");
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadTaxDocumentService.class);

    private final SearchTaxDocumentService searchTaxDocumentService;
    private final LinkDownloadService linkDownloadService;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    public DownloadTaxDocumentService(SearchTaxDocumentService searchTaxDocumentService,
                                      LinkDownloadService linkDownloadService,
                                      AmazonS3Service amazonS3Service) {
        this.searchTaxDocumentService = searchTaxDocumentService;
        this.linkDownloadService = linkDownloadService;
        this.amazonS3Service = amazonS3Service;
    }

    public LinkDownload generateLinkToDownload(SearchScheduling searchScheduling) {
        String nameZip = "download";
        String path = this.generateZip(searchScheduling, nameZip);

        LinkDownload linkDownload = new LinkDownload();
        linkDownload.setHash(GenerateHash.generateDownload());
        linkDownload.generateDescription(searchScheduling);
        linkDownload.setName(nameZip + ".zip");
        linkDownload.setRelativePath(path);

        File file = new File(linkDownload.getRelativePath() + linkDownload.getName());
        linkDownload.setSize(file.length());

        return this.linkDownloadService.save(linkDownload);
    }

    private String generateZip(SearchScheduling searchScheduling, String nameZip) {

        List<AbstractFile> taxDocumentBySearchScheduling = this.searchTaxDocumentService.getTaxDocumentBySearchScheduling(searchScheduling);
        String path = getDirectoryPath();
        taxDocumentBySearchScheduling
                .forEach(file -> {
                    if(file instanceof DatabaseFile) {
                        DatabaseFile df = (DatabaseFile) file;

                        String pathFile = null;

                        if(df.getRelativePath() != null) {
                            pathFile = path + df.getRelativePath().substring(0, df.getRelativePath().lastIndexOf('/')+1);
                        } else {
                            pathFile = path + getFileName(file);
                        }

                        new File(pathFile).mkdirs();

                        File outputFile = new File(pathFile + df.getName());

                        try (FileOutputStream outputStream = new FileOutputStream(outputFile); ) {
                            Hibernate.initialize(df.getParts());
                            for (DatabaseFilePart databaseFilePart : df.getParts()) {
                                outputStream.write(databaseFilePart.getRawBytes());
                                outputStream.flush();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            outputFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        if(file instanceof LocalFile) {
                            LocalFile lf = (LocalFile) file;
                            try {
                                FileUtils.copyFile(new File(LocalFileUtil.DIRECTORY_PATH + "/" + lf.getRelativePath()), new File(path + lf.getRelativePath()));
                            } catch (IOException e) {
                                LOGGER.error("Erro ao tentar copiar o arquivo para a pasta de download." + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            AmazonS3File amazonS3File = (AmazonS3File) file;
                            amazonS3Service.getFile(AmazonS3Util.TAX_DOCUMENT_BUCKET, path, amazonS3File.getRelativePath());
                        }
                    }
                });

        if(taxDocumentBySearchScheduling.size() > 0 ) {
            ZipOutputStream zipOutputStream = null;
            String zipFileName = getDirectoryPath();
            new File(zipFileName).mkdirs();

            try {

                zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName + nameZip + ".zip"));
                ZipUtil.zipFolder(new File(path), zipOutputStream, "");
            } catch (Exception e) {
                LOGGER.error("Erro ao tentar zipar os arquivos." + e.getMessage());
            } finally {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Erro ao fechar o zipOutputStream." + e.getMessage());
                }
            }

            try {
                FileUtils.deleteDirectory(new File(path));
            } catch (IOException e) {
                LOGGER.error("Erro ao remover a pasta dos arquivos gerados para serem zipados." + e.getMessage());
            }

            return zipFileName;
        }

        throw new RuntimeException("Nao existe arquivo");
    }

    private String getDirectoryPath() {
        return DIRECTORY_PATH + "/" +
                UUID.randomUUID().toString().replaceAll("-", "") + "/";
    }

    private String getFileName(AbstractFile file) {

        String path =
                file.getContainerKey() + "/"+
                file.getFileType().toString() + "/";

        if(FileType.NFCE.equals(file.getFileType()) || FileType.NFE.equals(file.getFileType())) {
            path += file.getDetailThree() + "/";
        }

        return path;
    }

}

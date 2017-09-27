package digital.container.service.taxdocument;

import digital.container.storage.domain.model.download.LinkDownload;
import digital.container.storage.domain.model.file.*;
import digital.container.util.GenerateHash;
import digital.container.storage.domain.model.util.SearchScheduling;
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

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    public LinkDownload generateLinkToDownload(SearchScheduling searchScheduling) {
        String nameZip = "download";
        String path = this.generateZip(searchScheduling, nameZip);

        LinkDownload linkDownload = new LinkDownload();
        linkDownload.setHash(GenerateHash.generateDownload());
        linkDownload.generateDescription(searchScheduling);
        linkDownload.setName(nameZip);
        linkDownload.setRelativePath(path);

        return linkDownload;
    }

    private String generateZip(SearchScheduling searchScheduling, String nameZip) {

        List<AbstractFile> taxDocumentBySearchScheduling = this.searchTaxDocumentService.getTaxDocumentBySearchScheduling(searchScheduling);
        String path = getDirectoryPath();
        taxDocumentBySearchScheduling
                .forEach(file -> {
                    if(file instanceof DatabaseFile) {
                        DatabaseFile df = (DatabaseFile) file;

                        String pathFile = path + getFileName(file);
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
                        LocalFile lf = (LocalFile) file;

                    }
                });


        ZipOutputStream zipOutputStream = null;
        String zipFileName = getDirectoryPath();
        new File(zipFileName).mkdirs();

        try {

            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName+nameZip+".zip"));
            ZipUtil.zipFolder(new File(path), zipOutputStream, "");
        } catch (Exception e) {
            LOGGER.error("Erro ao tentar zipar os arquivos."+e.getMessage());
        } finally {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                LOGGER.error("Erro ao fechar o zipOutputStream."+e.getMessage());
            }
        }

        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            LOGGER.error("Erro ao remover a pasta dos arquivos gerados para serem zipados."+e.getMessage());
        }

        return zipFileName;
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

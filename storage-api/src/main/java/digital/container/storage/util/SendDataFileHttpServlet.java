package digital.container.storage.util;


import digital.container.service.file.amazons3.AmazonS3Service;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.util.AmazonS3Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Component
public class SendDataFileHttpServlet {

    private final AmazonS3Service amazonS3Service;

    @Autowired
    public SendDataFileHttpServlet(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }


    public void send(AbstractFile file, HttpServletResponse httpServletResponse) {
        if (file != null) {
            if (file instanceof DatabaseFile) {
                DatabaseFile df = (DatabaseFile) file;
                SendDataDatabaseFileHttpServlet.send(df, httpServletResponse, Boolean.FALSE);
            } else {
                if (file instanceof DatabaseFile) {
                    LocalFile lf = (LocalFile) file;
                    SendDataLocalFileHttpServlet.send(lf, httpServletResponse, Boolean.FALSE);
                } else {
                    AmazonS3File s3File = (AmazonS3File) file;
                    File amazonFile = amazonS3Service.getFile(AmazonS3Util.TAX_DOCUMENT_BUCKET, System.getProperty("storage.foldertemp"), file.getRelativePath(), file.getName());
                    SendDataAmazonS3FileHttpServlet.send(s3File, amazonFile, httpServletResponse, Boolean.FALSE);
                }
            }
        }
    }
}

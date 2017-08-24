package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.GenerateHash;
import digital.container.util.LocalFileUtil;
import digital.container.util.ValidateNfXML;
import digital.container.util.XMLUtil;
import digital.container.vo.TaxDocumentModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

@Service
@Transactional
public class CommonTaxDocumentService {

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey) {
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        String xml = XMLUtil.getXml(multipartFile);

        TaxDocumentModel taxDocumentModel = new TaxDocumentModel();
        FileProcessed errors = ValidateNfXML.validate(containerKey, multipartFile, file, taxDocumentModel);
        if (errors != null) {
            return errors;
        }

        file.setFileType(taxDocumentModel.getFileType());

        if(file instanceof LocalFile) {
            Date date = ValidateNfXML.stringToDate(file.getDetailTwo());
            LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String path = LocalFileUtil.getRelativePathFileTAXDOCUMENT(containerKey,
                    ld.getYear(),
                    ld.getMonth().toString(),
                    file.getFileType(),
                    file.getDetailThree());

            ((LocalFile)file).setRelativePath(path + '/' + file.getName());
            file.setHash(GenerateHash.generateLocalFile());
        } else {
            file.setHash(GenerateHash.generateDatabaseFile());
        }

        file.setContainerKey(containerKey);
        file.setCreateDate(Calendar.getInstance());

        file.setContentType(multipartFile.getContentType());
        file.setSize(multipartFile.getSize());


        return new FileProcessed(file, Collections.EMPTY_LIST);
    }
}

package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.GenerateHash;
import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.util.*;
import digital.container.vo.TaxDocumentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

@Service
@Transactional
public class CommonTaxDocumentService {

    @Autowired
    private ValidateNfXML validateNfXML;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy) {
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        String xml = XMLUtil.getXml(multipartFile);

        TaxDocumentModel taxDocumentModel = new TaxDocumentModel();
        FileProcessed errors = validateNfXML.validate(containerKey, multipartFile, file, taxDocumentModel);
        if (errors != null) {
            return errors;
        }

        if(!TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN.equals(tokenResultProxy.accountantOi)) {
            file.addOrganization(tokenResultProxy.accountantOi);
        }

        if(!TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN.equals(tokenResultProxy.softwareHouseOi)) {
            file.addOrganization(tokenResultProxy.softwareHouseOi);
        }

        file.setFileType(taxDocumentModel.getFileType());

        Date date = validateNfXML.stringToDate(file.getDetailTwo());
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENT(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                file.getFileType(),
                file.getDetailThree());

        if(file instanceof LocalFile) {
            ((LocalFile)file).setRelativePath(path + '/' + file.getName());
            file.setHash(GenerateHash.generateLocalFile());
        } else {
            ((DatabaseFile)file).setRelativePath(path + '/' + file.getName());
            file.setHash(GenerateHash.generateDatabaseFile());
        }

        file.setContainerKey(containerKey);
        file.setCreateDate(Calendar.getInstance());

        file.setContentType(multipartFile.getContentType());
        file.setSize(multipartFile.getSize());


        return new FileProcessed(file, Collections.EMPTY_LIST);
    }
}

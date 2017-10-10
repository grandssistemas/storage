package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.*;
import digital.container.vo.TaxDocumentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
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

        TaxDocumentModel taxDocumentModel = new TaxDocumentModel();
        FileProcessed errors = validateNfXML.validate(containerKey, multipartFile, file, taxDocumentModel);
        if (errors != null) {
            return errors;
        }

        Date date = validateNfXML.stringToDate(file.getDetailTwo());
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        file.buildTaxDocument(multipartFile.getContentType(), multipartFile.getSize(), Boolean.FALSE, containerKey, tokenResultProxy, ld, taxDocumentModel.getFileType());

        return new FileProcessed(file, Collections.emptyList());
    }
}

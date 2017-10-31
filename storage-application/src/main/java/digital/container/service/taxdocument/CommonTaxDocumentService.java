package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.vo.FileProcessed;
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

    private final CommonTaxDocumentEventCanceledService commonTaxCocumentEventService;
    private final CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService;
    private final CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService;
    private final ValidateNfXML validateNfXML;

    @Autowired
    public CommonTaxDocumentService(CommonTaxDocumentEventCanceledService commonTaxCocumentEventService,
                                    CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService,
                                    CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService,
                                    ValidateNfXML validateNfXML) {

        this.commonTaxCocumentEventService = commonTaxCocumentEventService;
        this.commonTaxDocumentEventDisableService = commonTaxDocumentEventDisableService;
        this.commonTaxDocumentEventLetterCorrectionService = commonTaxDocumentEventLetterCorrectionService;
        this.validateNfXML = validateNfXML;
    }

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

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy, String xml) {
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        TaxDocumentModel taxDocumentModel = new TaxDocumentModel();
        FileProcessed errors = validateNfXML.validate(containerKey, multipartFile, file, taxDocumentModel, xml);
        if (errors != null) {
            return errors;
        }

        Date date = validateNfXML.stringToDate(file.getDetailTwo());
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        file.buildTaxDocument(multipartFile.getContentType(), multipartFile.getSize(), Boolean.FALSE, containerKey, tokenResultProxy, ld, taxDocumentModel.getFileType());

        return new FileProcessed(file, Collections.emptyList());
    }

    public FileProcessed identifyTaxDocument(AbstractFile file, String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        String xml = XMLUtil.getXml(multipartFile);

        return identifyTaxDocument(file, containerKey, multipartFile, tokenResultProxy, xml);
    }

    public FileProcessed identifyTaxDocument(AbstractFile file, String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy, String xml) {

        FileProcessed fileProcessed = this.getData(file, multipartFile, containerKey, tokenResultProxy, xml);

        if(!fileProcessed.getErrors().isEmpty()) {
            Boolean cancellationEvent = this.commonTaxCocumentEventService.isCancellationEvent(xml);
            if(cancellationEvent) {
                fileProcessed = this.commonTaxCocumentEventService.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
            } else {
                Boolean letterCorrectionEvent = this.commonTaxDocumentEventLetterCorrectionService.isLetterCorrectionEvent(xml);
                if(letterCorrectionEvent) {
                fileProcessed = this.commonTaxDocumentEventLetterCorrectionService.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
                } else {
                    Boolean disableEvent = this.commonTaxDocumentEventDisableService.isDisableEvent(xml);
                    if(disableEvent) {
                        fileProcessed = this.commonTaxDocumentEventDisableService.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
                    }
                }
            }
        }

//        fileProcessed = this.getData(file, multipartFile, containerKey, tokenResultProxy, xml);

//        if(!cancellationEvent && !letterCorrectionEvent && !disableEvent) {
//            fileProcessed = this.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
//        } else {
//            if(cancellationEvent) {
//                fileProcessed = this.commonTaxCocumentEventService.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
//            } else {
//                if(letterCorrectionEvent) {
//                    fileProcessed = this.commonTaxDocumentEventLetterCorrectionService.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
//                } else {
//                    if(disableEvent) {
//                        fileProcessed = this.commonTaxDocumentEventDisableService.getData(file, multipartFile, containerKey, tokenResultProxy, xml);
//                    }
//                }
//            }
//        }

        return fileProcessed;
    }
}

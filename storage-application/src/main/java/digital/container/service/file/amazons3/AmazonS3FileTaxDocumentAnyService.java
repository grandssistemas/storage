package digital.container.service.file.amazons3;

import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventCanceledService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.AmazonS3Util;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.XMLUtil;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class AmazonS3FileTaxDocumentAnyService extends GumgaService<AmazonS3File, String> {

    private final CommonTaxDocumentEventCanceledService commonTaxCocumentEventService;
    private final CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService;
    private final CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService;
    private final CommonTaxDocumentService commonTaxDocumentService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    public AmazonS3FileTaxDocumentAnyService(GumgaCrudRepository<AmazonS3File, String> repository,
                                             CommonTaxDocumentEventCanceledService commonTaxCocumentEventService,
                                             CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService,
                                             CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService,
                                             CommonTaxDocumentService commonTaxDocumentService,
                                             SendMessageMOMService sendMessageMOMService,
                                             SecurityTokenService securityTokenService,
                                             AmazonS3Service amazonS3Service) {
        super(repository);
        this.commonTaxCocumentEventService = commonTaxCocumentEventService;
        this.commonTaxDocumentEventDisableService = commonTaxDocumentEventDisableService;
        this.commonTaxDocumentEventLetterCorrectionService = commonTaxDocumentEventLetterCorrectionService;
        this.commonTaxDocumentService = commonTaxDocumentService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;

        this.amazonS3Service = amazonS3Service;
    }

    public FileProcessed processUpload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.identifyTaxDocument(containerKey, multipartFile, tokenResultProxy);
    }

    public List<FileProcessed> processUpload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.identifyTaxDocument(containerKey,multipartFile, tokenResultProxy));
        }
        return result;
    }

    public FileProcessed identifyTaxDocument(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        AmazonS3File amazonS3File = new AmazonS3File();
        String xml = XMLUtil.getXml(multipartFile);

        Boolean cancellationEvent = this.commonTaxCocumentEventService.isCancellationEvent(xml);

        FileProcessed fileProcessed = null;
        if(cancellationEvent) {
            fileProcessed = this.commonTaxCocumentEventService.getData(amazonS3File, multipartFile, containerKey, tokenResultProxy);
        } else {
            Boolean disableEvent = this.commonTaxDocumentEventDisableService.isDisableEvent(xml);
            if(disableEvent) {
                fileProcessed = this.commonTaxDocumentEventDisableService.getData(amazonS3File, multipartFile, containerKey, tokenResultProxy);
            } else {
                Boolean letterCorrectionEvent = this.commonTaxDocumentEventLetterCorrectionService.isLetterCorrectionEvent(xml);
                if(letterCorrectionEvent) {
                    fileProcessed = this.commonTaxDocumentEventLetterCorrectionService.getData(amazonS3File, multipartFile, containerKey, tokenResultProxy);
                }
            }
        }

        if(fileProcessed == null) {
            fileProcessed = this.commonTaxDocumentService.getData(amazonS3File, multipartFile, containerKey, tokenResultProxy);
        }

        if(!fileProcessed.getErrors().isEmpty()) {
            return  fileProcessed;
        }

        return saveFile(containerKey, multipartFile, amazonS3File);
    }

    public FileProcessed saveFile(String containerKey, MultipartFile multipartFile, AmazonS3File amazonS3File) {

        this.amazonS3Service.send(amazonS3File, multipartFile, Boolean.FALSE, AmazonS3Util.TAX_DOCUMENT_BUCKET);
        this.sendMessageMOMService.send(amazonS3File, containerKey);

        return new FileProcessed(this.repository.saveAndFlush(amazonS3File), Collections.emptyList());
    }
}

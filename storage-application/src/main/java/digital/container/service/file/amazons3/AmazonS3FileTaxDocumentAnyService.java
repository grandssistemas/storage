package digital.container.service.file.amazons3;

import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventCanceledService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.AmazonS3Util;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.XMLUtil;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AmazonS3FileTaxDocumentAnyService extends GumgaService<AmazonS3File, String> {

    private final CommonTaxDocumentService commonTaxDocumentService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    public AmazonS3FileTaxDocumentAnyService(GumgaCrudRepository<AmazonS3File, String> repository,
                                             CommonTaxDocumentService commonTaxDocumentService,
                                             SendMessageMOMService sendMessageMOMService,
                                             SecurityTokenService securityTokenService,
                                             AmazonS3Service amazonS3Service) {
        super(repository);
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
        FileProcessed fileProcessed = this.commonTaxDocumentService.identifyTaxDocument(amazonS3File, containerKey, multipartFile, tokenResultProxy, xml);

        if((fileProcessed != null && fileProcessed.getErrors() != null && !fileProcessed.getErrors().isEmpty())) {
            return  fileProcessed;
        }

        return processToSaveAmazonS3(containerKey, multipartFile, amazonS3File, xml);
//        return saveFile(containerKey, multipartFile, amazonS3File);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessed processToSaveAmazonS3(String containerKey, MultipartFile multipartFile, AmazonS3File amazonS3File, String xml) {
        this.sendMessageMOMService.sendInviteAmazon(amazonS3File, containerKey, xml);

        return new FileProcessed(this.repository.save(amazonS3File), Collections.emptyList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessed saveFile(String containerKey, MultipartFile multipartFile, AmazonS3File amazonS3File) {

        this.amazonS3Service.send(amazonS3File, multipartFile, Boolean.FALSE, AmazonS3Util.TAX_DOCUMENT_BUCKET);
        this.sendMessageMOMService.send(amazonS3File, containerKey);

        return new FileProcessed(this.repository.saveAndFlush(amazonS3File), Collections.emptyList());
    }
}

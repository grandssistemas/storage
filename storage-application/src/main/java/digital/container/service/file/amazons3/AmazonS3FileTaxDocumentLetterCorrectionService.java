package digital.container.service.file.amazons3;

import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.AmazonS3Util;
import digital.container.storage.domain.model.util.TokenResultProxy;
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
public class AmazonS3FileTaxDocumentLetterCorrectionService extends GumgaService<AmazonS3File, String> {

    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;
    private final CommonTaxDocumentEventLetterCorrectionService service;
    private final SendFileAmazonS3Service sendFileAmazonS3Service;


    @Autowired
    public AmazonS3FileTaxDocumentLetterCorrectionService(GumgaCrudRepository<AmazonS3File, String> repository,
                                                          SendMessageMOMService sendMessageMOMService,
                                                          SecurityTokenService securityTokenService,
                                                          CommonTaxDocumentEventLetterCorrectionService service,
                                                          SendFileAmazonS3Service sendFileAmazonS3Service) {
        super(repository);
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;
        this.service = service;
        this.sendFileAmazonS3Service = sendFileAmazonS3Service;
    }

    public FileProcessed processUpload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.saveFile(containerKey, multipartFile, tokenResultProxy);
    }

    public List<FileProcessed> processUpload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.saveFile(containerKey,multipartFile, tokenResultProxy));
        }
        return result;
    }

    public FileProcessed saveFile(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        AmazonS3File amazonS3File = new AmazonS3File();
        FileProcessed data = this.service.getData(amazonS3File, multipartFile, containerKey, tokenResultProxy);

        if(!data.getErrors().isEmpty()) {
            return data;
        }

        this.sendFileAmazonS3Service.send(amazonS3File, multipartFile, Boolean.FALSE, AmazonS3Util.TAX_DOCUMENT_BUCKET);
        this.sendMessageMOMService.send(amazonS3File, containerKey);

        return new FileProcessed(this.repository.saveAndFlush(amazonS3File), Collections.emptyList());
    }
}

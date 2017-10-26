package digital.container.service.file.amazons3;

import digital.container.service.message.SendMessageMOMService;
import digital.container.service.storage.LimitFileService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.vo.FileProcessed;
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
public class AmazonS3FileTaxDocumentService extends GumgaService<AmazonS3File, String> {

    private final SecurityTokenService securityTokenService;
    private final LimitFileService limitFileService;
    private final CommonTaxDocumentService commonTaxDocumentService;
    private final SendMessageMOMService sendMessageMOMService;
    private final AmazonS3Service amazonS3Service;


    @Autowired
    public AmazonS3FileTaxDocumentService(GumgaCrudRepository<AmazonS3File, String> repository,
                                          SecurityTokenService securityTokenService,
                                          LimitFileService limitFileService,
                                          CommonTaxDocumentService commonTaxDocumentService,
                                          SendMessageMOMService sendMessageMOMService,
                                          AmazonS3Service amazonS3Service) {
        super(repository);
        this.securityTokenService = securityTokenService;
        this.limitFileService = limitFileService;
        this.commonTaxDocumentService = commonTaxDocumentService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.amazonS3Service = amazonS3Service;
    }

    @Transactional
    public FileProcessed processUpload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.save(containerKey, multipartFile, tokenResultProxy);
    }

    @Transactional
    public List<FileProcessed> processUpload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        this.limitFileService.limitMaximumExceeded(multipartFiles);

        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);

        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.save(containerKey,multipartFile, tokenResultProxy));
        }
        return result;
    }

    @Transactional
    public FileProcessed save(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        AmazonS3File amazonS3 = new AmazonS3File();

        FileProcessed data = this.commonTaxDocumentService.getData(amazonS3, multipartFile, containerKey, tokenResultProxy);
        if(!data.getErrors().isEmpty()) {
            return data;
        }

        this.amazonS3Service.send(amazonS3, multipartFile, Boolean.FALSE, AmazonS3Util.TAX_DOCUMENT_BUCKET);
        this.sendMessageMOMService.send(amazonS3, containerKey);
        return new FileProcessed(this.repository.saveAndFlush(amazonS3), Collections.emptyList());
    }
}

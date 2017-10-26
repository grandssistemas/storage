package digital.container.service.file.amazons3;

import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventCanceledService;
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
public class AmazonS3FileTaxDocumentCanceledService extends GumgaService<AmazonS3File, String> {

    private final CommonTaxDocumentEventCanceledService commonTaxCocumentEventService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;
    private final AmazonS3Service amazonS3Service;

    @Autowired
    public AmazonS3FileTaxDocumentCanceledService(GumgaCrudRepository<AmazonS3File, String> repository,
                                                  CommonTaxDocumentEventCanceledService commonTaxCocumentEventService,
                                                  SendMessageMOMService sendMessageMOMService,
                                                  SecurityTokenService securityTokenService,
                                                  AmazonS3Service amazonS3Service) {
        super(repository);
        this.commonTaxCocumentEventService = commonTaxCocumentEventService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;
        this.amazonS3Service = amazonS3Service;
    }

    private FileProcessed saveFile(String containerKey,
                                   MultipartFile multipartFile,
                                   TokenResultProxy tokenResultProxy) {
        AmazonS3File amazonS3File = new AmazonS3File();
        FileProcessed data = this.commonTaxCocumentEventService.getData(amazonS3File,
                multipartFile, containerKey, tokenResultProxy);

        if(!data.getErrors().isEmpty()) {
            return data;
        }

        this.amazonS3Service.send(amazonS3File, multipartFile, Boolean.FALSE, AmazonS3Util.TAX_DOCUMENT_BUCKET);
        this.sendMessageMOMService.send(amazonS3File, containerKey);

        return new FileProcessed(this.repository.saveAndFlush(amazonS3File), Collections.emptyList());
    }

    public FileProcessed processUpload(String containerKey,
                                MultipartFile multipartFile,
                                String tokenSoftwareHouse,
                                String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.saveFile(containerKey, multipartFile, tokenResultProxy);
    }

    public List<FileProcessed> processUpload(String containerKey,
                                      List<MultipartFile> multipartFiles,
                                      String tokenSoftwareHouse,
                                      String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.saveFile(containerKey,multipartFile, tokenResultProxy));
        }
        return result;
    }
}

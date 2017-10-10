package digital.container.service.file.amazons3;

import digital.container.service.storage.LimitFileService;
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
public class AmazonS3FileService extends GumgaService<AmazonS3File, String> {

    private final SendFileAmazonS3Service sendFileAmazonS3Service;
    private final LimitFileService limitFileService;
    private final SecurityTokenService securityTokenService;

    @Autowired
    public AmazonS3FileService(GumgaCrudRepository<AmazonS3File, String> repository,
                               SendFileAmazonS3Service sendFileAmazonS3Service,
                               LimitFileService limitFileService, SecurityTokenService securityTokenService) {
        super(repository);
        this.sendFileAmazonS3Service = sendFileAmazonS3Service;
        this.limitFileService = limitFileService;
        this.securityTokenService = securityTokenService;
    }

    @Transactional
    public FileProcessed processUpload(String containerKey, MultipartFile multipartFile, boolean shared, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.upload(containerKey, multipartFile, shared, tokenResultProxy);
    }

    @Transactional
    public List<FileProcessed> processUpload(String containerKey, List<MultipartFile> multipartFiles, boolean shared, String tokenSoftwareHouse, String tokenAccountant) {
        this.limitFileService.limitMaximumExceeded(multipartFiles);
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);

        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.upload(containerKey,multipartFile, shared, tokenResultProxy));
        }

        return result;
    }


    private FileProcessed upload(String containerKey, MultipartFile multipartFile, boolean shared, TokenResultProxy tokenResultProxy) {
        AmazonS3File file = (AmazonS3File) new AmazonS3File()
                .buildAnything(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getSize(),
                shared,
                containerKey,
                tokenResultProxy);

        this.sendFileAmazonS3Service.send(file, multipartFile, shared, AmazonS3Util.ANYTHING_BUCKET);
        return new FileProcessed(this.repository.saveAndFlush(file), Collections.emptyList());
    }
}

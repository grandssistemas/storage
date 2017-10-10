package digital.container.service.file.amazons3;

import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Service
@Transactional
public class AmazonS3FileService extends GumgaService<AmazonS3File, String> {

    private final SendFileAmazonS3Service sendFileAmazonS3Service;

    @Autowired
    public AmazonS3FileService(GumgaCrudRepository<AmazonS3File, String> repository,
                               SendFileAmazonS3Service sendFileAmazonS3Service) {
        super(repository);
        this.sendFileAmazonS3Service = sendFileAmazonS3Service;
    }

    @Transactional
    public FileProcessed processUpload(String containerKey, MultipartFile multipartFile, boolean shared) {
        return this.upload(containerKey, multipartFile, shared);
    }

    private FileProcessed upload(String containerKey, MultipartFile multipartFile, boolean shared) {
        AmazonS3File file = (AmazonS3File) new AmazonS3File()
                .buildAnything(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getSize(),
                shared,
                containerKey);

//        this.sendFileAmazonS3Service.send();

        return new FileProcessed(this.repository.saveAndFlush(file), Collections.emptyList());
    }
}

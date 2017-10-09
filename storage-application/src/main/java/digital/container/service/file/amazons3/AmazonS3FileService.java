package digital.container.service.file.amazons3;

import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AmazonS3FileService extends GumgaService<AmazonS3File, Long> {

    @Autowired
    public AmazonS3FileService(GumgaCrudRepository<AmazonS3File, Long> repository) {
        super(repository);
    }

    public void processUpload(String containerKey, MultipartFile multipartFile, boolean shared) {

    }
}

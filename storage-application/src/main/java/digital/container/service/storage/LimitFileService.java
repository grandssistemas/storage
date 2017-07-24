package digital.container.service.storage;

import digital.container.exception.LimitFilesExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class LimitFileService {

    public void limitMaximumExceeded(List<MultipartFile> multipartFiles) {
        if(multipartFiles.size() > 500) {
            throw new LimitFilesExceededException(HttpStatus.FORBIDDEN);
        }
    }
}

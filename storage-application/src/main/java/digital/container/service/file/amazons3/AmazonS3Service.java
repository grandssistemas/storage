package digital.container.service.file.amazons3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import digital.container.storage.domain.model.file.AbstractFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class AmazonS3Service {

    private final AmazonS3 amazonS3;

    @Autowired
    public AmazonS3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public void send(AbstractFile abstractFile, MultipartFile multipartFile, Boolean shared, String bucketName) {
        File file = null;
        try {
            file = new File(multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);
            try {
                PutObjectRequest objectRequest = new PutObjectRequest(bucketName, abstractFile.getRelativePath(), file);
                if(shared) {
                    objectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
                }
                amazonS3.putObject(objectRequest);
            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(file != null && file.exists()) {
                file.delete();
            }
        }
    }

    public File getFile(String bucketName, String path, String relativePath) {
        File file = new File(path.concat("/").concat(relativePath));
        amazonS3.getObject(new GetObjectRequest(bucketName, relativePath), file);
        return file;
    }

    public File getFile(String bucketName, String path, String relativePath, String fileName) {
        File file = new File(path.concat("/").concat(UUID.randomUUID().toString()).concat("/").concat(fileName));
        amazonS3.getObject(new GetObjectRequest(bucketName, relativePath), file);
        return file;
    }
}

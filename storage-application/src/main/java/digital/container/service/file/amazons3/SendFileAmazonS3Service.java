package digital.container.service.file.amazons3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class SendFileAmazonS3Service {

    private final AmazonS3 amazonS3;

    @Autowired
    public SendFileAmazonS3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }


    public void send(MultipartFile multipartFile, Boolean shared, String bucketName) {
        try {
            File file = new File(multipartFile.getOriginalFilename());
            multipartFile.transferTo(file);
            try {
                amazonS3.putObject(new PutObjectRequest(bucketName,"a" ,file));
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
        }
//        S3Object s3Object = new S3Object();
//        s3Object.setBucketName(bucketName);
//        s3Object.set
    }
}

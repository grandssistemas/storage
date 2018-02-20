package digital.container.repository.file;

import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AmazonS3FileRepository extends GumgaCrudRepository<AmazonS3File, String> {

    @Modifying(clearAutomatically = true)
    @Query("update AmazonS3File set fileStatus = :status where id = :id")
    void changeStatusByID(@Param("id") String id, @Param("status") FileStatus status);
}

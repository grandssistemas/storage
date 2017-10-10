package digital.container.service.file.databasefile;

import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import digital.container.repository.file.DatabaseFilePartRepository;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class DatabaseFilePartService extends GumgaService<DatabaseFilePart, String> {

    private final DatabaseFilePartRepository databaseFilePartRepository;

    @Autowired
    public DatabaseFilePartService(GumgaCrudRepository<DatabaseFilePart, String> repository) {
        super(repository);
        this.databaseFilePartRepository = DatabaseFilePartRepository.class.cast(repository);
    }

    @Transactional
    public void saveFile(DatabaseFile databaseFile, MultipartFile multipartFile) {
        try(InputStream inputStream = multipartFile.getInputStream()) {

            while (inputStream.available() > 0) {
                DatabaseFilePart newDatabaseFilePart = new DatabaseFilePart();
                newDatabaseFilePart.setDatabaseFile(databaseFile);

                byte buffer[] = new byte[inputStream.available() > DatabaseFilePart.PART_SIZE ? DatabaseFilePart.PART_SIZE : inputStream.available()];
                newDatabaseFilePart.setRawBytes(buffer);
                inputStream.read(buffer);

                DatabaseFilePart databaseFilePartSaved = this.databaseFilePartRepository.saveAndFlush(newDatabaseFilePart);

                databaseFile.addDatabaseFilePart(databaseFilePartSaved);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

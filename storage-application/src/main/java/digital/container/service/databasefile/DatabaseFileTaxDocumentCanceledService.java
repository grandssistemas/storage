package digital.container.service.databasefile;

import digital.container.repository.DatabaseFileRepository;
import digital.container.service.taxdocument.CommonTaxCocumentEventService;
import digital.container.storage.domain.model.file.DatabaseFile;
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
public class DatabaseFileTaxDocumentCanceledService extends GumgaService<DatabaseFile, Long> {

    @Autowired
    private CommonTaxCocumentEventService commonTaxCocumentEventService;

    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    private DatabaseFilePartService databaseFilePartService;

    @Autowired
    public DatabaseFileTaxDocumentCanceledService(GumgaCrudRepository<DatabaseFile, Long> repository) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
    }

    public FileProcessed upload(String containerKey, MultipartFile multipartFile) {
        DatabaseFile databaseFile = new DatabaseFile();
        FileProcessed data = this.commonTaxCocumentEventService.getData(databaseFile, multipartFile, containerKey);

        if(data.getErrors().size() > 0) {
            return data;
        }
        this.databaseFileRepository.saveAndFlush(databaseFile);
        this.databaseFilePartService.saveFile(databaseFile, multipartFile);

        return new FileProcessed(this.databaseFileRepository.saveAndFlush(databaseFile), Collections.EMPTY_LIST);
    }
}

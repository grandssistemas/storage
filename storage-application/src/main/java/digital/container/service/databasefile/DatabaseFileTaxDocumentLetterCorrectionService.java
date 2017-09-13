package digital.container.service.databasefile;

import digital.container.repository.DatabaseFileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
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
public class DatabaseFileTaxDocumentLetterCorrectionService extends GumgaService<DatabaseFile, Long> {

    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    private CommonTaxDocumentEventLetterCorrectionService service;

    @Autowired
    private DatabaseFilePartService databaseFilePartService;

    @Autowired
    private SendMessageMOMService sendMessageMOMService;

    @Autowired
    public DatabaseFileTaxDocumentLetterCorrectionService(GumgaCrudRepository<DatabaseFile, Long> repository) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
    }

    public FileProcessed upload(String containerKey, MultipartFile multipartFile) {
        DatabaseFile databaseFile = new DatabaseFile();
        FileProcessed data = this.service.getData(databaseFile, multipartFile, containerKey);

        if(data.getErrors().size() > 0) {
            return data;
        }

        this.databaseFileRepository.saveAndFlush(databaseFile);
        this.databaseFilePartService.saveFile(databaseFile, multipartFile);
        this.sendMessageMOMService.send(databaseFile, containerKey);

        return new FileProcessed(this.databaseFileRepository.saveAndFlush(databaseFile), Collections.EMPTY_LIST);
    }

    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles) {
        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.upload(containerKey,multipartFile));
        }
        return result;
    }
}

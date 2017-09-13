package digital.container.service.databasefile;

import digital.container.repository.DatabaseFilePartRepository;
import digital.container.repository.DatabaseFileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventCanceledService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.XMLUtil;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class DatabaseFileTaxDocumentAnyService extends GumgaService<DatabaseFile, Long> {

    @Autowired
    private CommonTaxDocumentEventCanceledService commonTaxCocumentEventService;

    @Autowired
    private CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService;

    @Autowired
    private CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService;

    @Autowired
    private CommonTaxDocumentService commonTaxDocumentService;

    @Autowired
    private DatabaseFilePartService databaseFilePartService;

    @Autowired
    private SendMessageMOMService sendMessageMOMService;

    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    public DatabaseFileTaxDocumentAnyService(GumgaCrudRepository<DatabaseFile, Long> repository) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
    }


    public FileProcessed upload(String containerKey, MultipartFile multipartFile) {
        DatabaseFile databaseFile = new DatabaseFile();
        String xml = XMLUtil.getXml(multipartFile);

        Boolean cancellationEvent = this.commonTaxCocumentEventService.isCancellationEvent(xml);

        FileProcessed fileProcessed = null;
        if(cancellationEvent) {
            fileProcessed = this.commonTaxCocumentEventService.getData(databaseFile, multipartFile, containerKey);
        } else {
            Boolean disableEvent = this.commonTaxDocumentEventDisableService.isDisableEvent(xml);
            if(disableEvent) {
                fileProcessed = this.commonTaxDocumentEventDisableService.getData(databaseFile, multipartFile, containerKey);
            } else {
                Boolean letterCorrectionEvent = this.commonTaxDocumentEventLetterCorrectionService.isLetterCorrectionEvent(xml);
                if(letterCorrectionEvent) {
                    fileProcessed = this.commonTaxDocumentEventLetterCorrectionService.getData(databaseFile, multipartFile, containerKey);
                }
            }
        }

        if(fileProcessed == null) {
            fileProcessed = this.commonTaxDocumentService.getData(databaseFile, multipartFile, containerKey);
        }

        if(fileProcessed.getErrors().size() > 0) {
            return  fileProcessed;
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

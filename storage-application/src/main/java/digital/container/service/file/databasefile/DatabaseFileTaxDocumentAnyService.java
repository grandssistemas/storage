package digital.container.service.file.databasefile;

import digital.container.repository.file.DatabaseFileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventCanceledService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.XMLUtil;
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
public class DatabaseFileTaxDocumentAnyService extends GumgaService<DatabaseFile, String> {

    private final CommonTaxDocumentEventCanceledService commonTaxCocumentEventService;
    private final CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService;
    private final CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService;
    private final CommonTaxDocumentService commonTaxDocumentService;
    private final DatabaseFilePartService databaseFilePartService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;

    private final DatabaseFileRepository databaseFileRepository;

    @Autowired
    public DatabaseFileTaxDocumentAnyService(GumgaCrudRepository<DatabaseFile, String> repository,
                                             CommonTaxDocumentEventCanceledService commonTaxCocumentEventService,
                                             CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService,
                                             CommonTaxDocumentEventLetterCorrectionService commonTaxDocumentEventLetterCorrectionService,
                                             CommonTaxDocumentService commonTaxDocumentService,
                                             DatabaseFilePartService databaseFilePartService,
                                             SendMessageMOMService sendMessageMOMService,
                                             SecurityTokenService securityTokenService) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
        this.commonTaxCocumentEventService = commonTaxCocumentEventService;
        this.commonTaxDocumentEventDisableService = commonTaxDocumentEventDisableService;
        this.commonTaxDocumentEventLetterCorrectionService = commonTaxDocumentEventLetterCorrectionService;
        this.commonTaxDocumentService = commonTaxDocumentService;
        this.databaseFilePartService = databaseFilePartService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;
    }


    private FileProcessed saveFile(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        DatabaseFile databaseFile = new DatabaseFile();
        String xml = XMLUtil.getXml(multipartFile);

        Boolean cancellationEvent = this.commonTaxCocumentEventService.isCancellationEvent(xml);

        FileProcessed fileProcessed = null;
        if(cancellationEvent) {
            fileProcessed = this.commonTaxCocumentEventService.getData(databaseFile, multipartFile, containerKey, tokenResultProxy);
        } else {
            Boolean disableEvent = this.commonTaxDocumentEventDisableService.isDisableEvent(xml);
            if(disableEvent) {
                fileProcessed = this.commonTaxDocumentEventDisableService.getData(databaseFile, multipartFile, containerKey, tokenResultProxy);
            } else {
                Boolean letterCorrectionEvent = this.commonTaxDocumentEventLetterCorrectionService.isLetterCorrectionEvent(xml);
                if(letterCorrectionEvent) {
                    fileProcessed = this.commonTaxDocumentEventLetterCorrectionService.getData(databaseFile, multipartFile, containerKey, tokenResultProxy);
                }
            }
        }

        if(fileProcessed == null) {
            fileProcessed = this.commonTaxDocumentService.getData(databaseFile, multipartFile, containerKey, tokenResultProxy);
        }

        if(!fileProcessed.getErrors().isEmpty()) {
            return  fileProcessed;
        }

        this.databaseFileRepository.saveAndFlush(databaseFile);
        this.databaseFilePartService.saveFile(databaseFile, multipartFile);
        this.sendMessageMOMService.send(databaseFile, containerKey);

        return new FileProcessed(this.databaseFileRepository.saveAndFlush(databaseFile), Collections.emptyList());
    }

    public FileProcessed upload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.saveFile(containerKey, multipartFile, tokenResultProxy);
    }

    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.saveFile(containerKey,multipartFile, tokenResultProxy));
        }
        return result;
    }
}

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
import digital.container.util.TokenResultProxy;
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

    @Autowired
    private SecurityTokenService securityTokenService;

    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    public DatabaseFileTaxDocumentAnyService(GumgaCrudRepository<DatabaseFile, Long> repository) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
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

        if(fileProcessed.getErrors().size() > 0) {
            return  fileProcessed;
        }

        this.databaseFileRepository.saveAndFlush(databaseFile);
        this.databaseFilePartService.saveFile(databaseFile, multipartFile);
        this.sendMessageMOMService.send(databaseFile, containerKey);

        return new FileProcessed(this.databaseFileRepository.saveAndFlush(databaseFile), Collections.EMPTY_LIST);
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

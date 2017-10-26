package digital.container.service.file.databasefile;

import digital.container.repository.file.DatabaseFileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventCanceledService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.XMLUtil;
import io.gumga.application.GumgaService;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DatabaseFileTaxDocumentAnyService extends GumgaService<DatabaseFile, String> {

    private final CommonTaxDocumentService commonTaxDocumentService;
    private final DatabaseFilePartService databaseFilePartService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;

    private final DatabaseFileRepository databaseFileRepository;

    @Autowired
    public DatabaseFileTaxDocumentAnyService(GumgaCrudRepository<DatabaseFile, String> repository,
                                             CommonTaxDocumentService commonTaxDocumentService,
                                             DatabaseFilePartService databaseFilePartService,
                                             SendMessageMOMService sendMessageMOMService,
                                             SecurityTokenService securityTokenService) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
        this.commonTaxDocumentService = commonTaxDocumentService;
        this.databaseFilePartService = databaseFilePartService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;
    }

    public FileProcessed saveFile(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        DatabaseFile databaseFile = new DatabaseFile();

        FileProcessed fileProcessed = this.commonTaxDocumentService.identifyTaxDocument(databaseFile, containerKey, multipartFile, tokenResultProxy);

        if(!fileProcessed.getErrors().isEmpty()) {
            return  fileProcessed;
        }

//        this.databaseFileRepository.saveAndFlush(databaseFile);
//        this.databaseFilePartService.saveFile(databaseFile, multipartFile);
//        this.sendMessageMOMService.send(databaseFile, containerKey);


        return new FileProcessed(databaseFile, Collections.emptyList(), multipartFile);
    }

    public FileProcessed upload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.saveFile(containerKey, multipartFile, tokenResultProxy);
    }

    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        List<FileProcessed> result = new ArrayList<>();
        GumgaOi gumgaOi = new GumgaOi(GumgaThreadScope.organizationCode.get());

        if(multipartFiles.size() > 350) {
            System.out.println("parallel");
            multipartFiles
                    .stream()
                    .parallel()
                    .forEach(x -> {
                        GumgaThreadScope.organizationCode.set(gumgaOi.getValue());
                        result.add(this.saveFile(containerKey,x, tokenResultProxy));
                    });
        } else {
            System.out.println("SIMPLES");
            for(MultipartFile multipartFile : multipartFiles) {
                result.add(this.saveFile(containerKey,multipartFile, tokenResultProxy));
            }
        }

        for (FileProcessed fileProcessed : result) {
            if(fileProcessed != null && fileProcessed.getErrors() != null && fileProcessed.getErrors().isEmpty()) {
                DatabaseFile file = (DatabaseFile) fileProcessed.getFile();
                this.saveDatabaseFile(file, fileProcessed.getMultipartFile());
            }
        }
        this.databaseFileRepository.flush();

        new Thread(() -> {
            sendMEssageMoM(result, containerKey);
        }).start();

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDatabaseFile(DatabaseFile file, MultipartFile multipartFile) {
        this.databaseFileRepository.save(file);
        this.databaseFilePartService.saveFile(file, multipartFile);
        this.databaseFileRepository.save(file);
    }

    public void sendMEssageMoM(List<FileProcessed> result, String containerKey) {
        for (FileProcessed fileProcessed : result) {
            if (fileProcessed.getErrors().isEmpty()) {
                this.sendMessageMOMService.send(fileProcessed.getFile(), containerKey);
            }
        }
    }

}

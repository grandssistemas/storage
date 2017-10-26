package digital.container.service.file.databasefile;

import digital.container.repository.file.DatabaseFileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventDisableService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenResultProxy;
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
public class DatabaseFileTaxDocumentDisableService extends GumgaService<DatabaseFile, String> {

    private final DatabaseFileRepository databaseFileRepository;
    private final CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService;
    private final DatabaseFilePartService databaseFilePartService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;

    @Autowired
    public DatabaseFileTaxDocumentDisableService(GumgaCrudRepository<DatabaseFile, String> repository,
                                                 CommonTaxDocumentEventDisableService commonTaxDocumentEventDisableService,
                                                 DatabaseFilePartService databaseFilePartService,
                                                 SendMessageMOMService sendMessageMOMService,
                                                 SecurityTokenService securityTokenService) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
        this.commonTaxDocumentEventDisableService = commonTaxDocumentEventDisableService;
        this.databaseFilePartService = databaseFilePartService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;
    }

    private FileProcessed saveFile(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        DatabaseFile databaseFile = new DatabaseFile();
        FileProcessed data = this.commonTaxDocumentEventDisableService.getData(databaseFile, multipartFile, containerKey, tokenResultProxy);

        if(!data.getErrors().isEmpty()) {
            return data;
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

package digital.container.service.file.databasefile;

import digital.container.exception.FileNotFoundException;
import digital.container.service.message.SendMessageMOMService;
import digital.container.repository.file.DatabaseFileRepository;
import digital.container.service.storage.LimitFileService;
import digital.container.service.storage.MessageStorage;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.storage.domain.model.util.TokenUtil;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Transactional
public class DatabaseFileTaxDocumentService extends GumgaService<DatabaseFile, String> {

    private final DatabaseFileRepository databaseFileRepository;
    private final DatabaseFilePartService databaseFilePartService;
    private final LimitFileService limitFileService;
    private final CommonTaxDocumentService commonTaxDocumentService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;

    @Autowired
    public DatabaseFileTaxDocumentService(GumgaCrudRepository<DatabaseFile, String> repository,
                                          DatabaseFilePartService databaseFilePartService,
                                          LimitFileService limitFileService,
                                          CommonTaxDocumentService commonTaxDocumentService,
                                          SendMessageMOMService sendMessageMOMService,
                                          SecurityTokenService securityTokenService) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
        this.databaseFilePartService = databaseFilePartService;
        this.limitFileService = limitFileService;
        this.commonTaxDocumentService = commonTaxDocumentService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;
    }

    @Transactional
    public FileProcessed saveFile(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {

        DatabaseFile databaseFile = new DatabaseFile();

        FileProcessed data = this.commonTaxDocumentService.getData(databaseFile, multipartFile, containerKey, tokenResultProxy);
        if(!data.getErrors().isEmpty()) {
            return data;
        }

        this.databaseFileRepository.saveAndFlush(databaseFile);
        this.databaseFilePartService.saveFile(databaseFile, multipartFile);
        this.sendMessageMOMService.send(databaseFile, containerKey);

        return new FileProcessed(this.databaseFileRepository.saveAndFlush(databaseFile), Collections.emptyList());
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy result = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.saveFile(containerKey, multipartFile, result);
    }


    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        this.limitFileService.limitMaximumExceeded(multipartFiles);
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);

        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.saveFile(containerKey,multipartFile, tokenResultProxy));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DatabaseFile getFileHash(String hash) {
        DatabaseFile result = this.databaseFileRepository
                .getByHash(hash, TokenUtil.getEndWithOi(), TokenUtil.getContainsSharedOi())
                .orElseThrow(() -> new FileNotFoundException(MessageStorage.FILE_NOT_FOUND + ":" + hash, HttpStatus.NOT_FOUND));

        Hibernate.initialize(result.getParts());
        return result;
    }

    @Transactional
    @Override
    public void delete(DatabaseFile resource) {
        for(DatabaseFilePart databaseFile: resource.getParts()) {
            this.databaseFilePartService.delete(databaseFile);
        }

        super.delete(resource);
    }

    @Transactional
    public Boolean deleteFileByHash(String hash) {
        Optional<DatabaseFile> result = this.databaseFileRepository.getByHash(hash, TokenUtil.getEndWithOi(), TokenUtil.getContainsSharedOi());
        if(result.isPresent()) {
            delete(result.get());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Transactional
    public Boolean deleteFileById(String id) {
        DatabaseFile view = view(id);
        delete(view);
        return Boolean.FALSE;
    }
}

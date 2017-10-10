package digital.container.service.file.databasefile;

import digital.container.exception.FileNotFoundException;
import digital.container.service.container.PermissionContainerService;
import digital.container.service.storage.LimitFileService;
import digital.container.service.storage.MessageStorage;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.repository.file.DatabaseFileRepository;
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
public class DatabaseFileService extends GumgaService<DatabaseFile, String> {

    private final DatabaseFileRepository databaseFileRepository;
    private final DatabaseFilePartService databaseFilePartService;
    private final LimitFileService limitFileService;
    private final SecurityTokenService securityTokenService;


    @Autowired
    public DatabaseFileService(GumgaCrudRepository<DatabaseFile, String> repository,
                               DatabaseFilePartService databaseFilePartService,
                               LimitFileService limitFileService,
                               SecurityTokenService securityTokenService) {
        super(repository);
        this.databaseFileRepository = DatabaseFileRepository.class.cast(repository);
        this.databaseFilePartService = databaseFilePartService;
        this.limitFileService = limitFileService;
        this.securityTokenService = securityTokenService;
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile, boolean shared, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.save(containerKey, multipartFile, shared, tokenResultProxy);
    }

    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, boolean shared, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);

        this.limitFileService.limitMaximumExceeded(multipartFiles);

        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.save(containerKey, multipartFile, shared, tokenResultProxy));
        }

        return result;
    }

    @Transactional
    private FileProcessed save(String containerKey, MultipartFile multipartFile, Boolean shared, TokenResultProxy tokenResultProxy){
        DatabaseFile databaseFile = (DatabaseFile) new DatabaseFile()
                .buildAnything(
                        multipartFile.getOriginalFilename(),
                        multipartFile.getContentType(),
                        multipartFile.getSize(),
                        shared,
                        containerKey,
                        tokenResultProxy);

        DatabaseFile newDatabaseFile = this.databaseFileRepository.saveAndFlush(databaseFile);

        this.databaseFilePartService.saveFile(newDatabaseFile, multipartFile);
        return new FileProcessed(this.databaseFileRepository.saveAndFlush(newDatabaseFile), Collections.emptyList());
    }



    @Transactional(readOnly = true)
    public DatabaseFile getFileHash(String hash, Boolean shared) {
        DatabaseFile result = this.databaseFileRepository
                .getByHash(hash, shared)
                .orElseThrow(() -> new FileNotFoundException(MessageStorage.FILE_NOT_FOUND + ":" + hash, HttpStatus.NOT_FOUND));

        Hibernate.initialize(result.getParts());
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

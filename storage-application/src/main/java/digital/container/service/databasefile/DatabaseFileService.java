package digital.container.service.databasefile;

import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.DatabaseFilePart;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.repository.DatabaseFileRepository;
import digital.container.util.GenerateHash;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class DatabaseFileService extends GumgaService<DatabaseFile, Long> {

    @Autowired
    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    private DatabaseFilePartService databaseFilePartService;

    @Autowired
    public DatabaseFileService(GumgaCrudRepository<DatabaseFile, Long> repository) {
        super(repository);
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile, boolean shared) {
        DatabaseFile databaseFile = new DatabaseFile();
        databaseFile.setFileType(FileType.ANYTHING);
        databaseFile.setFileStatus(FileStatus.DO_NOT_SYNC);
        databaseFile.setFilePublic(shared);

        databaseFile.setName(multipartFile.getOriginalFilename());
        databaseFile.setContainerKey(containerKey);
        databaseFile.setCreateDate(Calendar.getInstance());
        databaseFile.setHash(GenerateHash.generateDatabaseFile());
        databaseFile.setContentType(multipartFile.getContentType());
        databaseFile.setSize(multipartFile.getSize());
        DatabaseFile newDatabaseFile = this.databaseFileRepository.saveAndFlush(databaseFile);

        this.databaseFilePartService.saveFile(newDatabaseFile, multipartFile);
        return new FileProcessed(this.databaseFileRepository.saveAndFlush(newDatabaseFile), Collections.EMPTY_LIST);
    }

    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, boolean shared) {
        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.upload(containerKey, multipartFile, shared));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DatabaseFile getFileHash(String hash, Boolean shared) {
        DatabaseFile result = this.databaseFileRepository
                .getByHash(hash, shared)
                .orElseThrow(() -> new GumgaRunTimeException("Não foi encontrado o documento com o hash:" + hash, HttpStatus.NOT_FOUND));

        Hibernate.initialize(result.getParts());
        return result;
    }

    @Transactional(readOnly = true)
    public DatabaseFile getFileHash(String hash) {
        DatabaseFile result = this.databaseFileRepository
                .getByHash(hash)
                .orElseThrow(() -> new GumgaRunTimeException("Não foi encontrado o documento com o hash:" + hash, HttpStatus.NOT_FOUND));

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
        Optional<DatabaseFile> result = this.databaseFileRepository.getByHash(hash);
        if(result.isPresent()) {
            delete(result.get());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Transactional
    public Boolean deleteFileById(Long id) {
        DatabaseFile view = view(id);
        delete(view);
        return Boolean.FALSE;
    }
}
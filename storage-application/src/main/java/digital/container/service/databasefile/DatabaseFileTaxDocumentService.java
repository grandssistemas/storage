package digital.container.service.databasefile;

import digital.container.exception.FileNotFoundException;
import digital.container.exception.KeyWasNotRegisteredInStorageYetException;
import digital.container.repository.DatabaseFileRepository;
import digital.container.service.container.PermissionContainerService;
import digital.container.service.storage.LimitFileService;
import digital.container.service.storage.MessageStorage;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.DatabaseFilePart;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.GenerateHash;
import digital.container.util.ValidateNfXML;
import digital.container.vo.TaxDocumentModel;
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
public class DatabaseFileTaxDocumentService extends GumgaService<DatabaseFile, Long> {

    @Autowired
    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    private DatabaseFilePartService databaseFilePartService;

//    @Autowired
//    private JmsTemplate jmsTemplate;

    @Autowired
    private LimitFileService limitFileService;

    @Autowired
    private PermissionContainerService permissionContainerService;

    @Autowired
    public DatabaseFileTaxDocumentService(GumgaCrudRepository<DatabaseFile, Long> repository) {
        super(repository);
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile) {
        if(!this.permissionContainerService.containerKeyValid(containerKey)) {
            throw new KeyWasNotRegisteredInStorageYetException(HttpStatus.FORBIDDEN);
        }

        DatabaseFile databaseFile = new DatabaseFile();
        databaseFile.setName(multipartFile.getOriginalFilename());
        databaseFile.setFileStatus(FileStatus.NOT_SYNC);


        TaxDocumentModel taxDocumentModel = new TaxDocumentModel();
        FileProcessed errors = ValidateNfXML.validate(containerKey, multipartFile, databaseFile, taxDocumentModel);
        if (errors != null) {
            return errors;
        }

        databaseFile.setContainerKey(containerKey);
        databaseFile.setCreateDate(Calendar.getInstance());
        databaseFile.setFileType(taxDocumentModel.getFileType());
        databaseFile.setHash(GenerateHash.generateDatabaseFile());
        databaseFile.setContentType(multipartFile.getContentType());
        databaseFile.setSize(multipartFile.getSize());

        DatabaseFile newDatabaseFile = this.databaseFileRepository.saveAndFlush(databaseFile);

        this.databaseFilePartService.saveFile(newDatabaseFile, multipartFile);

//        Map invite = new HashMap();
//        invite.put("container", containerKey);
//        invite.put("fileName", databaseFile.getName());
//        invite.put("hash", databaseFile.getHash());
//        invite.put("taxDocumentModel", databaseFile.getFileType().toString());
//        invite.put("version", databaseFile.getDetailFour());
//
//
//        this.jmsTemplate.convertAndSend(invite);

        return new FileProcessed(this.databaseFileRepository.saveAndFlush(newDatabaseFile), Collections.EMPTY_LIST);
    }

    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles) {
        this.limitFileService.limitMaximumExceeded(multipartFiles);

        if(!this.permissionContainerService.containerKeyValid(containerKey)) {
            throw new KeyWasNotRegisteredInStorageYetException(HttpStatus.FORBIDDEN);
        }

        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.upload(containerKey,multipartFile));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DatabaseFile getFileHash(String hash) {
        DatabaseFile result = this.databaseFileRepository
                .getByHash(hash)
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

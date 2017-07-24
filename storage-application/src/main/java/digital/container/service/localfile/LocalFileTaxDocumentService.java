package digital.container.service.localfile;

import digital.container.exception.LimitFilesExceededException;
import digital.container.service.container.PermissionContainerService;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.repository.LocalFileRepository;
import digital.container.util.GenerateHash;
import digital.container.util.LocalFileUtil;
import digital.container.util.SaveLocalFile;
import digital.container.util.ValidateNfXML;
import digital.container.vo.TaxDocumentModel;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import io.gumga.presentation.exceptionhandler.GumgaRunTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class LocalFileTaxDocumentService extends GumgaService<LocalFile, Long> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFileTaxDocumentService.class);

    @Autowired
    private LocalFileRepository localFileRepository;
    @Autowired
    private PermissionContainerService permissionContainerService;

    @Autowired
    public LocalFileTaxDocumentService(GumgaCrudRepository<LocalFile, Long> repository) {
        super(repository);
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile) {
        LocalFile localFile = new LocalFile();
        localFile.setName(multipartFile.getOriginalFilename());

        if(!this.permissionContainerService.containerKeyValid(containerKey)) {
            return new FileProcessed(localFile, Arrays.asList("You are not allowed to use the container:"+containerKey));
        }

        localFile.setFileStatus(FileStatus.NOT_SYNC);

        TaxDocumentModel taxDocumentModel = new TaxDocumentModel();
        FileProcessed errors = ValidateNfXML.validate(containerKey, multipartFile, localFile, taxDocumentModel);
        if (errors != null) {
            return errors;
        }

        localFile.setFileType(taxDocumentModel.getFileType());

        Date date = ValidateNfXML.stringToDate(localFile.getDetailTwo());
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENT(containerKey,
                        ld.getYear(),
                        ld.getMonth().toString(),
                        localFile.getFileType(),
                        localFile.getDetailThree());

        File folder = new File(LocalFileUtil.DIRECTORY_PATH + '/' + path);

        folder.mkdirs();

        localFile.setRelativePath(path + '/' + localFile.getName());

        localFile.setContainerKey(containerKey);
        localFile.setCreateDate(Calendar.getInstance());
        localFile.setHash(GenerateHash.generateLocalFile());

        localFile.setContentType(multipartFile.getContentType());
        localFile.setSize(multipartFile.getSize());

        try {
            SaveLocalFile.saveFile(folder, localFile.getName(), multipartFile.getInputStream());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return new FileProcessed(this.localFileRepository.saveAndFlush(localFile), Collections.EMPTY_LIST);
    }

    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles) {
        if(multipartFiles.size() > 500) {
            throw new LimitFilesExceededException(HttpStatus.FORBIDDEN);
        }

        List<FileProcessed> result = new ArrayList<>();

        if(!this.permissionContainerService.containerKeyValid(containerKey)) {
            for(MultipartFile multipartFile : multipartFiles) {
                LocalFile localFile = new LocalFile();
                localFile.setName(multipartFile.getOriginalFilename());
                result.add(new FileProcessed(localFile, Arrays.asList("You are not allowed to use the container:" + containerKey)));
            }
            return result;
        }

        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.upload(containerKey,multipartFile));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public LocalFile getFileHash(String hash) {
        LocalFile result;
        result = this.localFileRepository
                .getByHash(hash)
                .orElseThrow(() -> new GumgaRunTimeException("Não foi encontrado o documento XML com o hash:" + hash, HttpStatus.NOT_FOUND));

        return result;
    }

    @Transactional
    public Boolean deleteFileByHash(String hash) {
        Optional<LocalFile> result = this.localFileRepository.getByHash(hash);
        if(result.isPresent()) {
            delete(result.get());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Transactional
    public Boolean deleteFileById(Long id) {
        LocalFile view = view(id);
        delete(view);
        return Boolean.FALSE;
    }

    @Transactional
    @Override
    public void delete(LocalFile resource) {
        super.delete(resource);
        new File(LocalFileUtil.DIRECTORY_PATH + "/" + resource.getRelativePath()).delete();
    }

}

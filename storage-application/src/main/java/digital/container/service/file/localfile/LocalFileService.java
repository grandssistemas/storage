package digital.container.service.file.localfile;

import digital.container.exception.FileNotFoundException;
import digital.container.service.storage.LimitFileService;
import digital.container.service.storage.MessageStorage;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.repository.file.LocalFileRepository;
import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.util.SaveLocalFile;
import digital.container.util.TokenUtil;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class LocalFileService extends GumgaService<LocalFile, String> {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileService.class);

    private final LocalFileRepository localFileRepository;
    private final LimitFileService limitFileService;

    @Autowired
    public LocalFileService(GumgaCrudRepository<LocalFile, String> repository,
                            LimitFileService limitFileService) {
        super(repository);
        this.localFileRepository = LocalFileRepository.class.cast(repository);
        this.limitFileService = limitFileService;
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile, boolean shared) {
        LocalFile localFile = (LocalFile) new LocalFile()
                .buildAnything(
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getSize(),
                shared,
                containerKey);

        File folder = new File(LocalFileUtil.DIRECTORY_PATH + '/' + LocalFileUtil.getRelativePathFileANYTHING(containerKey));
        folder.mkdirs();

        try {
            SaveLocalFile.saveFile(folder, localFile.getName(), multipartFile.getInputStream());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return new FileProcessed(this.localFileRepository.saveAndFlush(localFile), Collections.emptyList());
    }

    @Transactional
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, boolean shared) {
        this.limitFileService.limitMaximumExceeded(multipartFiles);

        List<FileProcessed> result = new ArrayList<>();
        for(MultipartFile multipartFile : multipartFiles) {
            result.add(this.upload(containerKey,multipartFile, shared));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public LocalFile getFileHash(String hash, Boolean shared) {
        LocalFile result;
        result = this.localFileRepository
                .getByHash(hash, shared)
                .orElseThrow(() -> new FileNotFoundException(MessageStorage.FILE_NOT_FOUND + ":" + hash, HttpStatus.NOT_FOUND));
        return result;
    }

    @Transactional(readOnly = true)
    public LocalFile getFileHash(String hash) {
        LocalFile result;
        result = this.localFileRepository
                .getByHash(hash, TokenUtil.getEndWithOi(), TokenUtil.getContainsSharedOi())
                .orElseThrow(() -> new digital.container.exception.FileNotFoundException(MessageStorage.FILE_NOT_FOUND + ":" + hash, HttpStatus.NOT_FOUND));
        return result;
    }

    @Transactional
    public Boolean deleteFileByHash(String hash) {
        Optional<LocalFile> result = this.localFileRepository.getByHash(hash, TokenUtil.getEndWithOi(), TokenUtil.getContainsSharedOi());
        if(result.isPresent()) {
            delete(result.get());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Transactional
    public Boolean deleteFileById(String id) {
        LocalFile view = view(id);
        delete(view);
        return Boolean.FALSE;
    }

    @Override
    @Transactional
    public void delete(LocalFile resource) {
        super.delete(resource);
        boolean deleted = new File(LocalFileUtil.DIRECTORY_PATH.concat("/").concat(resource.getRelativePath())).delete();
        if(!deleted) {
            LOG.error("Arquivo não foi removido");
        }
    }
}

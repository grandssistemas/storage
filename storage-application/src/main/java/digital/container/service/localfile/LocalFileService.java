package digital.container.service.localfile;

import digital.container.storage.domain.model.FileStatus;
import digital.container.storage.domain.model.FileType;
import digital.container.storage.domain.model.LocalFile;
import digital.container.storage.domain.model.vo.FileProcessed;
import digital.container.repository.LocalFileRepository;
import digital.container.util.GenerateHash;
import digital.container.util.LocalFileUtil;
import digital.container.util.SaveLocalFile;
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
import java.util.*;

@Service
public class LocalFileService extends GumgaService<LocalFile, Long> {
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileService.class);

    @Autowired
    private LocalFileRepository localFileRepository;

    @Autowired
    public LocalFileService(GumgaCrudRepository<LocalFile, Long> repository) {
        super(repository);
    }

    @Transactional
    public FileProcessed upload(String containerKey, MultipartFile multipartFile, boolean shared) {
        LocalFile localFile = new LocalFile();
        localFile.setName(multipartFile.getOriginalFilename());
        localFile.setFileStatus(FileStatus.DO_NOT_SYNC);
        localFile.setFileType(FileType.ANYTHING);
        localFile.setFilePublic(shared);

        File folder = new File(LocalFileUtil.DIRECTORY_PATH + '/' + LocalFileUtil.getRelativePathFileANYTHING(containerKey));
        folder.mkdirs();

        localFile.setRelativePath(LocalFileUtil.getRelativePathFileANYTHING(containerKey) + '/' + localFile.getName());

        localFile.setContainerKey(containerKey);
        localFile.setCreateDate(Calendar.getInstance());
        localFile.setHash(GenerateHash.generate());

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
    public List<FileProcessed> upload(String containerKey, List<MultipartFile> multipartFiles, boolean shared) {
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
                .orElseThrow(() -> new GumgaRunTimeException("Não foi encontrado o documento com o hash:" + hash, HttpStatus.NOT_FOUND));

        return result;
    }

    @Transactional(readOnly = true)
    public LocalFile getFileHash(String hash) {
        LocalFile result;
        result = this.localFileRepository
                .getByHash(hash)
                .orElseThrow(() -> new GumgaRunTimeException("Não foi encontrado o documento com o hash:" + hash, HttpStatus.NOT_FOUND));

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

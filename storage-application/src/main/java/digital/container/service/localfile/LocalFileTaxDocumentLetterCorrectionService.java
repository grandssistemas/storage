package digital.container.service.localfile;

import digital.container.repository.file.LocalFileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentEventLetterCorrectionService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.LocalFileUtil;
import digital.container.util.SaveLocalFile;
import digital.container.util.TokenResultProxy;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class LocalFileTaxDocumentLetterCorrectionService extends GumgaService<LocalFile, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileTaxDocumentLetterCorrectionService.class);
    private LocalFileRepository localFileRepository;

    @Autowired
    private CommonTaxDocumentEventLetterCorrectionService service;

    @Autowired
    private SendMessageMOMService sendMessageMOMService;

    @Autowired
    private SecurityTokenService securityTokenService;

    @Autowired
    public LocalFileTaxDocumentLetterCorrectionService(GumgaCrudRepository<LocalFile, Long> repository) {
        super(repository);
        this.localFileRepository = LocalFileRepository.class.cast(repository);
    }

    private FileProcessed saveFile(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        LocalFile localFile = new LocalFile();
        FileProcessed data = this.service.getData(localFile, multipartFile, containerKey, tokenResultProxy);
        if(data.getErrors().size() > 0) {
            return data;
        }

        File folder = new File(LocalFileUtil.DIRECTORY_PATH + '/' + localFile.getRelativePath().substring(0,localFile.getRelativePath().lastIndexOf('/')));

        folder.mkdirs();
        try {
            SaveLocalFile.saveFile(folder, localFile.getName(), multipartFile.getInputStream());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        this.sendMessageMOMService.send(localFile, containerKey);
        return new FileProcessed(this.localFileRepository.saveAndFlush(localFile), Collections.EMPTY_LIST);
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

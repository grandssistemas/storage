package digital.container.service.file.amazons3;

import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import digital.container.repository.file.FileRepository;
import digital.container.service.message.SendMessageMOMService;
import digital.container.service.taxdocument.CommonTaxDocumentService;
import digital.container.service.token.SecurityTokenService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.AmazonS3Util;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.XMLUtil;
import io.gumga.application.GumgaService;
import io.gumga.core.GumgaThreadScope;
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
public class AmazonS3FileTaxDocumentAnyService extends GumgaService<AmazonS3File, String> {

    private final CommonTaxDocumentService commonTaxDocumentService;
    private final SendMessageMOMService sendMessageMOMService;
    private final SecurityTokenService securityTokenService;
    private final AmazonS3Service amazonS3Service;
    private final FileRepository fileRepository;

    @Autowired
    public AmazonS3FileTaxDocumentAnyService(GumgaCrudRepository<AmazonS3File, String> repository,
                                             CommonTaxDocumentService commonTaxDocumentService,
                                             SendMessageMOMService sendMessageMOMService,
                                             SecurityTokenService securityTokenService,
                                             AmazonS3Service amazonS3Service,
                                             FileRepository fileRepository) {
        super(repository);
        this.commonTaxDocumentService = commonTaxDocumentService;
        this.sendMessageMOMService = sendMessageMOMService;
        this.securityTokenService = securityTokenService;

        this.amazonS3Service = amazonS3Service;
        this.fileRepository = fileRepository;
    }

    public FileProcessed processUpload(String containerKey, MultipartFile multipartFile, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        return this.identifyTaxDocument(containerKey, multipartFile, tokenResultProxy);
    }

    @Transactional
    public List<FileProcessed> processUpload(String containerKey, List<MultipartFile> multipartFiles, String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = this.securityTokenService.searchOiSoftwareHouseAndAccountant(tokenSoftwareHouse, tokenAccountant);
        List<FileProcessed> result = new ArrayList<>();
        List<AbstractFile> files = new ArrayList<>();
//        List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntryList = new ArrayList<>();


        String oi = GumgaThreadScope.organizationCode.get();

//        long startTime = System.currentTimeMillis();
        multipartFiles.stream().parallel()
                .forEach(multipartFile -> {
                    GumgaThreadScope.organizationCode.set(oi);
                    FileProcessed fileProcessed = this.identifyTaxDocument(containerKey, multipartFile, tokenResultProxy);
                    result.add(fileProcessed);

                    if (fileProcessed.getErrors().isEmpty()) {
                        files.add(fileProcessed.getFile());
                    }
                });
//        long endTime = System.currentTimeMillis();

//        System.out.println(containerKey + " Proccess files " + (endTime - startTime) + " milliseconds");
//        startTime = System.currentTimeMillis();
        if (!files.isEmpty()) {
            int index = 0;
            sendteste(result);
            for (AbstractFile file : files) {
                this.fileRepository.save(file);
                if (index % 100 == 0) {
                    index = 0;
                    this.fileRepository.flush();
                }
                index++;
            }
            if (index <= 100) {
                this.fileRepository.flush();
            }

        }
//        endTime = System.currentTimeMillis();
//        System.out.println(containerKey + " Save/SEnd files " + (endTime - startTime) + " milliseconds");
        return result;
    }

    private void sendteste(List<FileProcessed> result) {
        result
                .stream()
                .filter(f -> f.getErrors() != null && f.getErrors().isEmpty() && f.getXml() != null)
                .forEach(f -> {
                    this.sendMessageMOMService.sendInviteAmazon(f.getFile(), f.getFile().getContainerKey(), f.getXml());
                });
    }

    public FileProcessed identifyTaxDocument(String containerKey, MultipartFile multipartFile, TokenResultProxy tokenResultProxy) {
        AmazonS3File amazonS3File = new AmazonS3File();
        String xml = XMLUtil.getXml(multipartFile);
        FileProcessed fileProcessed = this.commonTaxDocumentService.identifyTaxDocument(amazonS3File, containerKey, multipartFile, tokenResultProxy, xml);

        if ((fileProcessed != null && fileProcessed.getErrors() != null && !fileProcessed.getErrors().isEmpty())) {

            return fileProcessed;
        }

        fileProcessed.setXml(xml);
        return fileProcessed;
//        return processToSaveAmazonS3(containerKey, multipartFile, amazonS3File, xml);
//        return saveFile(containerKey, multipartFile, amazonS3File);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessed processToSaveAmazonS3(String containerKey, MultipartFile multipartFile, AmazonS3File amazonS3File, String xml) {
        FileProcessed fileProcessed = new FileProcessed(this.repository.save(amazonS3File), Collections.emptyList());
        this.sendMessageMOMService.sendInviteAmazon(amazonS3File, containerKey, xml);

        return fileProcessed;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileProcessed saveFile(String containerKey, MultipartFile multipartFile, AmazonS3File amazonS3File) {

        this.amazonS3Service.send(amazonS3File, multipartFile, Boolean.FALSE, AmazonS3Util.TAX_DOCUMENT_BUCKET);
        this.sendMessageMOMService.send(amazonS3File, containerKey);

        return new FileProcessed(this.repository.saveAndFlush(amazonS3File), Collections.emptyList());
    }
}

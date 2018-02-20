package digital.container.service.message;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digital.container.repository.file.AmazonS3FileRepository;
import digital.container.repository.file.FileRepository;
import digital.container.service.file.databasefile.DatabaseFileService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.util.SaveLocalFile;
import io.gumga.core.GumgaThreadScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SendMessageMOMService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageMOMService.class);

    @Autowired
    private AmazonSQSAsync amazonSQS;
    @Autowired
    private DatabaseFileService databaseFileService;
//    @Autowired
//    private AmazonS3FileRepository amazonS3FileRepository;

    public void send(AbstractFile file, String containerKey) {
        try {
            Map invite = createInvete(file, containerKey, file.getOi().getValue());

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));
            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));


            file.setFileStatus(FileStatus.WAS_SENT_TO_MOM);
            this.amazonSQS.sendMessage(sendMessageRequest);

//            this.jmsTemplate.convertAndSend(invite);

        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        } finally {
//            this.fileRepository.saveAndFlush(file);
        }
    }

    public void send(AbstractFile file) {
        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));
            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));

            file.setFileStatus(FileStatus.WAS_SENT_TO_MOM);
            this.amazonSQS.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        } finally {
//            this.fileRepository.saveAndFlush(file);
        }
    }

//    @Async
    public void sendInviteAmazon(AbstractFile file, String containerKey, String xml) {
        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());
            invite.put("type", "XML");
            invite.put("xml", xml);
            invite.put("awss3", "YES");
            invite.put("bucket", System.getProperty("amazon.s3.tax_document_bucket"));

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));

            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));
            file.setFileStatus(FileStatus.WAS_SENT_TO_MOM);
//            amazonS3FileRepository.changeStatusByID(file.getId(), FileStatus.WAS_SENT_TO_MOM);
            this.amazonSQS.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
//            amazonS3FileRepository.changeStatusByID(file.getId(), FileStatus.FAILED_SYNC_IN_SEND_TO_MOM_BUT_WAS_SAVED_CONTINGENCY);
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM_BUT_WAS_SAVED_CONTINGENCY);
            createFile(file, xml);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        } finally {

        }
    }

    private void createFile(AbstractFile file, String xml) {
        DatabaseFile databaseFile = new DatabaseFile();
        databaseFile.setName(file.getName());
        databaseFile.setHash(file.getHash());
        databaseFile.setContentType(file.getContentType());
        databaseFile.setFileType(FileType.TAX_DOCUMENT_IN_CONTINGENCY);
        databaseFile.setSize(file.getSize());
        databaseFileService.saveDatabaseFile(databaseFile, xml);

//        File folder = new File(LocalFileUtil.DIRECTORY_PATH + '/' + file.getRelativePath().substring(0,file.getRelativePath().lastIndexOf('/')));
//        folder.mkdirs();
//        SaveLocalFile.saveFile(folder, file.getName(), new ByteArrayInputStream(xml.getBytes()));
    }
/*
    public SendMessageBatchRequestEntry createSendMessageBatchRequestEntry(AbstractFile file, String containerKey, String xml) {
        SendMessageBatchRequestEntry sendMessageBatchRequestEntry = new SendMessageBatchRequestEntry();

        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());
            ObjectMapper objectMapper = new ObjectMapper();

            invite.put("type", "XML");
            invite.put("xml", xml);
            invite.put("awss3", "YES");
            invite.put("relativePath", file.getRelativePath());
            invite.put("bucket", System.getProperty("amazon.s3.tax_document_bucket"));
            file.setFileStatus(FileStatus.WAS_SENT_TO_MOM);
            sendMessageBatchRequestEntry.setId(file.getHash());
            sendMessageBatchRequestEntry.setMessageBody(objectMapper.writeValueAsString(invite));
        } catch (JsonProcessingException e) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), e);
            return null;
        }

        return sendMessageBatchRequestEntry;
    }

    public void sendInviteAmazon(List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntryList) {
        if(!sendMessageBatchRequestEntryList.isEmpty()) {
            SendMessageBatchRequest send_batch_request = new SendMessageBatchRequest()
                    .withQueueUrl(System.getProperty("mom.queue"))
                    .withEntries(sendMessageBatchRequestEntryList);
            this.amazonSQS.sendMessageBatch(send_batch_request);
        }
        LOGGER.info("Messages send AWS SQS --> " + sendMessageBatchRequestEntryList.size());
    }

*/
    private Map createInvete(AbstractFile file, String containerKey, String oi) {
        Map invite = new HashMap();
        invite.put("container", containerKey);
        invite.put("fileName", file.getName());
        invite.put("hash", file.getHash());
        invite.put("taxDocumentModel", file.getFileType().toString());
        invite.put("oi", oi);
        invite.put("sizeFile", file.getSize());
        invite.put("gumgaOrganizations", file.getGumgaOrganizations() == null ? "" : file.getGumgaOrganizations());
        invite.put("type", "NO_XML");
        invite.put("awss3", "NO");
        invite.put("relativePath", file.getRelativePath());
        return invite;
    }

}

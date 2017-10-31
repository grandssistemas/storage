package digital.container.service.message;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.amazon.AmazonS3File;
import io.gumga.core.GumgaThreadScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SendMessageMOMService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageMOMService.class);

//    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private AmazonSQSAsync amazonSQS;

    public void send(AbstractFile file, String containerKey) {
        try {
            Map invite = createInvete(file, containerKey, file.getOi().getValue());

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));
            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));
//            MessageAttributeValue messageAttributeValue = new MessageAttributeValue();
//            sendMessageRequest.setMessageAttributes(invite);
            this.amazonSQS.sendMessage(sendMessageRequest);

//            this.jmsTemplate.convertAndSend(invite);

        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        }
    }

    public void send(AbstractFile file) {
        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));
            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));
//            sendMessageRequest.setMessageAttributes(invite);
            this.amazonSQS.sendMessage(sendMessageRequest);
//            this.jmsTemplate.convertAndSend(invite);
        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        }
    }

//    @Async
    public void sendInviteAmazon(AbstractFile file, String containerKey, String xml) {
        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());
            invite.put("type", "XML");
            invite.put("xml", xml);
            invite.put("awss3", "YES");
            invite.put("relativePath", file.getRelativePath());
            invite.put("bucket", System.getProperty("amazon.s3.tax_document_bucket"));

            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));
            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));

            this.amazonSQS.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        }
    }

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
            sendMessageBatchRequestEntry.setId(file.getHash());
            sendMessageBatchRequestEntry.setMessageBody(objectMapper.writeValueAsString(invite));
        } catch (JsonProcessingException e) {
            file.setFileStatus(FileStatus.FAILED_SYNC_IN_SEND_TO_MOM);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), e);
            return null;
        }

        return sendMessageBatchRequestEntry;
    }

//    @Async
    public void sendInviteAmazon(List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntryList) {
        if(!sendMessageBatchRequestEntryList.isEmpty()) {
            SendMessageBatchRequest send_batch_request = new SendMessageBatchRequest()
                    .withQueueUrl(System.getProperty("mom.queue"))
                    .withEntries(sendMessageBatchRequestEntryList);
            this.amazonSQS.sendMessageBatch(send_batch_request);
        }
        LOGGER.info("Messages send AWS SQS --> " + sendMessageBatchRequestEntryList.size());
    }


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
        return invite;
    }

}

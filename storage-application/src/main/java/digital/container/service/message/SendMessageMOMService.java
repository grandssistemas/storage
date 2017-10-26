package digital.container.service.message;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
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
import java.util.Map;

@Service
public class SendMessageMOMService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageMOMService.class);

//    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private AmazonSQS amazonSQS;

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
            file.setFileStatus(FileStatus.FAILED_SYNC);
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
            file.setFileStatus(FileStatus.FAILED_SYNC);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        }
    }

    public void sendInviteAmazon(AbstractFile file, String containerKey, String xml) {
        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());
            invite.put("type", "XML");
            invite.put("xml", xml);
            invite.put("awss3", "YES");
            SendMessageRequest sendMessageRequest = new SendMessageRequest();
            sendMessageRequest.setQueueUrl(System.getProperty("mom.queue"));
            ObjectMapper objectMapper = new ObjectMapper();
            sendMessageRequest.setMessageBody(objectMapper.writeValueAsString(invite));
            this.amazonSQS.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        }
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

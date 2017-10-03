package digital.container.service.message;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import io.gumga.core.GumgaThreadScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SendMessageMOMService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessageMOMService.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(AbstractFile file, String containerKey) {
        try {
            Map invite = createInvete(file, containerKey, GumgaThreadScope.organizationCode.get());

            this.jmsTemplate.convertAndSend(invite);
        } catch (Exception ex) {
            file.setFileStatus(FileStatus.FAILED_SYNC);
            LOGGER.error("Erro ao enviar o arquivo:"+file.getHash(), ex);
        }
    }

    public void send(AbstractFile file) {
        try {
            Map invite = createInvete(file, file.getContainerKey(), file.getOi().getValue());

            this.jmsTemplate.convertAndSend(invite);
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
        invite.put("gumgaOrganizations", file.getGumgaOrganizations());

        return invite;
    }
}

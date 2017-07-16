package digital.container.storage.api;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.util.Map;

/**
 * Created by felipesabadinifacina on 16/07/17.
 */
@Component
public class MessageReceiver {

    private static final String ORDER_QUEUE = "tax.document.queue";

    @JmsListener(destination = ORDER_QUEUE)
    public void receiveMessage(final Message<Map> message) throws JMSException {

        System.out.println(message.getPayload());
//        LOG.info("----------------------------------------------------");
//        MessageHeaders headers =  message.getHeaders();
//        LOG.info("Application : headers received : {}", headers);
//
//        Product product = message.getPayload();
//        LOG.info("Application : product : {}",product);
//
//        orderService.processOrder(product);
//        LOG.info("----------------------------------------------------");

    }

}

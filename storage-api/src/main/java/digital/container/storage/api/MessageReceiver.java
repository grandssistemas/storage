package digital.container.storage.api;

import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.jms.JMSException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by felipesabadinifacina on 16/07/17.
 */
@Component
public class MessageReceiver {

    private static final String ORDER_QUEUE = "tax.document.queue";

//    @JmsListener(destination = ORDER_QUEUE)
//    public void receiveMessage(final Message<Map> message) throws JMSException {
//        Map result = message.getPayload();
//        String url = "http://localhost:8080/storage-api/api/file-hash/" + result.get("hash");
//        String toFile = "/Users/gumgait/temp/"+result.get("fileName").toString();
//        try {
//
//            URL website = new URL(url);
//            File file = new File(toFile);
//            FileUtils.copyURLToFile(website, file, 10000, 10000);
//            file.delete();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(message.getPayload());
//    }

}

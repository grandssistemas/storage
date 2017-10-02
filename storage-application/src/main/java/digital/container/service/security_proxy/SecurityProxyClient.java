package digital.container.service.security_proxy;

import digital.container.util.TokenUtil;
import io.gumga.core.GumgaThreadScope;
import io.gumga.presentation.api.GumgaJsonRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.OkHttpClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SecurityProxyClient {

    private RestTemplate restTemplate = new GumgaJsonRestTemplate();
    private HttpEntity<Map> requestEntity;
    private String url = System.getProperty("url.host") + "/security-api";


    public SecurityProxyClient () {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json;charset=utf-8");
        headers.set("gumgaToken", GumgaThreadScope.gumgaToken.get());
        this.requestEntity = new HttpEntity(headers);
    }

    public String searchOiByToken(String token) {
        ResponseEntity<Map> exchange = this.restTemplate.exchange(this.url.concat("/publicoperations/token/get/" + token + "/"), HttpMethod.GET, (HttpEntity<?>) this.requestEntity, Map.class, new HashMap<>());
        if(exchange.getStatusCode().equals(HttpStatus.OK)) {
            Map body = exchange.getBody();

            return (String) Optional.ofNullable(body.get("organizationHierarchyCode")).orElse(TokenUtil.NO_FOUND_OI);
        }

        return "NO_OI";
    }
}

package digital.container.service.token;

import digital.container.service.security_proxy.SecurityProxyClient;
import digital.container.util.TokenResultProxy;
import digital.container.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SecurityTokenService {

    @Autowired
    private SecurityProxyClient securityProxyClient;

    public TokenResultProxy searchOiSoftwareHouseAndAccountant(String tokenSoftwareHouse, String tokenAccountant) {
        TokenResultProxy tokenResultProxy = new TokenResultProxy();

        if(!TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN.equals(tokenSoftwareHouse)) {
            String oiSoftwareHouse = this.securityProxyClient.searchOiByToken(tokenSoftwareHouse);
            if(!oiSoftwareHouse.equals(TokenUtil.NO_FOUND_OI)) {
                tokenResultProxy.softwareHouseOi = oiSoftwareHouse;
            }
        }

        if(!TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN.equals(tokenAccountant)) {
            String oiAccountant = this.securityProxyClient.searchOiByToken(tokenAccountant);
            if (!oiAccountant.equals(TokenUtil.NO_FOUND_OI)) {
                tokenResultProxy.accountantOi = oiAccountant;
            }
        }

        return tokenResultProxy;

    }
}

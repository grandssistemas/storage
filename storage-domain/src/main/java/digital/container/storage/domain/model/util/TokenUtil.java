package digital.container.storage.domain.model.util;

import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;

public final class TokenUtil {

    protected TokenUtil(){}

    public static final String SOFTWARE_HOUSE_NO_HAVE_TOKEN = "NO_TOKEN_SOFTWARE_HOUSE";
    public static final String ACCOUNTANT_NO_HAVE_TOKEN = "NO_TOKEN_ACCOUNTANT";
    public static final String NO_FOUND_OI = "NO_OI";


    public static GumgaOi getEndWithOi() {
        return new GumgaOi(GumgaThreadScope.organizationCode.get()+"%");
    }

    public static String getContainsSharedOi() {
        return "%,"+GumgaThreadScope.organizationCode.get()+",%";
    }
}

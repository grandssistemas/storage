package digital.container.util;


import javax.persistence.Embeddable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchXMLUtil {
    private static final String EMPTY = "";

    private SearchXMLUtil(){}

    public static String getEmitCNPJ(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<emit>.*<CNPJ>(.*)<\\/CNPJ>.*<\\/emit>"))
                .orElse(EMPTY);
    }

    public static String getInfProtChNFe(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infProt>.*<chNFe>(.*)<\\/chNFe>.*<\\/infProt>"))
                .orElse(EMPTY);
    }


    private static String searchGroup1(String xml, String regex) {
        String result = null;
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(xml);
        while (matcher.find()) {
            if(matcher.groupCount() > 0) {
                result = matcher.group(1);
            }
        }
        return result;
    }

    public static String getIdeMod(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<ide>.*<mod>(.*)<\\/mod>.*<\\/ide>"))
                .orElse(EMPTY);
    }
}

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

    public static String getIdeDhEmi(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<ide>.*<dhEmi>(.*)<\\/dhEmi>.*<\\/ide>"))
                .orElse(getIdeDEmi(xml));
    }

    private static String getIdeDEmi(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<ide>.*<dEmi>(.*)<\\/dEmi>.*<\\/ide>"))
                .orElse(EMPTY);
    }

    public static String getIdeTpNF(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<ide>.*<tpNF>(.*)<\\/tpNF>.*<\\/ide>"))
                .orElse(EMPTY);
    }

    public static String getIdeMod(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<ide>.*<mod>(.*)<\\/mod>.*<\\/ide>"))
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
    }

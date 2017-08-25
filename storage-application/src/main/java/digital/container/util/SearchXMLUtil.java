package digital.container.util;


import javax.persistence.Embeddable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchXMLUtil {
    private static final String EMPTY = "";

    private SearchXMLUtil() {
    }

    public static String getEmitCNPJ(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<emit>.*<CNPJ>(.*)<\\/CNPJ>.*<\\/emit>"))
                .orElse(EMPTY);
    }

    public static String getInfProtChNFe(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infProt.*<chNFe>(.*)<\\/chNFe>.*<\\/infProt>"))
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

    public static String getVersion(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<nfeProc.*versao=\"(.{4})\""))
                .orElse(Optional.ofNullable(searchGroup1(xml, "<procNFe.*versao=\"(.{4})\""))
                        .orElse(EMPTY));

    }

    public static String getInfEventoXevento(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infEvento.*<xEvento>(.*)<\\/xEvento>.*<\\/infEvento>"))
                .orElse(EMPTY);
    }

    public static String getInfEventochNFe(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infEvento.*<chNFe>(.*)<\\/chNFe>.*<\\/infEvento>"))
                .orElse(EMPTY);
    }

    public static String getInfEventochDhRegEvento(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infEvento.*<dhRegEvento>(.*)<\\/dhRegEvento>.*<\\/infEvento>"))
                .orElse(EMPTY);
    }

    public static String getInfInutXServ(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infInut.*<xServ>(.*)<\\/xServ>.*<\\/infInut>"))
                .orElse(EMPTY);
    }

    public static String getInfInutXMotivo(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infInut.*<xMotivo>(.*)<\\/xMotivo>.*<\\/infInut>"))
                .orElse(EMPTY);
    }

    public static String getInfInutCNPJ(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infInut.*<CNPJ>(.*)<\\/CNPJ>.*<\\/infInut>"))
                .orElse(EMPTY);
    }

    public static String getInfInutMod(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infInut.*<mod>(.*)<\\/mod>.*<\\/infInut>"))
                .orElse(EMPTY);
    }

    public static String getInfInutNProt(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infInut.*<nProt>(.*)<\\/nProt>.*<\\/infInut>"))
                .orElse(EMPTY);
    }

    public static String getInfInutDhRecbto(String xml) {
        return Optional
                .ofNullable(searchGroup1(xml, "<infInut.*<dhRecbto>(.*)<\\/dhRecbto>.*<\\/infInut>"))
                .orElse(EMPTY);
    }

    private static String searchGroup1(String xml, String regex) {
        String result = null;
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(xml);
        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                result = matcher.group(1);
            }
        }
        return result;
    }


    private static String searchGroup2(String xml, String regex) {
        String result = null;
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(xml);
        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                result = matcher.group(2);
            }
        }
        return result;
    }
}

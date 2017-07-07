package digital.container.util;


import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.vo.TaxDocumentModel;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValidateNfXML {
    private ValidateNfXML() {}

    public static FileProcessed validate(String containerKey, MultipartFile multipartFile, AbstractFile file, TaxDocumentModel taxDocumentModel) {
        try(InputStream inputStream = multipartFile.getInputStream()) {

            List<String> errors = new ArrayList<>();
            String xml = IOUtils.toString(inputStream, "UTF8");

            if(!SearchXMLUtil.getEmitCNPJ(xml).equals(containerKey)) {
                errors.add("O CNPJ do XML é diferente da chave do container informada.");
            }
            taxDocumentModel.model = SearchXMLUtil.getIdeMod(xml);

            if(!taxDocumentModel.isValid()) {
                errors.add("Não suportamos o modelo informado no seu xml.");
            }

            String chNFe = SearchXMLUtil.getInfProtChNFe(xml);
            String dhEmi = SearchXMLUtil.getIdeDhEmi(xml);
            String tpNF = SearchXMLUtil.getIdeTpNF(xml);
            tpNF = tpNF.equals("1") ? "SAIDA" : "ENTRADA";

            if(errors.size() > 0) {
                return new FileProcessed(file, errors);
            }

            file.setDetailOne(chNFe);
            file.setDetailTwo(dhEmi);
            file.setDetailThree(tpNF);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Date stringToDate(String data) {
        Date parse = null;
        SimpleDateFormat format = null;
        try {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            parse = format.parse(data);
        } catch (Exception e) {
            format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                parse = format.parse(data);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }

        return parse;
    }
}

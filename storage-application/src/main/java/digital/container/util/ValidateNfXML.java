package digital.container.util;


import digital.container.storage.domain.model.AbstractFile;
import digital.container.storage.domain.model.vo.FileProcessed;
import digital.container.vo.TaxDocumentModel;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

            if(errors.size() > 0) {
                return new FileProcessed(file, errors);
            }

            file.setDetailOne(chNFe);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

package digital.container.util;


import digital.container.service.storage.MessageStorage;
import digital.container.service.taxdocument.SearchTaxDocumentService;
import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.vo.TaxDocumentModel;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ValidateNfXML {

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    private ValidateNfXML() {}

    public FileProcessed validate(String containerKey, MultipartFile multipartFile, AbstractFile file, TaxDocumentModel taxDocumentModel) {
        try(InputStream inputStream = multipartFile.getInputStream()) {

            List<String> errors = new ArrayList<>();
            String xml = IOUtils.toString(inputStream, "UTF8");

            String chNFe = SearchXMLUtil.getInfProtChNFe(xml);
            AbstractFile fileFromDB = this.searchTaxDocumentService.getFileByGumgaOIAndChNFeAndNF(new GumgaOi(GumgaThreadScope.organizationCode.get() + "%"), chNFe);

            if(fileFromDB != null) {
                errors.add(MessageStorage.TAX_DOCUMENT_WITH_THIS_ACCESS_KEY_ALREADY_EXISTS);
                return new FileProcessed(file, errors);
            }

            String tpNF = SearchXMLUtil.getIdeTpNF(xml);
            tpNF = tpNF.equals("1") ? "SAIDA" : "ENTRADA";
            String emitCNPJ = SearchXMLUtil.getEmitCNPJ(xml);
            if(!emitCNPJ.equals(containerKey) && tpNF.equals("SAIDA")) {
                errors.add(MessageStorage.CNPJ_OF_XML_IS_DIFFERENT_CONTAINER_KEY);
            }
            taxDocumentModel.model = SearchXMLUtil.getIdeMod(xml);

            if(!taxDocumentModel.isValid()) {
                errors.add(MessageStorage.WE_DONT_SUPPORT_TEMPLATE_REPORTED_IN_YOUR_XML);
            }

            String dhEmi = SearchXMLUtil.getIdeDhEmi(xml);
            String version = SearchXMLUtil.getVersion(xml);

            if(errors.size() > 0) {
                return new FileProcessed(file, errors);
            }

            file.setDetailOne(chNFe);
            file.setDetailTwo(dhEmi);
            file.setDetailThree(tpNF);
            file.setDetailFour(version);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Date stringToDate(String data) {
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

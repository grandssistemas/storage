package digital.container.service.taxdocument;

import digital.container.service.storage.MessageStorage;
import digital.container.storage.domain.model.file.*;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.local.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.storage.domain.model.util.GenerateHash;
import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.storage.domain.model.util.TokenUtil;
import digital.container.util.*;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class CommonTaxDocumentEventLetterCorrectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTaxDocumentEventLetterCorrectionService.class);

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private ValidateNfXML validateNfXML;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy) {
        List<String> errors = new ArrayList<>();
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        String xml = XMLUtil.getXml(multipartFile);

        FileProcessed fileProcessed = validateLetterCorrectionEvent(file, xml);
        if(!fileProcessed.getErrors().isEmpty()) {
            return fileProcessed;
        }

        String infInutCNPJ = SearchXMLUtil.getInfEventoCNPJ(xml);
        String chNFe = SearchXMLUtil.getInfEventochNFe(xml);

        if(infInutCNPJ.isEmpty()) {
            if(!chNFe.isEmpty() && chNFe.length() > 20) {
                infInutCNPJ = chNFe.substring(6, 20);
            }
        }

        if(!containerKey.equals(infInutCNPJ)) {
            errors.add(MessageStorage.CNPJ_OF_XML_IS_DIFFERENT_CONTAINER_KEY);
            return new FileProcessed(file, errors);
        }

        String infInutDhRecbto = SearchXMLUtil.getInfEventoDhEvento(xml);
        if(infInutDhRecbto.isEmpty()) {
            errors.add(MessageStorage.CORRECTION_LETTER_EVENT_HAVE_RECEIPT_DATE_TIME);
            return new FileProcessed(file, errors);
        }



        AbstractFile fileFromDB = this.searchTaxDocumentService.getFileByGumgaOIAndNProtAndNFLetterCorrection(getGumgaOiToHQL(), chNFe);
        if(fileFromDB != null) {
            errors.add(MessageStorage.CORRECTION_LETTER_EVENT_ALREADY_EXISTS);
            return new FileProcessed(file, errors);
        }

        AbstractFile taxDocument = this.searchTaxDocumentService.getFileByGumgaOIAndChNFeAndNF(getGumgaOiToHQL(), chNFe);
        if(taxDocument == null) {
            errors.add(MessageStorage.THERE_ISNT_TAX_DOCUMENT_THIS_ACCESS_KEY);
            return new FileProcessed(file, errors);
        }

        Date date = validateNfXML.stringToDate(infInutDhRecbto);
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        file.buildTaxDocumentLetterCorrection(infInutDhRecbto, chNFe, containerKey, multipartFile.getContentType(), multipartFile.getSize(), ld, tokenResultProxy);

        return new FileProcessed(file, errors);
    }

    private FileProcessed validateLetterCorrectionEvent(AbstractFile file, String xml) {
        List<String> errors = new ArrayList<>();

        if(!isLetterCorrectionEvent(xml)) {
            errors.add(MessageStorage.ISNT_LETTER_CORRECTION_EVENT);
        }

        return new FileProcessed(file, errors);
    }

    public Boolean isLetterCorrectionEvent(String xml) {
        String tpEvento = SearchXMLUtil.getInfEventoTpEvento(xml);
        return "110110".equals(tpEvento);
    }


    private GumgaOi getGumgaOiToHQL() {
        return new GumgaOi(GumgaThreadScope.organizationCode.get()+"%");
    }


}

package digital.container.service.taxdocument;

import digital.container.service.storage.MessageStorage;
import digital.container.storage.domain.model.file.*;
import digital.container.vo.FileProcessed;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.util.*;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommonTaxDocumentEventDisableService {

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private ValidateNfXML validateNfXML;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy) {
        String xml = XMLUtil.getXml(multipartFile);
        return getData(file, multipartFile, containerKey, tokenResultProxy, xml);
    }

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy, String xml) {
        List<String> errors = new ArrayList<>();
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        FileProcessed fileProcessed = validateDisableEvent(file, xml);
        if(!fileProcessed.getErrors().isEmpty()) {
            return fileProcessed;
        }

        String infInutCNPJ = SearchXMLUtil.getInfInutCNPJ(xml);
        if(!containerKey.equals(infInutCNPJ)) {
            errors.add(MessageStorage.CNPJ_OF_XML_IS_DIFFERENT_CONTAINER_KEY);
            return new FileProcessed(file, errors);
        }

        String infInutDhRecbto = SearchXMLUtil.getInfInutDhRecbto(xml);
        if(infInutDhRecbto.isEmpty()) {
            errors.add(MessageStorage.DISABLING_EVENT_MUST_HAVE_THE_RECEIVING_DATE_TIME);
            return new FileProcessed(file, errors);
        }

        String infInutNProt = SearchXMLUtil.getInfInutNProt(xml);
        if(infInutDhRecbto.isEmpty()) {
            errors.add(MessageStorage.DISABLE_EVENT_MUST_HAVE_PROTOCOL_NUMBER);
            return new FileProcessed(file, errors);
        }

        AbstractFile fileFromDB = this.searchTaxDocumentService.getFileByGumgaOIAndNProtAndNFDisable(getGumgaOiToHQL(), infInutNProt);
        if(fileFromDB != null) {
            errors.add(MessageStorage.DISABLE_EVENT_ALREADY_EXISTS);
            return new FileProcessed(file, errors);
        }

        String mod = SearchXMLUtil.getInfInutMod(xml);
        FileType fileType = null;

        switch (mod) {
            case "55":
                fileType = FileType.NFE_DISABLE;
                break;
            case "65":
                fileType = FileType.NFCE_DISABLE;
                break;
            default:
                errors.add(MessageStorage.WE_DONT_SUPPORT_TEMPLATE_REPORTED_IN_YOUR_XML);
                return new FileProcessed(file, errors);
        }
        Date date = validateNfXML.stringToDate(infInutDhRecbto);
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


        file.buildTaxDocumentDisable(infInutNProt, infInutDhRecbto, multipartFile.getContentType(), multipartFile.getSize(), Boolean.FALSE, containerKey, tokenResultProxy, ld, fileType);

        return new FileProcessed(file, errors);
    }

    public Boolean isDisableEvent(String xml) {
        String xServ = SearchXMLUtil.getInfInutXServ(xml);
        String xMotivo = SearchXMLUtil.getInfInutXMotivo(xml);
        String infProtXMotivo = SearchXMLUtil.getInfProtXMotivo(xml);

        if(((infProtXMotivo.isEmpty() || infProtXMotivo.toLowerCase().contains("autorizado")) && xServ.isEmpty() && xMotivo.isEmpty()) || (!xMotivo.isEmpty() && !xMotivo.toLowerCase().contains("inutilizacao")) || (!xServ.isEmpty() && !"INUTILIZAR".equalsIgnoreCase(xServ))) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private FileProcessed validateDisableEvent(AbstractFile file, String xml) {
        List<String> errors = new ArrayList<>();
        String xServ = SearchXMLUtil.getInfInutXServ(xml);
        String xMotivo = SearchXMLUtil.getInfInutXMotivo(xml);

        if((!xMotivo.isEmpty() && !xMotivo.toLowerCase().contains("inutilizacao")) || (!xServ.isEmpty() && !"INUTILIZAR".equalsIgnoreCase(xServ))) {
            errors.add(MessageStorage.ISNT_DISABLING_EVENT);
        }

        return new FileProcessed(file, errors);
    }

    private GumgaOi getGumgaOiToHQL() {
        return new GumgaOi(GumgaThreadScope.organizationCode.get()+"%");
    }


}

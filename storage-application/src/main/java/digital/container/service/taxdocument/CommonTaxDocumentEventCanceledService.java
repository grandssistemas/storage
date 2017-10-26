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
import java.util.*;

@Service
public class CommonTaxDocumentEventCanceledService {

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private ValidateNfXML validateNfXML;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy) {
        String xml = XMLUtil.getXml(multipartFile);

        return getData(file, multipartFile, containerKey, tokenResultProxy, xml);
    }

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy, String xml) {
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        FileProcessed fileProcessed = validateCancellationEvent(file, xml);
        if(!fileProcessed.getErrors().isEmpty()) {
            return fileProcessed;
        }

        List<String> errors = new ArrayList<>();
        AbstractFile taxDocument = getTaxDocument(xml);


        FileType fileType = taxDocument.getFileType();

        switch (fileType) {
            case NFE:
                file.setFileType(FileType.NFE_CANCELED);
                break;
            case NFCE:
                file.setFileType(FileType.NFCE_CANCELED);
                break;
            default:
                errors.add(MessageStorage.WE_DONT_SUPPORT_TEMPLATE_REPORTED_IN_YOUR_XML);
                return new FileProcessed(file, errors);
        }
        String infEventochDhRegEvento = SearchXMLUtil.getInfEventochDhRegEvento(xml);

        Date date = validateNfXML.stringToDate(infEventochDhRegEvento);
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String chNFe = getChNFe(xml);

        file.buildTaxDocumentCanceled(chNFe, infEventochDhRegEvento, taxDocument.getDetailThree(), containerKey, multipartFile.getContentType(), multipartFile.getSize(), ld, tokenResultProxy);

        return new FileProcessed(file, errors);
    }

    private FileProcessed validateCancellationEvent(AbstractFile file, String xml) {
        List<String> errors = new ArrayList<>();

        if(!isCancellationEvent(xml)) {
            errors.add(MessageStorage.FILE_ISNT_CANCELLATION_EVENT_TAX_DOCUMENT);
            return new FileProcessed(file, errors);
        }

        if(existsCancellationEventToChNFe(xml)) {
            errors.add(MessageStorage.CANCEL_EVENT_ALREADY_EXISTIS_THIS_ACCESS_KEY);
            return new FileProcessed(file, errors);
        }

        AbstractFile taxDocument = getTaxDocument(xml);
        if(taxDocument == null) {
            errors.add(MessageStorage.THERE_ISNT_TAX_DOCUMENT_THIS_ACCESS_KEY);
            return new FileProcessed(file, errors);
        }

        return new FileProcessed(file, errors);
    }

    public Boolean isCancellationEvent(String xml) {
        String xEvento = SearchXMLUtil.getInfEventoXevento(xml);
        return "Cancelamento".equalsIgnoreCase(xEvento);
    }

    private Boolean existsCancellationEventToChNFe(String xml) {
        String chNFe = getChNFe(xml);
        AbstractFile file = this.searchTaxDocumentService.getFileByGumgaOIAndChNFeAndNFCanceled(getGumgaOiToHQL(), chNFe);
        return file == null ? Boolean.FALSE : Boolean.TRUE;
    }

    private AbstractFile getTaxDocument(String xml) {
        String chNFe = getChNFe(xml);
        return this.searchTaxDocumentService.getFileByGumgaOIAndChNFeAndNF(getGumgaOiToHQL(), chNFe);

    }

    private GumgaOi getGumgaOiToHQL() {
        return new GumgaOi(GumgaThreadScope.organizationCode.get()+"%");
    }

    private String getChNFe(String xml) {
        return SearchXMLUtil.getInfEventochNFe(xml);
    }
}

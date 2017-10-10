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
import java.util.*;

@Service
public class CommonTaxDocumentEventCanceledService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTaxDocumentEventCanceledService.class);

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private ValidateNfXML validateNfXML;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey, TokenResultProxy tokenResultProxy) {
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        String xml = XMLUtil.getXml(multipartFile);

        FileProcessed fileProcessed = validateCancellationEvent(file, xml);
        if(fileProcessed.getErrors().size() > 0) {
            return fileProcessed;
        }

        List<String> errors = new ArrayList<>();
        AbstractFile taxDocument = getTaxDocument(xml);

        FileType fileType = FileType.INVALID_TAX_DOCUMENT;
        fileType = taxDocument.getFileType();

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

        if(!TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN.equals(tokenResultProxy.accountantOi)) {
            file.addOrganization(tokenResultProxy.accountantOi);
        }

        if(!TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN.equals(tokenResultProxy.softwareHouseOi)) {
            file.addOrganization(tokenResultProxy.softwareHouseOi);
        }

        String chNFe = getChNFe(xml);
        file.setDetailOne(chNFe);

        String infEventochDhRegEvento = SearchXMLUtil.getInfEventochDhRegEvento(xml);
        file.setDetailTwo(infEventochDhRegEvento);

        String movement = taxDocument.getDetailThree();
        Date date = validateNfXML.stringToDate(taxDocument.getDetailTwo());
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTCanceled(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                file.getFileType(),
                movement);

        if(file instanceof LocalFile) {
            ((LocalFile)file).setRelativePath(path + '/' + file.getName());
            file.setHash(GenerateHash.generateLocalFile());
        } else {
            ((DatabaseFile)file).setRelativePath(path + '/' + file.getName());
            file.setHash(GenerateHash.generateDatabaseFile());
        }

        file.setContainerKey(containerKey);
        file.setCreateDate(Calendar.getInstance());


        file.setContentType(multipartFile.getContentType());
        file.setSize(multipartFile.getSize());

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
        AbstractFile file = this.searchTaxDocumentService.getFileByGumgaOIAndChNFeAndNF(getGumgaOiToHQL(), chNFe);
        return file;
    }

    private GumgaOi getGumgaOiToHQL() {
        return new GumgaOi(GumgaThreadScope.organizationCode.get()+"%");
    }

    private String getChNFe(String xml) {
        return SearchXMLUtil.getInfEventochNFe(xml);
    }
}

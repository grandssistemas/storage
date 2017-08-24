package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.*;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class CommonTaxDocumentEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTaxDocumentEventService.class);

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey) {
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
                errors.add("Tipo de documento invalido.");
                return new FileProcessed(file, errors);
        }

        String chNFe = getChNFe(xml);
        file.setDetailOne(chNFe);

        String infEventochDhRegEvento = SearchXMLUtil.getInfEventochDhRegEvento(xml);
        file.setDetailTwo(infEventochDhRegEvento);

        if(file instanceof LocalFile) {
            String movement = taxDocument.getDetailThree();
            Date date = ValidateNfXML.stringToDate(taxDocument.getDetailTwo());
            LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTCanceled(containerKey,
                    ld.getYear(),
                    ld.getMonth().toString(),
                    file.getFileType(),
                    movement);

            ((LocalFile)file).setRelativePath(path + '/' + file.getName());
            file.setHash(GenerateHash.generateLocalFile());
        } else {
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
            errors.add("Não é um evento de cancelamento.");
            return new FileProcessed(file, errors);
        }

        if(existsCancellationEventToChNFe(xml)) {
            errors.add("Já existe um evento de cancelamento com essa chave de acesso.");
            return new FileProcessed(file, errors);
        }

        AbstractFile taxDocument = getTaxDocument(xml);
        if(taxDocument == null) {
            errors.add("Não existe documento fiscal com essa chave de acesso.");
            return new FileProcessed(file, errors);
        }

        return new FileProcessed(file, errors);
    }

    private Boolean isCancellationEvent(String xml) {
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

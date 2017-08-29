package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.AbstractFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
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

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey) {
        List<String> errors = new ArrayList<>();
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        String xml = XMLUtil.getXml(multipartFile);

        FileProcessed fileProcessed = validateLetterCorrectionEvent(file, xml);
        if(fileProcessed.getErrors().size() > 0) {
            return fileProcessed;
        }

        String infInutCNPJ = SearchXMLUtil.getInfEventoCNPJ(xml);
        String chNFe = SearchXMLUtil.getInfEventoChNFe(xml);

        if(infInutCNPJ.isEmpty()) {
            if(!chNFe.isEmpty() && chNFe.length() > 20) {
                infInutCNPJ = chNFe.substring(6, 20);
            }
        }

        if(!containerKey.equals(infInutCNPJ)) {
            errors.add("O CNPJ do evento da carta de correção é diferente da chave do container.");
            return new FileProcessed(file, errors);
        }

        String infInutDhRecbto = SearchXMLUtil.getInfEventoDhEvento(xml);
        if(infInutDhRecbto.isEmpty()) {
            errors.add("O evento de inutização precisa ter data e hora do recebimento.");
            return new FileProcessed(file, errors);
        }



        AbstractFile fileFromDB = this.searchTaxDocumentService.getFileByGumgaOIAndNProtAndNFLetterCorrection(getGumgaOiToHQL(), chNFe);
        if(fileFromDB != null) {
            errors.add("Esse evento de carta de correção já existe.");
            return new FileProcessed(file, errors);
        }

        AbstractFile taxDocument = this.searchTaxDocumentService.getFileByGumgaOIAndChNFeAndNF(getGumgaOiToHQL(), chNFe);
        if(taxDocument == null) {
            errors.add("Não existe documento fiscal com essa chave de acesso.");
            return new FileProcessed(file, errors);
        }



        file.setDetailOne(chNFe);

        file.setFileType(FileType.NFE_LETTER_CORRECTION);

        file.setDetailTwo(infInutDhRecbto);

        if(file instanceof LocalFile) {
            Date date = validateNfXML.stringToDate(file.getDetailTwo());
            LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTLetterCorrection(containerKey,
                    ld.getYear(),
                    ld.getMonth().toString(),
                    FileType.NFE);

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

    private FileProcessed validateLetterCorrectionEvent(AbstractFile file, String xml) {
        List<String> errors = new ArrayList<>();

        if(!isLetterCorrectionEvent(xml)) {
            errors.add("Não é um evento de carta de correção.");
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

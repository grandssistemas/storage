package digital.container.service.taxdocument;

import digital.container.storage.domain.model.file.*;
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
public class CommonTaxDocumentEventDisableService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTaxDocumentEventDisableService.class);

    @Autowired
    private SearchTaxDocumentService searchTaxDocumentService;

    @Autowired
    private ValidateNfXML validateNfXML;

    public FileProcessed getData(AbstractFile file, MultipartFile multipartFile, String containerKey) {
        List<String> errors = new ArrayList<>();
        file.setName(multipartFile.getOriginalFilename());
        file.setFileStatus(FileStatus.NOT_SYNC);

        String xml = XMLUtil.getXml(multipartFile);

        FileProcessed fileProcessed = validateDisableEvent(file, xml);
        if(fileProcessed.getErrors().size() > 0) {
            return fileProcessed;
        }

        String infInutCNPJ = SearchXMLUtil.getInfInutCNPJ(xml);
        if(!containerKey.equals(infInutCNPJ)) {
            errors.add("O CNPJ do evento de inutização é diferente da chave do container.");
            return new FileProcessed(file, errors);
        }

        String infInutDhRecbto = SearchXMLUtil.getInfInutDhRecbto(xml);
        if(infInutDhRecbto.isEmpty()) {
            errors.add("O evento de inutização precisa ter data e hora do recebimento.");
            return new FileProcessed(file, errors);
        }

        String infInutNProt = SearchXMLUtil.getInfInutNProt(xml);
        if(infInutDhRecbto.isEmpty()) {
            errors.add("O evento de inutização precisa ter numero do protocolo.");
            return new FileProcessed(file, errors);
        }

        AbstractFile fileFromDB = this.searchTaxDocumentService.getFileByGumgaOIAndNProtAndNFDisable(getGumgaOiToHQL(), infInutNProt);
        if(fileFromDB != null) {
            errors.add("Esse evento de inutização já existe.");
            return new FileProcessed(file, errors);
        }
        file.setDetailOne(infInutNProt);


        String mod = SearchXMLUtil.getInfInutMod(xml);
        switch (mod) {
            case "55":
                file.setFileType(FileType.NFE_DISABLE);
                break;
            case "65":
                file.setFileType(FileType.NFCE_DISABLE);
                break;
            default:
                errors.add("Tipo de documento invalido.");
                return new FileProcessed(file, errors);
        }

        file.setDetailTwo(infInutDhRecbto);

        Date date = validateNfXML.stringToDate(file.getDetailTwo());
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTDisable(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                mod.equals("55") ? FileType.NFE : FileType.NFCE);

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

    private FileProcessed validateDisableEvent(AbstractFile file, String xml) {
        List<String> errors = new ArrayList<>();
        String xServ = SearchXMLUtil.getInfInutXServ(xml);
        String xMotivo = SearchXMLUtil.getInfInutXMotivo(xml);

        if((!xMotivo.isEmpty() && !xMotivo.toLowerCase().contains("inutilizacao")) || (!xServ.isEmpty() && !"INUTILIZAR".equalsIgnoreCase(xServ))) {
            errors.add("Não é um evento de inutilização.");
        }

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

    private GumgaOi getGumgaOiToHQL() {
        return new GumgaOi(GumgaThreadScope.organizationCode.get()+"%");
    }


}

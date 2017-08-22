package digital.container.service.localfile;

import digital.container.repository.DatabaseFileRepository;
import digital.container.repository.LocalFileRepository;
import digital.container.storage.domain.model.file.DatabaseFile;
import digital.container.storage.domain.model.file.FileStatus;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.LocalFile;
import digital.container.storage.domain.model.file.vo.FileProcessed;
import digital.container.util.*;
import io.gumga.application.GumgaService;
import io.gumga.core.GumgaThreadScope;
import io.gumga.domain.domains.GumgaOi;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class LocalFileTaxDocumentoCanceledService extends GumgaService<LocalFile, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileTaxDocumentoCanceledService.class);
    private LocalFileRepository localFileRepository;
    @Autowired
    private DatabaseFileRepository databaseFileRepository;

    @Autowired
    public LocalFileTaxDocumentoCanceledService(GumgaCrudRepository<LocalFile, Long> repository) {
        super(repository);
        this.localFileRepository = LocalFileRepository.class.cast(repository);
    }

    public FileProcessed upload(String containerKey, MultipartFile multipartFile) {
        LocalFile localFile = new LocalFile();
        localFile.setName(multipartFile.getOriginalFilename());

        localFile.setFileStatus(FileStatus.NOT_SYNC);

        String oi = GumgaThreadScope.organizationCode.get() + "%";

        String movement = null;
        LocalDate ld = null;

        try(InputStream inputStream = multipartFile.getInputStream()) {
            List<String> errors = new ArrayList<>();
            String xml = IOUtils.toString(inputStream, "UTF8");


            String infEventoXevento = SearchXMLUtil.getInfEventoXevento(xml);

            if(!"Cancelamento".equalsIgnoreCase(infEventoXevento)) {
                errors.add("Não é um evento de cancelamento");
                return new FileProcessed(localFile, errors);
            }

            String infEventochNFe = SearchXMLUtil.getInfEventochNFe(xml);
            Optional<LocalFile> lfDocument = this.localFileRepository.getByChNFe(new GumgaOi(oi), infEventochNFe);
            Optional<DatabaseFile> dbDocument = this.databaseFileRepository.getByChNFe(new GumgaOi(oi), infEventochNFe);

            FileType fileType = FileType.INVALID_TAX_DOCUMENT;

            if(lfDocument.isPresent()) {
                LocalFile lf = lfDocument.get();
                fileType = lf.getFileType();
                movement = lf.getDetailThree();
                Date date = ValidateNfXML.stringToDate(lf.getDetailTwo());
                ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            } else {

                if(dbDocument.isPresent()) {
                    DatabaseFile db = dbDocument.get();
                    fileType = db.getFileType();
                    movement = db.getDetailThree();
                    Date date = ValidateNfXML.stringToDate(db.getDetailTwo());
                    ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else {
                    errors.add("Não existe documento fiscal com essa chave de acesso:"+infEventochNFe);
                    return new FileProcessed(localFile, errors);
                }
            }

            switch (fileType) {
                case NFE:
                        localFile.setFileType(FileType.NFE_CANCELED);
                    break;
                case NFCE:
                        localFile.setFileType(FileType.NFCE_CANCELED);
                    break;
                default:
                        errors.add("Tipo de documento invalido");
                    return new FileProcessed(localFile, errors);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTCanceled(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                localFile.getFileType(),
                movement);

        File folder = new File(LocalFileUtil.DIRECTORY_PATH + '/' + path);

        folder.mkdirs();


        localFile.setRelativePath(path + '/' + localFile.getName());

        localFile.setContainerKey(containerKey);
        localFile.setCreateDate(Calendar.getInstance());
        localFile.setHash(GenerateHash.generateLocalFile());

        localFile.setContentType(multipartFile.getContentType());
        localFile.setSize(multipartFile.getSize());

        try {
            SaveLocalFile.saveFile(folder, localFile.getName(), multipartFile.getInputStream());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return new FileProcessed(this.localFileRepository.saveAndFlush(localFile), Collections.EMPTY_LIST);
    }
}

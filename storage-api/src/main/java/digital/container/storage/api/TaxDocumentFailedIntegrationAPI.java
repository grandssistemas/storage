package digital.container.storage.api;

import digital.container.repository.file.DatabaseFilePartRepository;
import digital.container.repository.file.DatabaseFileRepository;
import digital.container.storage.domain.model.file.FileType;
import digital.container.storage.domain.model.file.database.DatabaseFile;
import digital.container.storage.domain.model.file.database.DatabaseFilePart;
import io.gumga.core.GumgaThreadScope;
import io.gumga.core.QueryObject;
import io.gumga.core.SearchResult;
import io.gumga.core.gquery.ComparisonOperator;
import io.gumga.core.gquery.Criteria;
import io.gumga.core.gquery.GQuery;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RequestMapping
@RestController("/tax-document/failed-integration")
public class TaxDocumentFailedIntegrationAPI {

    @Autowired
    private DatabaseFileRepository databaseFileRepository;


    @RequestMapping("/public/tax-document-contingency")
    @Transactional
    public String sycn(){
        System.out.println(Thread.currentThread().getId());
        asdas();
//        GumgaThreadScope.ignoreCheckOwnership.set(false);
//        QueryObject qo = new QueryObject();
//        qo.setPageSize(1000);
//        qo.setgQuery(new GQuery(new Criteria("obj.fileType",  ComparisonOperator.EQUAL, FileType.TAX_DOCUMENT_IN_CONTINGENCY)));
//        SearchResult<DatabaseFile> search = this.databaseFileRepository.search(qo);
//        GumgaThreadScope.ignoreCheckOwnership.set(true);
//        for (DatabaseFile databaseFile : search.getValues()) {
//            GumgaThreadScope.organizationCode.set(databaseFile.getOi().getValue());
//            DatabaseFile one = databaseFileRepository.findOne(databaseFile.getId());
//            Hibernate.initialize(one.getParts());
//            StringBuilder sb = new StringBuilder();
//
//            for (DatabaseFilePart databaseFilePart : one.getParts()) {
//                sb.append(databaseFilePart.getRawBytes());
//            }
//            System.out.println(sb.toString());
//        }
//
//        return  "SINCRONIZADO";
        return "foi";
    }



    @Async
    @Transactional
    public void asdas() {
        try {
            System.out.println(Thread.currentThread().getId());
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("testando");
        DatabaseFile databaseFile = new DatabaseFile();
        this.databaseFileRepository.saveAndFlush(databaseFile);
    }
}

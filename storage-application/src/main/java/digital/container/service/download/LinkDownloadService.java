package digital.container.service.download;

import digital.container.repository.download.LinkDownloadRepository;
import digital.container.storage.domain.model.download.LinkDownload;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LinkDownloadService extends GumgaService<LinkDownload, String> {

    private LinkDownloadRepository linkDownloadRepository;

    @Autowired
    public LinkDownloadService(GumgaCrudRepository<LinkDownload, String> repository) {
        super(repository);
        this.linkDownloadRepository = LinkDownloadRepository.class.cast(repository);
    }

    public LinkDownload getLinkDownloadByHash(String hash) {
        return this.linkDownloadRepository.getLinkDownloadByHash(hash);
    }
}

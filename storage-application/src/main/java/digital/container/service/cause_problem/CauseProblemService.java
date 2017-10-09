package digital.container.service.cause_problem;

import digital.container.storage.domain.model.exception.CauseProblem;
import digital.container.storage.domain.model.exception.SituationCauseProblem;
import io.gumga.application.GumgaService;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CauseProblemService extends GumgaService<CauseProblem, Long> {

    @Autowired
    public CauseProblemService(GumgaCrudRepository<CauseProblem, Long> repository) {
        super(repository);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(String description, SituationCauseProblem situationCauseProblem) {
        CauseProblem causeProblem = new CauseProblem();
        causeProblem.setReason(description);
        causeProblem.setSituation(situationCauseProblem);
        this.save(causeProblem);
    }
}

package digital.container.repository.exception;

import digital.container.storage.domain.model.exception.CauseProblem;
import io.gumga.domain.repository.GumgaCrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CauseProblemRepository extends GumgaCrudRepository<CauseProblem, String> {
}

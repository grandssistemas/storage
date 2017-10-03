package digital.container.repository;

import digital.container.storage.domain.model.file.CauseProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CauseProblemRepository extends JpaRepository<CauseProblem, Long> {
}

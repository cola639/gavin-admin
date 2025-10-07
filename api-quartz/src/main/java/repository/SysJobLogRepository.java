package repository;

import domain.SysJobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysJobLogRepository extends JpaRepository<SysJobLog, Long> {}

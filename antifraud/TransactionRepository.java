package antifraud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByNumber(String number);
    List<Transaction> findByNumberAndDateBetween(String number, LocalDateTime start, LocalDateTime end);
}

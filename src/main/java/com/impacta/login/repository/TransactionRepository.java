package com.impacta.login.repository;
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.impacta.login.model.TransactionDao;

public interface TransactionRepository extends JpaRepository<TransactionDao, Long> {
    
}
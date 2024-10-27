package org.apiether.apietherscan.repository;

import org.apiether.apietherscan.model.Address;
import org.apiether.apietherscan.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Optional<Transaction> findByTransactionHash(String transactionHash); // Metodo per trovare una transazione per hash
    // Trova tutte le transazioni associate ad un address, ordinate per il timestamp. Dal Meno recente al più recente
    List<Transaction> findByAddressOrderByTimeStampAsc(Address address);
}

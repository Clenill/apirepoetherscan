package org.apiether.apietherscan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.apiether.apietherscan.model.Address;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByAddress(String address); // trova l'indirizzo per valore
}

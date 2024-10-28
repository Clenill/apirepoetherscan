package org.apiether.apietherscan.service;
import org.apiether.apietherscan.model.Address;
import org.apiether.apietherscan.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    public AddressServiceImpl(final AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address salvaNuovoAddress(String address, String balance) {
        //Cerca se l'indirizzo è presente nel DB
        // Si potrebbe implementare una logica per verificare la lunghezza e schema base dell'address

        Optional<Address> existingAddress = addressRepository.findByAddress(address);
        Address addressEntity;
        //Se la ricerca ha esito negativo assegno ad addressEntity i campi del nuovo address
        if (existingAddress.isEmpty()) {
            addressEntity = new Address();
            addressEntity.setAddress(address);
            addressEntity.setBalance(balance);
            //Mancano alcuni campi data
            addressRepository.save(addressEntity);
        }else{
            //Se invece l'address è presente faccio una get e ritorno l'entità
            addressEntity = existingAddress.get();
        }
        return addressEntity;
    }

    @Override
    public void verificatimeStampAddress(Address addressEntity, String timeStampString) {
        long timeStamp = Long.parseLong(timeStampString);
        LocalDateTime transactionDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(timeStamp), ZoneOffset.UTC);
        boolean modifica = false;
        //Controllo se le date di createAt e lastUpdateAt sono vuote

            if(addressEntity.getCreatedAt() == null || transactionDate.isBefore(addressEntity.getCreatedAt())) {
                // Aggiorno createdAt
                addressEntity.setCreatedAt(transactionDate);
                modifica = true;
            }

            if (addressEntity.getLastUpdateAt() == null || transactionDate.isAfter(addressEntity.getLastUpdateAt())) {
                addressEntity.setLastUpdateAt(transactionDate);
                modifica = true;
            }
            if(modifica){
                addressRepository.save(addressEntity);
            }



    }
}

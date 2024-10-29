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

        Optional<Address> existingAddress = addressRepository.findByAddress(address);
        Address addressEntity;
        //Se la ricerca ha esito negativo assegno ad addressEntity i campi del nuovo address
        if (existingAddress.isEmpty()) {
            addressEntity = new Address();
            addressEntity.setAddress(address);
            addressEntity.setBalance(balance);
            //I campi data sono assegnati successivamente
            addressRepository.save(addressEntity);
        }else{
            //Se invece l'address è presente faccio una get e ritorno l'entità
            addressEntity = existingAddress.get();
            //Comparo le stringhe se sono  diverse assegno il nuovo balance e lo salvo
            if(!addressEntity.getBalance().equals(balance)){
                addressEntity.setBalance(balance);
                addressRepository.save(addressEntity);
            }
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
    @Override
    public boolean controllaUltimotimeStamp(String ultimotimeStamp, Address addressEntity){
        //Conversione in LocalDateTime
        long timeStamp = Long.parseLong(ultimotimeStamp);
        LocalDateTime transactionDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(timeStamp), ZoneOffset.UTC);
        // se le date sono uguali restituisco true e non c'è bisogno di entrare nel for
        if(addressEntity.getLastUpdateAt() != null && transactionDate.isEqual(addressEntity.getLastUpdateAt())){
            return true;
        }

        return false;
    }
}

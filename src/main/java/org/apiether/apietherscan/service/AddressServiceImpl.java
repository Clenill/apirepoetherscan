package org.apiether.apietherscan.service;
import org.apiether.apietherscan.model.Address;
import org.apiether.apietherscan.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    public AddressServiceImpl(final AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Address salvaNuovoAddress(String address) {
        //Cerca se l'indirizzo è presente nel DB
        // Si potrebbe implementare una logica per verificare la lunghezza e schema base dell'address

        Optional<Address> existingAddress = addressRepository.findByAddress(address);
        Address addressEntity;
        //Se la ricerca ha esito negativo assegno ad addressEntity i campi del nuovo address
        if (existingAddress.isEmpty()) {
            addressEntity = new Address();
            addressEntity.setAddress(address);
            //Mancano alcuni campi data
            addressRepository.save(addressEntity);
        }else{
            //Se invece l'address è presente faccio una get e ritorno
            addressEntity = existingAddress.get();
        }



        return addressEntity;
    }
}
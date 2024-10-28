package org.apiether.apietherscan.service;
import com.fasterxml.jackson.databind.JsonNode;
import org.apiether.apietherscan.model.Address;
import org.apiether.apietherscan.model.Transaction;
import org.apiether.apietherscan.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.apiether.apietherscan.service.AddressServiceImpl;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AddressServiceImpl addressService;
    public TransactionServiceImpl(final TransactionRepository transactionRepository, AddressServiceImpl addressService) {
        this.transactionRepository = transactionRepository;
        this.addressService = addressService;
    }

    //IMPLEMENTAZIONE LOGICA DI SALVATAGGIO NUOVA TRANSAZIONE
    public Boolean salvaTransazione(String trxHash, JsonNode transactionNode, Address addressEntity ){
        // find by transactionHash per verificare se la transazione c'è nel DB
        Optional<Transaction> existingTransaction = transactionRepository.findByTransactionHash(trxHash);

        if(existingTransaction.isEmpty()){
            //Se non c'è alcuna transazione la salva
            //Bisogna gestire il nullpointException
            Transaction transaction = new Transaction();
            transaction.setTransactionHash(trxHash);
            transaction.setBlockNumber(transactionNode.get("blockNumber").asText());
            //salvo il campo timeStamp in timestampString
            String timeStampString = transactionNode.get("timeStamp").asText();
            transaction.setTimeStamp(timeStampString);
            transaction.setFrom(transactionNode.get("from").asText());
            transaction.setTo(transactionNode.get("to").asText());
            transaction.setValue(transactionNode.get("value").asText());
            transaction.setGasUsed(transactionNode.get("gasUsed").asText());

            //Collegamento della transazione all'indirizzo
            transaction.setAddress(addressEntity);

            //Verifica del campo timeStampString con le date dell'Address


            transactionRepository.save(transaction);
            //Vado a verificare il timestamp della transazione
            addressService.verificatimeStampAddress(addressEntity, timeStampString);
            return true;
        }

        return false;

    }
}

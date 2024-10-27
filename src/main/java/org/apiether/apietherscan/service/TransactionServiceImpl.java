package org.apiether.apietherscan.service;
import com.fasterxml.jackson.databind.JsonNode;
import org.apiether.apietherscan.model.Address;
import org.apiether.apietherscan.model.Transaction;
import org.apiether.apietherscan.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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
            transaction.setTimeStamp(transactionNode.get("timeStamp").asText());
            transaction.setFrom(transactionNode.get("from").asText());
            transaction.setTo(transactionNode.get("to").asText());
            transaction.setValue(transactionNode.get("value").asText());
            transaction.setGasUsed(transactionNode.get("gasUsed").asText());

            //Collegamento della transazione all'indirizzo
            transaction.setAddress(addressEntity);
            transactionRepository.save(transaction);
            return true;
        }

        return false;

    }
}

package org.apiether.apietherscan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apiether.apietherscan.model.Transaction;
import org.apiether.apietherscan.repository.AddressRepository;
import org.apiether.apietherscan.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apiether.apietherscan.service.EtherscanService;
import org.apiether.apietherscan.model.Address;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
public class EatherscanController {
    @Autowired
    private EtherscanService etherscanService;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/transactions/{address}")
    public ResponseEntity<JsonNode> getTransactions(@PathVariable("address") String address) {
        try{
            JsonNode result = etherscanService.getTransactionsByAddress(address);
            if(result!= null){
                return ResponseEntity.ok(result);
            }else{
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode resultnull = mapper.createObjectNode();
                resultnull.put("address", address);
                resultnull.put("message", "Risposta null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resultnull);
            }

        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);

        }
    }

    @GetMapping("/indirizzo/transactions")
    public ResponseEntity<JsonNode> getTransactionForAddress(@RequestParam("address") String address) {
        //Trova l'indirizzo nel DB
        Optional<Address> addressEntity = addressRepository.findByAddress(address);
        if(addressEntity.isEmpty()){
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode result = mapper.createObjectNode();
            result.put("address", address);
            result.put("message", "Indirizzo non presente nel DB.");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        Address addressCercato = addressEntity.get();
        ObjectNode jsonCompilato = etherscanService.rispostaJsonAPI(addressCercato);

        return ResponseEntity.ok(jsonCompilato);
    }

}

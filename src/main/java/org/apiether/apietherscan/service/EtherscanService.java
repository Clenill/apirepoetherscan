package org.apiether.apietherscan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apiether.apietherscan.model.Address;
import org.apiether.apietherscan.model.Transaction;
import org.apiether.apietherscan.repository.AddressRepository;
import org.apiether.apietherscan.service.TransactionServiceImpl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class EtherscanService {


    private static final String ETHERSCAN_API_URL = "https://api.etherscan.io/api";
    private static final String API_KEY = "P4B6RR7M55Q1GFF9HR11GV9HDMHZNXC7SD"; //Da non pubblicare su git
    private final RestTemplate restTemplate;
    private final AddressRepository addressRepository;
    private final TransactionServiceImpl transactionService;
    private final AddressServiceImpl addressService;

    //Costruttore
    public EtherscanService(RestTemplate restTemplate, AddressRepository addressRepository, TransactionServiceImpl transactionService, AddressServiceImpl addressService) {
        this.restTemplate = restTemplate;
        this.addressRepository = addressRepository;
        this.transactionService = transactionService;
        this.addressService = addressService;
    }
    public JsonNode getTransactionsByAddress(String address) throws IOException {
        // Stampo l'indirizzo a terminale per controllo, costuzione dei parametri richiesta e stampa per controllo
        System.out.println("Address inserito:"+ address);
        String url = String.format("%s?module=account&action=txlist&address=%s&startblock=0&endblock=99999999&sort=asc&apikey=%s",
                ETHERSCAN_API_URL, address, API_KEY);


        System.out.println("Riesta URL"+url);
        // Chiamata con RestTemplate
        String response = restTemplate.getForObject(url, String.class);

        // Conversione della risposta JSON in JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response);

        //Chiamata per salvare o recuperare nel DB l'indirizzo cercato
        Address addressEntity = addressService.salvaNuovoAddress(address);

        // Bisogna verificare il JSON, solo se si hanno risultati positivi
        //passare il result altrimenti se esito negativo gestire la risposta negativa


        // Itera sulle transazioni e salva quelle non presenti
        JsonNode transactions = jsonResponse.get("result");


        if(transactions != null) {
            if(transactions.isArray()) {
                for (JsonNode transactionNode : transactions) {
                    String transactionHash = transactionNode.get("hash").asText();

                    if(transactionService.salvaTransazione(transactionHash, transactionNode, addressEntity)){
                        System.out.println("Transazione salvata correttamente");
                    }else{
                        System.out.println("Transazione non salvata perché presente nel DB.");
                    }
                }
            } else if (transactions.isObject()) {
                String transactionHash = transactions.get("hash").asText();

                if(transactionService.salvaTransazione(transactionHash, transactions, addressEntity)){
                    System.out.println("Transazione salvata correttamente");
                }else{
                    System.out.println("Transazione non salvata perché presente nel DB.");
                }
            }
        }

        return jsonResponse;

    }

}

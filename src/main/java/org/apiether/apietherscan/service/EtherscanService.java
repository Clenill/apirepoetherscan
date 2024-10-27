package org.apiether.apietherscan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apiether.apietherscan.model.Address;

import java.io.IOException;

@Service
public class EtherscanService {


    private static final String ETHERSCAN_API_URL = "https://api.etherscan.io/api";
    private final RestTemplate restTemplate;
    private final TransactionServiceImpl transactionService;
    private final AddressServiceImpl addressService;

    @Value("${etherscan.api.key}")
    private String apiKey; // La chiave API viene iniettata

    //Costruttore
    public EtherscanService(RestTemplate restTemplate, TransactionServiceImpl transactionService, AddressServiceImpl addressService) {
        this.restTemplate = restTemplate;

        this.transactionService = transactionService;
        this.addressService = addressService;
    }
    public JsonNode getTransactionsByAddress(String address) throws IOException {
        // Stampo l'indirizzo a terminale per controllo, costuzione dei parametri richiesta e stampa per controllo
        System.out.println("Address inserito:"+ address);
        String url = String.format("%s?module=account&action=txlist&address=%s&startblock=0&endblock=99999999&sort=asc&apikey=%s",
                ETHERSCAN_API_URL, address, apiKey);


        System.out.println("Riesta URL"+url);
        // Chiamata con RestTemplate
        String response = restTemplate.getForObject(url, String.class);

        // Conversione della risposta JSON in JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response);

        //Chiamata per salvare o recuperare nel DB l'indirizzo cercato
        Address addressEntity = addressService.salvaNuovoAddress(address);

        // Si potrebbe aggiungere un controllo sul JSON, solo se si hanno risultati positivi procedere
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

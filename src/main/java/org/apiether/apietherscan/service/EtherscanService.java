package org.apiether.apietherscan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apiether.apietherscan.model.Address;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public String getEtherBalanceForAddress(String address) throws JsonProcessingException {

        String url = String.format("%s?module=account&action=balance&address=%s&tag=latest&apikey=%s",
                ETHERSCAN_API_URL, address, apiKey);

        System.out.println("Riesta URL: "+url);
        // Chiamata con RestTemplate
        String response = restTemplate.getForObject(url, String.class);
        //Conversione risposta JSON in JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response);

        if(jsonResponse.has("result") && !jsonResponse.get("result").asText().isEmpty()) {
            String weiValue = jsonResponse.get("result").asText();

            //Conversione in Ether con le 18 cifre decimali
            return convertWeiToEtherWith18Decimals(weiValue);
        }else{
            return "NO VALUE";
        }
    }

    // Metodo per convertire Wei in Ether mantenendo 18 cifre decimali
    private String convertWeiToEtherWith18Decimals(String weiValue) {
        // Wei per 1 Ether
        BigDecimal WEI_IN_ETHER = new BigDecimal("1000000000000000000");

        // Converti la stringa in BigDecimal
        BigDecimal wei = new BigDecimal(weiValue);
        if(wei.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        // Dividi per 10^18 senza arrotondare, mantenendo le 18 cifre decimali
        BigDecimal ether = wei.divide(WEI_IN_ETHER, 18, RoundingMode.DOWN);
        // Restituisci il valore formattato come stringa con 18 cifre decimali
        return ether.toPlainString();
    }

}

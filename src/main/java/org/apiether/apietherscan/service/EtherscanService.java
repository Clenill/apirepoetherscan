package org.apiether.apietherscan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apiether.apietherscan.model.Transaction;
import org.apiether.apietherscan.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.apiether.apietherscan.model.Address;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EtherscanService {

    private static final String ETHERSCAN_API_URL = "https://api.etherscan.io/api";
    private final RestTemplate restTemplate;
    private final TransactionServiceImpl transactionService;
    private final AddressServiceImpl addressService;

    private final TransactionRepository transactionRepository;
    // La chiave API viene letta da application.properties
    @Value("${etherscan.api.key}")
    private String apiKey;

    //Costruttore
    public EtherscanService(RestTemplate restTemplate,TransactionRepository transactionRepository, TransactionServiceImpl transactionService, AddressServiceImpl addressService) {
        this.restTemplate = restTemplate;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.addressService = addressService;
    }


    public JsonNode getTransactionsByAddress(String address) {
        // Stampo l'indirizzo a terminale per controllo, costuzione dei parametri richiesta e stampa per controllo
        System.out.println("Address inserito:" + address);
        // sort desc, la prima transazione restituita è la più recente
        String url = String.format("%s?module=account&action=txlist&address=%s&startblock=0&endblock=99999999&sort=desc&apikey=%s",
                ETHERSCAN_API_URL, address, apiKey);

        boolean transazione_trovata = false;

        System.out.println("Richiesta URL: " + url);
        // Chiamata con RestTemplate
        try {


        String response = restTemplate.getForObject(url, String.class);

        // Conversione della risposta JSON in JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonResponse = mapper.readTree(response);

        String message = jsonResponse.get("message").asText();
        String status = jsonResponse.get("status").asText();

        // Se il JSON ha una risposta positiva allora l'indirizzo è valido
        if ("OK".equalsIgnoreCase(message) || "1".equals(status)) {
            // prelevo il balance per l'address
            String balance = getEtherBalanceForAddress(address);
            //Chiamata per salvare o recuperare nel DB l'indirizzo cercato
            Address addressEntity = addressService.salvaNuovoAddress(address, balance);

            //Prelevo il campo result della risposta JSON
            JsonNode transactions = jsonResponse.get("result");

            if (transactions != null && transactions.isArray()) {
                String ultimotimeStamp = transactions.get(0).get("timeStamp").asText();


                // Se l'ultimo timeStamp del JSON è lo stesso del campo Address allora non si entra nel for


                if (!addressService.controllaUltimotimeStamp(ultimotimeStamp, addressEntity)) {
                    // Itera sulle transazioni e salva quelle non presenti solo se il primo timeStamp è diverso da lastUpdatedAt
                    for (JsonNode transactionNode : transactions) {
                        String transactionHash = transactionNode.get("hash").asText();

                        // verifico che la prima transazione di result sia diversa da lastupdateAt di Address Entity
                        //Nel caso sono uguali non c'è bisogno di aggiornamenti delle transazioni


                        if (transactionService.salvaTransazione(transactionHash, transactionNode, addressEntity)) {
                            System.out.println("Transazione salvata correttamente");
                            transazione_trovata = true;
                        } else {
                            System.out.println("Transazione non salvata perché presente nel DB.");
                            // si potrebbe uscire dal for quando trova una transazione presente perché l'ordinamento dell'API è desc
                            // di conseguenza vengono salvate in questo ordine nel DB, altrimenti si potrebbe fare un riordino delle
                            // transazioni e confrontare solo i timeStamp.
                            //break;
                        }
                    }
                }
            } else if (transactions != null && transactions.isObject()) {
                String ultimotimeStamp = transactions.get(0).get("timestamp").asText();
                if (!addressService.controllaUltimotimeStamp(ultimotimeStamp, addressEntity)) {
                    String transactionHash = transactions.get("hash").asText();

                    if (transactionService.salvaTransazione(transactionHash, transactions, addressEntity)) {
                        System.out.println("Transazione salvata correttamente");
                        transazione_trovata = true;
                    } else {
                        System.out.println("Transazione non salvata perché presente nel DB.");
                    }
                }
            }
        }
        // Costruzione JSON di risposta

        return composizioneRispostaJson(address, status, message, transazione_trovata);
    }catch (IOException e){
            System.err.println("Errore durante la lettura della risposta JSON: " + e.getMessage());
            return composizioneRispostaJson(address, "0", "Errore durante la comunicazione con il server.", false);
        }catch (NullPointerException e){
            System.err.println("Errore null pointer: " + e.getMessage());
            return composizioneRispostaJson(address, "0", "Dati non trovati.", false);
        }catch(Exception e){
            System.err.println("Errore imprevisto: " + e.getMessage());
            return composizioneRispostaJson(address, "0", "Si è verificato un errore imprevisto.", false);
        }

    }

    public String getEtherBalanceForAddress(String address){

        String url = String.format("%s?module=account&action=balance&address=%s&tag=latest&apikey=%s",
                ETHERSCAN_API_URL, address, apiKey);

        System.out.println("Richiesta URL Balance: "+url);
        // Chiamata con RestTemplate

        try {


            String response = restTemplate.getForObject(url, String.class);
            //Conversione risposta JSON in JsonNode
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response);

            if (jsonResponse.has("result") && !jsonResponse.get("result").asText().isEmpty()) {
                String weiValue = jsonResponse.get("result").asText();

                //Conversione in Ether con le 18 cifre decimali
                return convertWeiToEtherWith18Decimals(weiValue);
            } else {
                return "NO VALUE";
            }
        }catch(IOException e){
            System.err.println("Errore durante la risposta JSON: " + e.getMessage());
            return "BAD JSON";
        }catch (NullPointerException e){
            System.err.println("Errore null pointer: " + e.getMessage());
            return "ERROR NULL";
        }catch (Exception e){
            System.err.println("Errore imprevisto: " + e.getMessage());
            return "ERROR";
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

    public ObjectNode composizioneRispostaJson(String address, String status, String message, boolean transactionFound){
        // Risposta per l'API di salvataggio transazioni e address

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonResponse = mapper.createObjectNode();
        jsonResponse.put("address", address);
        jsonResponse.put("status", status);
        jsonResponse.put("message", message);
        jsonResponse.put("New transactions Found", transactionFound);

        return jsonResponse;
    }

    public ObjectNode rispostaJsonAPI(Address addressCercato){
        //Recupera la lista delle transazioni associate ad un Address
        List<Transaction> transactions = transactionRepository.findByAddressOrderByTimeStampAsc(addressCercato);

        // Creazione di un oggetto JSON per la risposta
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode responseJson = objectMapper.createObjectNode();
        responseJson.put("address", addressCercato.getAddress());
        responseJson.put("message", "OK");
        responseJson.put("balance ETH", addressCercato.getBalance());
        ArrayNode transactionsArray = objectMapper.createArrayNode();

        if(transactions != null){
            // For per aggiungere di ogni transazione all'array
            //Aggiungere controllo su transactin != null
            for (Transaction transaction : transactions) {
                ObjectNode transactionJson = objectMapper.createObjectNode();
                transactionJson.put("hash", transaction.getTransactionHash());
                transactionJson.put("blockNumber", transaction.getBlockNumber());
                transactionJson.put("timeStamp", transaction.getTimeStamp());
                transactionJson.put("from", transaction.getFrom());
                transactionJson.put("to", transaction.getTo());
                transactionJson.put("value ETH", convertWeiToEtherWith18Decimals(transaction.getValue()));// Valore in Ethereum invece che in Wei
                transactionJson.put("gasUsed", transaction.getGasUsed());
                transactionsArray.add(transactionJson);
            }

            responseJson.set("transactions", transactionsArray);
        }


        return responseJson;
    }

}

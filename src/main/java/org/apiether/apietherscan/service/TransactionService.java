package org.apiether.apietherscan.service;
import com.fasterxml.jackson.databind.JsonNode;
import org.apiether.apietherscan.model.Address;

public interface TransactionService {
    Boolean salvaTransazione(String trxHash, JsonNode trxNode, Address addressEntity);
}

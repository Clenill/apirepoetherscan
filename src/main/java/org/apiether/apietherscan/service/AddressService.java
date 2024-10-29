package org.apiether.apietherscan.service;
import org.apiether.apietherscan.model.Address;

public interface AddressService {
    Address salvaNuovoAddress(String address, String balance);
    void verificatimeStampAddress(Address addressEntity, String timeStampString);
    boolean controllaUltimotimeStamp(String ultimotimeStamp, Address addressEntity);
}

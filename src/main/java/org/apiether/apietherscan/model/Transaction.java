package org.apiether.apietherscan.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @Column(name = "transaction_hash")
    @JsonProperty("hash")
    private String transactionHash;//Campo JSON hash

    @Column(name = "block_number")
    @JsonProperty("blockNumber")
    private String blockNumber;

    @Column(name = "time_stamp")
    @JsonProperty("timeStamp")
    private String timeStamp;

    @Column(name = "from_address")
    @JsonProperty("from")
    private String _from; // underscore per conflitto nome Hibernate

    @Column(name = "to_address")
    @JsonProperty("to")
    private String _to; // underscore per conflitto nome Hibernate

    @Column(name = "value")
    @JsonProperty("value")
    private String value;

    @Column(name = "gas_used")
    @JsonProperty("gasUsed")
    private String gasUsed;

    @ManyToOne
    @JoinColumn(name = "address_id")
    @JsonBackReference
    private Address address;

    public Transaction() {this.transactionHash = "";  }
    public Transaction(String transactionHash, String blockNumber, String timeStamp, String from, String to, String value, String gasUsed, Address address) {
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
        this.timeStamp = timeStamp;
        this._from = from;
        this._to = to;
        this.value = value;
        this.gasUsed = gasUsed;
        this.address = address;
    }
    //Getter

    public String getTransactionHash() {return this.transactionHash;}
    public String getBlockNumber() {return this.blockNumber;}
    public String getTimeStamp() {return this.timeStamp;}
    public String getFrom() {return this._from;}
    public String getTo() {return this._to;}
    public String getValue() {return this.value;}
    public String getGasUsed() {return this.gasUsed;}
    //Setter

    public void setTransactionHash(String transactionHash) {this.transactionHash = transactionHash;}
    public void setBlockNumber(String blockNumber) {this.blockNumber = blockNumber;}
    public void setTimeStamp(String timeStamp) {this.timeStamp = timeStamp;}
    public void setFrom(String from) {this._from = from;}
    public void setTo(String to) {this._to = to;}
    public void setValue(String value) {this.value = value;}
    public void setGasUsed(String gasUsed) {this.gasUsed = gasUsed;}
    // Get e Set Address
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

package org.apiether.apietherscan.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "address")
public class Address {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "address", unique = true)
    private String address;

    @Column(name = "balance")
    private String balance;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate createdAt;

    @Column(name = "last_update_at")
    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate lastUpdateAt;

    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Transaction> transactions;
    // Lazy pigro le transazioni sono caricate solo quando richeste, Cascadetype all propaga le operazioni di persistenza
    public Address() {
        this.address = "";// Non voglio stringhe null
    }

    public Address(Long id, String address,String balance, LocalDate createdAt, LocalDate lastUpdateAt) {
        this.id = id;
        this.address = address;
        this.balance = balance;
        this.createdAt = createdAt;
        this.lastUpdateAt = lastUpdateAt;
    }

    //Getter

    public Long getId() {return this.id;}
    public String getAddress() {return this.address = address;}
    public String getBalance() {return this.balance = address;}
    public LocalDate getCreatedAt() {return this.createdAt;}
    public LocalDate getLastUpdateAt() {return this.lastUpdateAt;}
    //Setter

    public void setId(Long id) {this.id = id;}
    public void setAddress(String address) {this.address = address;}
    public void setBalance(String balance) {this.balance = balance;}
    public void setCreatedAt(LocalDate createdAt) {this.createdAt = createdAt;}
    public void setLastUpdateAt(LocalDate lastUpdateAt) {this.lastUpdateAt = lastUpdateAt;}

    //Relazione con Transactin
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

}

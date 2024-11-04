<h1>Task Okipo</h1>
Implementazione di un programma che si avvale delle API fornite da etherscan 
per salvare e verificare le transizioni di indirizzi Ethereum.

Caratteristiche tecniche:
* Java SDK 17
* Spring Boot 3.x
* Database PostgreSQL
* Utilizzo di Maven


Configurazione base:

Assicurarsi di aver ricevuto il file application.properties e inserirlo correttamente all'interno della root path del programma.<br>
Se è già installata una versione di PostgreSQL controllare che la porta coincida con la configurazione di application.properties. <br>
Creare il database a cui connettersi rispettando la configurazione ricevuta ed assicurarsi che il nome dello schema sia rispettato con quello presente nella configurazione.
<br>
Al primo avvio per avere piena compatibilità con la struttura del Database assicurarsi di avere attiva l'opzione: spring.jpa.hibernate.ddl-auto=create.
<br>Dopo il primo avvio questa opzione può essere disabilitata o messa su validate in modo da mantenere il database popolato.

Caratteristiche principali:

Il programma è composto da due tabelle: Address e Transaction, correlate al relativo Database Postgre.
Ogni Transaction è associata ad un Address, è possibile salvare e visualizzare le transazioni associate ad un Address tramite due diverse chiamate:
1. "http://localhost:porta/transactions/address", effettuare questa chiamata per popolare le Transaction per un Address. Assicurarsi di sostituire "porta" che in questo caso coincide con quella di default per un'applicazione spring e "address" che corrisponde all'indirizzo Ethereum che si vuole analizzare. 
<br>La risposta è composta da un JSON che contiene, oltre alle altre, due voci per interpretare l'esito della chiamata status pari a 1 e message OK in caso di esito positivo del recupero delle transazioni associate, in questo caso si sfrutta l'API di Etherscan per analizzare le transazioni dell'indirizzo.
2. "http://localhost:porta/indirizzo/transactions?address=addressdaverificare", con questa chiamata si visualizzeranno le Transaction e Balance per un Address specifico. Anche in questo caso sostituire "porta" con quella stabilita e "addressdaverificare" con l'indirizzo desiderato. In questo caso la risposta è sempre un JSON che mostrerà l'address inserito il balance associato ad esso e le transazioni associate all'address ordinate cronologicamente dalla meno recente alla più recente. <br>

Le API da Etherscan utilizzate sono le seguenti:
<br>
* Chiamata per prelevare il balance di un Address:
<br>
    https://api.etherscan.io/api?module=account
    &action=balance
    &address=address
    &tag=latest
    &apikey=YourApiKeyToken
<br>
* Chiamata per prelevare le Transaction associate ad un Address:
<br>
  https://api.etherscan.io/api
  ?module=account
  &action=txlist
  &address=address
  &startblock=0
  &endblock=99999999
  &page=1
  &offset=10
  &sort=desc
  &apikey=YourApiKeyToken





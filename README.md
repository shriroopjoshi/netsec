# MessagingApp
A secure messaging application as a part of Network Security project. It uses Otway-Rees protocol on KDC server, and uses the key generated for communication.
Each key is valid only for a session.

### Dependencies
It uses <a href='https://github.com/google/gson'>Gson</a> for parsing messages to JSON and vice versa.
No other external dependency is used.

### How to build
It is a NetBeans IDE project. Import this project into NetBeans IDE to  build.

### How to run
To start the server:
<code>
java -jar dist/MessagingApp.jar server
</code>

To start the client:
<code>
java -jar dist/MessagingApp.jar client
</code>

To see the encrypted messages:
<code>
java -jar dist/MessagingApp.jar [client | server] verbose
</code>
All keys and configurations are stored in 'resources' directory

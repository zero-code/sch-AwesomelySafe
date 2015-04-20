package authentication;


import AwesomeSockets.AwesomeServerSocket;
import encryption.EncryptDecryptHelper;
import encryption.FilePaths;
import encryption.SecurityFileReader;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by JiaHao on 19/4/15.
 */
public class APServer {

    private final AwesomeServerSocket serverSocket;
    private final Cipher encryptCipher;




    public APServer() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        this.serverSocket = new AwesomeServerSocket(AuthenticationConstants.PORT);


        this.encryptCipher = EncryptDecryptHelper.getEncryptCipher(FilePaths.SERVER_PRIVATE_KEY);


    }


    public void start() throws IOException {
        authenticationProtocol();

    }

    public void authenticationProtocol() {
        try {
            acceptClient();
            waitForClientToSayHello();
            waitForClientToAskForCertificate();
            waitForClientToSendSymmetricKey();
            waitForClientToSendFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void acceptClient() throws IOException {

        this.serverSocket.acceptClient();


    }

    private void waitForClientToSayHello() throws IOException {
        System.out.println("Waiting for client to say hello...");
        // wait for client to say hello
        boolean clientSaidHello = false;

        while (!clientSaidHello) {

            String clientMessage = this.serverSocket.readMessageLineForClient(0);

            if (clientMessage.equals(AuthenticationConstants.CLIENT_HELLO_MESSAGE)) {
                clientSaidHello = true;
            }
        }


        // todo nonce
        // todo bye

        // send encrypted response
        byte[] encryptedReplyToHello = EncryptDecryptHelper.encryptString(AuthenticationConstants.SERVER_REPLY_TO_HELLO, this.encryptCipher);
        serverSocket.sendByteArrayForClient(0, encryptedReplyToHello);

    }



    private void waitForClientToAskForCertificate() throws IOException {
        System.out.println("Waiting for client to ask for certificate...");
        // wait for client to ask for certificate
        boolean clientAskedForCertificate = false;

        while (!clientAskedForCertificate) {

            String clientMessage = this.serverSocket.readMessageLineForClient(0);

            if (clientMessage.equals(AuthenticationConstants.CLIENT_ASK_FOR_CERT)) {
                clientAskedForCertificate = true;
            }
        }

        // send certificate
        byte[] serverCert = SecurityFileReader.readFileIntoByteArray(FilePaths.SERVER_CERTIFICATE);
        serverSocket.sendByteArrayForClient(0, serverCert);

    }


    private void waitForClientToSendSymmetricKey() throws IOException {
        System.out.println("Waiting for client to send symmetric key...");
        // wait for client to sent symmetric key
        byte[] receivedEncryptedSymmetricKey = this.serverSocket.readByteArrayForClient(0);

    }

    private void waitForClientToSendFile() {
        System.out.println("Waiting for client to send file...");
    }

    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        APServer server = new APServer();
        server.start();

    }

}
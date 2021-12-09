import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        String host = "localhost"; // or "networkslab-ivt.ftp.sh"
        int port = 5111;
        Socket socket = new Socket(host, port);
        Thread receiveThread = new ReceiveThread(socket);
        Thread sendThread = new SendThread(socket, args[0]);

        receiveThread.start();
        sendThread.start();
    }
}

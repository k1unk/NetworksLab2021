import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
      //  Socket socket = new Socket("networkslab-ivt.ftp.sh", 5111);
        Socket socket = new Socket("localhost", 5111);
        Thread receiveThread = new ReceiveThread(socket);
        Thread sendThread = new SendThread(socket, args[0]);

        receiveThread.start();
        sendThread.start();
    }
}

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(5111);
        System.out.println("server is running on port " + serverSocket.getLocalPort());
        ArrayList<Socket> sockets = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        HashMap<Socket, DataOutputStream> writers = new HashMap<>();
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
                writers.put(socket, new DataOutputStream(socket.getOutputStream()));
            } catch (Exception e) {
                break;
            }
            sockets.add(socket);

            Thread clientHandler = new ClientThread(socket, writers, sockets, serverSocket);
            clientHandler.start();

            threads.add(clientHandler);
        }
        for (int i = 0; i < sockets.size(); i++) {
            try {
                sockets.get(i).close();
            } catch (Exception ignored) {
            }
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
}
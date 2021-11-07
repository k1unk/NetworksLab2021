import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientThread extends Thread {

    Socket socket;
    HashMap<Socket, DataOutputStream> writers;
    ArrayList<Socket> sockets;
    ServerSocket serverSocket;

    public ClientThread(Socket socket,
                        HashMap<Socket, DataOutputStream> writers,
                        ArrayList<Socket> sockets,
                        ServerSocket serverSocket) {
        this.socket = socket;
        this.writers = writers;
        this.sockets = sockets;
        this.serverSocket = serverSocket;
    }

    HashMap<Socket, String> userNames = new HashMap<>();

    @Override
    public void run() {
        DataInputStream reader;
        try {
            reader = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (!socket.isClosed()) {
            int lengthReceived;
            try {
                lengthReceived = reader.readInt();
            } catch (IOException z) {
                long time = Instant.now().toEpochMilli();
                String serverDate = Long.toString(time);
                byte[] bytesToSend = MessageCreator.createMessage(userNames.get(socket),
                        'c', serverDate);
                closeSocket(userNames.get(socket));
                try {
                    writeToClients(bytesToSend, "");
                } catch (Exception ignored) {}
                break;
            }
            String text = "_";
            try {
                text = reader.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] bytesReceived = new byte[lengthReceived];
            try {
                reader.readFully(bytesReceived);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Parser p = Parser.getAll(bytesReceived);
            String userName = p.getUserName();
            char command = p.getCommand();
            String fileName = p.getFileName();
            byte[] bytesFile = p.getFileBytes();
            byte[] bytesToSend = null;
            long time = Instant.now().toEpochMilli();
            String serverDate = Long.toString(time);
            if (command == 'g') {
                bytesToSend = MessageCreator.createMessage(userName, 'g',serverDate);
                userNames.put(socket, userName);
            }
            if (command == 't') {
                bytesToSend = MessageCreator.createMessage(userName, 't', serverDate,
                        fileName, bytesFile.length, bytesFile);
            }

            if (command == 'c') {
                bytesToSend = MessageCreator.createMessage(userName, 'c', serverDate);
                closeSocket(userName);
            }

            if (command == 'f') {
                bytesToSend = MessageCreator.createMessage(userName, 'f', serverDate);
                writeToClients(bytesToSend, text);

                for (int i = 0; i < sockets.size(); i++) {
                    try {
                        closeSocket(userNames.get(sockets.get(i)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

            writeToClients(bytesToSend, text);
        }
    }

    private void writeToClients(byte[] bytesToSend, String text) {
        for (int i = 0; i < writers.size(); i++) {
            try {
                writers.get(sockets.get(i)).writeInt(bytesToSend.length);
                writers.get(sockets.get(i)).write(bytesToSend);
                writers.get(sockets.get(i)).writeUTF(text);
            } catch (IOException ignored) { }
        }
    }

    private void closeSocket(String userName) {
        userNames.remove(socket, userName);
        sockets.remove(socket);
        writers.remove(socket);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

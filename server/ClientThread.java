import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientThread extends Thread {

    Socket socket;
    HashMap<Socket, DataOutputStream> writers;
    List<Socket> sockets;
    ServerSocket serverSocket;

    public ClientThread(Socket socket,
                        HashMap<Socket, DataOutputStream> writers,
                        List<Socket> sockets,
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
            String text = "_";
            byte[] bytesReceived;
            try {
                lengthReceived = reader.readInt();
                bytesReceived = new byte[lengthReceived];
                text = reader.readUTF();
                reader.readFully(bytesReceived);
            } catch (IOException e) {
                String serverDate = getServerDate();
                byte[] bytesToSend = MessageCreator.createMessage(userNames.get(socket),
                        'c', serverDate);
                closeSocket(userNames.get(socket));
                try {
                    writeToClients(bytesToSend, "");
                } catch (Exception ignored) {}
                break;
            }
            Parser p = Parser.getAll(bytesReceived);
            String userName = p.getUserName();
            char command = p.getCommand();
            String fileName = p.getFileName();
            byte[] bytesFile = p.getFileBytes();
            byte[] bytesToSend = null;
            long time = Instant.now().toEpochMilli();
            String serverDate = Long.toString(time);
            if (command == Command.GREETING.getSymbol()) {
                bytesToSend = MessageCreator.createMessage(userName, Command.GREETING.getSymbol(), serverDate);
                userNames.put(socket, userName);
            }
            if (command == Command.TEXT.getSymbol()) {
                bytesToSend = MessageCreator.createMessage(userName, Command.TEXT.getSymbol(), serverDate,
                        fileName, bytesFile.length, bytesFile);
            }

            if (command == Command.CLOSE.getSymbol()) {
                bytesToSend = MessageCreator.createMessage(userName, Command.CLOSE.getSymbol(), serverDate);
                closeSocket(userName);
            }

            if (command == Command.FINISH_SERVER.getSymbol()) {
                bytesToSend = MessageCreator.createMessage(userName, Command.FINISH_SERVER.getSymbol(), serverDate);
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

    private String getServerDate() {
        long time = Instant.now().toEpochMilli();
        return Long.toString(time);
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

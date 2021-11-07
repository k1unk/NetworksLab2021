import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class ReceiveThread extends Thread {
    Socket socket;
    DataInputStream reader;

    public ReceiveThread(Socket socket) {
        try {
            this.reader = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            int length;
            byte[] bytes = new byte[0];
            try {
                length = reader.readInt();
                bytes = new byte[length];
                reader.readFully(bytes);
            } catch (IOException e) {
                try {
                    socket.close();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }

            Parser p = Parser.getAll(bytes);
            String userName = p.getUserName();
            char command = p.getCommand();
            String date = p.getDate();
            String text = null;
            try {
                text = reader.readUTF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String fileName = p.getFileName();
            int fileLength = p.getFileLength();
            byte[] bytesFile = p.getFileBytes();

            if (command == 'g') {
                System.out.println(
                        Color.YELLOW_BOLD_BRIGHT + userName +
                                Color.BLUE_BRIGHT + " joined the server at " +
                                Color.MAGENTA + date +
                                Color.RESET);
            }

            if (command == 't') {
                if (fileLength == 0) {
                    System.out.println(
                            Color.MAGENTA + "<" + date + "> " +
                                    Color.YELLOW_BOLD_BRIGHT + "[" + userName + "] " +
                                    Color.RESET + text);
                } else {
                    System.out.println(
                            Color.MAGENTA + "<" + date + "> " +
                                    Color.YELLOW_BOLD_BRIGHT + "[" + userName + "] " +
                                    Color.RESET + text +
                                    ", file: " + fileName);
                    try (FileOutputStream fos = new FileOutputStream(fileName +
                            new Random().nextInt(1000000))) {
                        fos.write(bytesFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (command == 'c') {
                System.out.println(
                        Color.YELLOW_BOLD_BRIGHT + userName +
                                Color.BLUE_BRIGHT + " left the server at " +
                                Color.MAGENTA + date +
                                Color.RESET);
            }
            if (command == 'f') {
                System.out.println(
                        Color.YELLOW_BOLD_BRIGHT + userName +
                                Color.BLUE_BRIGHT + " finished the server at " +
                                Color.MAGENTA + date +
                                Color.RESET);
                try {
                    socket.close();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}

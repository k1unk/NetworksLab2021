import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class SendThread extends Thread {
    Socket socket;
    String userName;

    public SendThread(Socket socket, String userName) {
        this.socket = socket;
        this.userName = userName;
    }

    public String getFileName(String filePath) {
        String[] arr = filePath.split("/");
        return arr[arr.length - 1];
    }

    @Override
    public void run() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream serverWriter = null;
        try {
            serverWriter = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = MessageCreator.createMessage(userName, 'g');
        try {
            serverWriter.writeInt(bytes.length);
            serverWriter.writeUTF("");
            serverWriter.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            String text = null;

            try {
                do {
                    text = consoleReader.readLine();
                } while (text.equals(""));
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean end = false;
            if (text.contains("///")) {
                String splittedText = "";
                try {
                    splittedText = text.split("///")[1].trim();
                } catch (Exception ignored) { }
                if (splittedText.equals("close")) {
                    bytes = MessageCreator.createMessage(userName, 'c');
                    end = true;
                } else if (splittedText.equals("finishServer")) {
                    bytes = MessageCreator.createMessage(userName, 'f');
                } else {
                    byte[] fileBytes = new byte[0];
                    String fileName = "";
                    try {
                        fileName = getFileName(splittedText);
                        File file = new File(splittedText);
                        fileBytes = Files.readAllBytes(file.toPath());
                    } catch (IOException e) {
                        System.out.println(Color.RED + "no such file" + Color.RESET);
                    }
                    bytes = MessageCreator.createMessage(userName, 't',
                            fileName,
                            fileBytes.length, fileBytes);
                }
            } else {
                bytes = MessageCreator.createMessage(userName, 't');
            }

            try {
                serverWriter.writeInt(bytes.length);
                String z = text.split("///")[0];
                serverWriter.writeUTF(z);
                serverWriter.write(bytes);
                if (end) break;
            } catch (IOException e) {
                try {
                    socket.close();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
}
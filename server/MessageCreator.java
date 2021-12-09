import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MessageCreator {

    public static byte[] createMessage(String userName, char command, String date) {
        return createMessage(userName, command, date, "", 0, null);
    }

    public static byte[] createMessage(String userName,
                                       char command,
                                       String date,
                                       String fileName,
                                       int fileLength,
                                       byte[] fileBytes) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(date));
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        String newDate = ldt.toString().replace("T", " ");
        if (command == Command.GREETING.getSymbol()) {
            System.out.println(userName + " joined the server at " + newDate);
        }
        if (command == Command.TEXT.getSymbol()) {
            if (fileLength == 0) {
                System.out.println("<" + newDate + "> [" + userName + "] " );
            } else {
                System.out.println("<" + newDate + "> [" + userName + "] "  +
                        ", file: " + fileName);
            }
        }
        if (command == Command.CLOSE.getSymbol()) {
            System.out.println(userName + " left the server at " + newDate);
        }
        if (command == Command.FINISH_SERVER.getSymbol()) {
            System.out.println(userName + " finishing the server at " + newDate);
        }

        String message =
                "['u':'" + userName.replace("'", "'\\") +
                        "','c':'" + command +
                        "','d':'" + date +
                        "','f':'" + fileName +
                        "','l':'" + fileLength +
                        "','b':'";
        byte[] bytes;
        if (fileLength == 0) {
            message += "']";
            bytes = message.getBytes();
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(message.getBytes());
                outputStream.write(fileBytes);
                outputStream.write("']".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            bytes = outputStream.toByteArray();
        }
        return bytes;
    }
}

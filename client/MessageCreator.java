import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageCreator {

    public static byte[] createMessage(String userName, char command) {
        return createMessage(userName, command, "", 0, null);
    }

    public static byte[] createMessage(String userName,
                                       char command,
                                       String fileName,
                                       int fileLength,
                                       byte[] fileBytes) {
        String message =
                "['u':'" + userName.replace("'", "'\\") +
                        "','c':'" + command +
                        "','d':'" + "" +
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

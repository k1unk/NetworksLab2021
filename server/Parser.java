public class Parser {
    private final String userName;
    private final char command;
    private final String date;
    private final String fileName;
    private final int fileLength;
    private final byte[] fileBytes;

    public Parser(String userName,
                  char command,
                  String date,
                  String fileName,
                  int fileLength,
                  byte[] fileBytes) {
        this.userName = userName;
        this.command = command;
        this.date = date;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.fileBytes = fileBytes;
    }

    public String getUserName() {
        return userName;
    }

    public char getCommand() {
        return command;
    }

    public String getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileLength() {
        return fileLength;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public static String createField(byte[] bytes, int index) {
        String field = "";
        int ind = index + 5;
        StringBuilder fieldBuilder = new StringBuilder(field);
        while (true) {
            if (bytes[ind] != '\'') {
                fieldBuilder.append((char) bytes[ind]);
                ind++;
            } else {
                if (bytes[ind + 1] == '\\') {
                    fieldBuilder.append((char) bytes[ind]);
                    ind += 2;
                } else {
                    break;
                }
            }
        }

        field = fieldBuilder.toString();
        return field;
    }

    public static Parser getAll(byte[] bytes) {
        int index = 0;
        String userName;
        char command;
        String date;
        String fileName;
        int fileLength;

        while (true) {
            index++;
            if (bytes[index] == '\'' && bytes[index + 1] == 'u' &&
                    bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                userName = createField(bytes, index);
                break;
            }
        }
        while (true) {
            index++;
            if (bytes[index] == '\'' && bytes[index + 1] == 'c' &&
                    bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                command = createField(bytes, index).charAt(0);
                break;
            }
        }
        while (true) {
            index++;
            if (bytes[index] == '\'' && bytes[index + 1] == 'd' &&
                    bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                date = createField(bytes, index);
                break;
            }
        }

        while (true) {
            index++;
            if (bytes[index] == '\'' && bytes[index + 1] == 'f' &&
                    bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                fileName = createField(bytes, index);
                break;
            }
        }
        while (true) {
            index++;
            if (bytes[index] == '\'' && bytes[index + 1] == 'l' &&
                    bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                fileLength = Integer.parseInt(createField(bytes, index));
                break;
            }
        }

        byte[] fileBytes = new byte[fileLength];
        while (true) {
            index++;
            try {
                if (bytes[index] == '\'' && bytes[index + 1] == 'b' &&
                        bytes[index + 2] == '\'' && bytes[index + 3] == ':') {
                    for (int i = index + 5; i < index + 5 + fileLength; i++) {
                        fileBytes[i - index - 5] = bytes[i];
                    }
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return new Parser(userName, command, date, fileName, fileLength, fileBytes);
    }
}

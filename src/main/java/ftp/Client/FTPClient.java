import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FtpClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2121;
    private static final int DATA_PORT = 30000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            String line;
            while (true) {
                System.out.print("$> ");
                line = consoleReader.readLine();
                String[] parts = line.trim().split(" ");
                String cmd = parts[0].toUpperCase();

                writer.write(line + "\n");
                writer.flush();

                switch (cmd) {
                    case "LIST":
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        }
                        break;
                    case "CWD":
                        if (parts.length != 2) {
                            System.out.println("命令格式错误，输入 HELP 查看命令格式");
                            break;
                        }
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        }
                        break;
                    case "STOR":
                        if (parts.length != 2) {
                            System.out.println("命令格式错误，输入 HELP 查看命令格式");
                            break;
                        }
                        String localFilePath = parts[1];
                        Path localPath = Paths.get(localFilePath);
                        if (!Files.exists(localPath)) {
                            System.out.println("本地文件不存在");
                            break;
                        }
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        } // 读取开始传输消息
                        try (Socket dataSocket = new Socket(SERVER_ADDRESS, DATA_PORT);
                             OutputStream dataOut = dataSocket.getOutputStream()) {
                            Files.copy(localPath, dataOut);
                        }
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        } // 读取传输结束消息
                        break;
                    case "RETR":
                        if (parts.length != 2) {
                            System.out.println("命令格式错误，输入 HELP 查看命令格式");
                            break;
                        }
                        String remoteFilePath = parts[1];
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        } // 读取开始传输消息
                        try (Socket dataSocket = new Socket(SERVER_ADDRESS, DATA_PORT);
                             InputStream dataIn = dataSocket.getInputStream()) {
                            Path localSavePath = Paths.get(remoteFilePath);
                            Files.copy(dataIn, localSavePath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        } // 读取传输结束消息
                        break;
                    case "HELP":
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                break;
                            }
                            System.out.println(line);
                        }
                        break;
                    case "QUIT":
                        System.out.println(reader.readLine());
                        return;
                    default:
                        System.out.println(reader.readLine());
                        System.out.println(reader.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
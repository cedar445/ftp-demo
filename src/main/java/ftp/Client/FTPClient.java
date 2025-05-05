import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FtpClient {
    private static final int PORT = 2121;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.print("请输入命令: ");
                String command = scanner.nextLine();
                String[] parts = command.trim().split(" ");
                String cmd = parts[0].toUpperCase();

                writer.write(command + "\n");
                writer.flush();

                if ("QUIT".equalsIgnoreCase(command)) {
                    System.out.println(reader.readLine());
                    System.out.println(reader.readLine());
                    break;
                } else if ("STOR".equalsIgnoreCase(cmd)) {
                    if (parts.length != 2) {
                        System.out.println("输入help查看命令格式");
                        continue;
                    }
                    // 发送文件
                    sendFile(socket, parts[1]);
                } else if ("RETR".equalsIgnoreCase(cmd)) {
                    if (parts.length != 2) {
                        System.out.println("输入help查看命令格式");
                        continue;
                    }
                    // 接收文件
                    receiveFile(socket, parts[1]);
                }

                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(Socket socket, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在或不可读");
            return;
        }

        try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {
            // 发送文件大小
            dos.writeLong(file.length());

            // 发送文件内容
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void receiveFile(Socket socket, String filePath) throws IOException {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             FileOutputStream fos = new FileOutputStream(filePath)) {
            // 接收文件大小
            long fileSize = dis.readLong();

            // 接收文件内容
            byte[] buffer = new byte[1024];
            long totalBytesRead = 0;
            int bytesRead;
            while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }
    }
}
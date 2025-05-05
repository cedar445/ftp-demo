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
                writer.write(command + "\n");
                writer.flush();

                if ("QUIT".equalsIgnoreCase(command)) {
                    System.out.println(reader.readLine());
                    System.out.println(reader.readLine());
                    break;
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
}
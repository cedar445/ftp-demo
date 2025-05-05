package ftp.Client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class FTPClient {
    private static final int BUFFER_SIZE = 8192;
    private Socket controlSocket;
    private BufferedReader in;
    private PrintWriter out;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;

    public void connect(String host, int port) throws IOException {
        controlSocket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
        out = new PrintWriter(controlSocket.getOutputStream(), true);
        dataIn = new DataInputStream(controlSocket.getInputStream());
        dataOut = new DataOutputStream(controlSocket.getOutputStream());
        System.out.println(in.readLine());
    }

    public void disconnect() {
        try {
            sendCommand("QUIT");
            controlSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCommand(String command) {
        out.println(command);
    }

    public void handleList() {
        try {
            sendCommand("LIST");
            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
                if (response.startsWith("226")) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCwd(String directory) {
        try {
            sendCommand("CWD " + directory);
            System.out.println(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlePut(String localFile, String remoteFile) {
        try {
            Path filePath = Paths.get(localFile);
            if (Files.isRegularFile(filePath)) {
                sendCommand("STOR " + remoteFile);
                long fileSize = Files.size(filePath);
                dataOut.writeLong(fileSize);
                try (InputStream fileIn = Files.newInputStream(filePath)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    while ((read = fileIn.read(buffer)) != -1) {
                        dataOut.write(buffer, 0, read);
                    }
                }
                System.out.println(in.readLine());
            } else {
                System.out.println("Local file not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleGet(String remoteFile, String localFile) {
        try {
            sendCommand("RETR " + remoteFile);
            String response = in.readLine();
            if (response.startsWith("150")) {
                long fileSize = dataIn.readLong();
                try (OutputStream fileOut = Files.newOutputStream(Paths.get(localFile))) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    long remaining = fileSize;
                    while (remaining > 0) {
                        int read = dataIn.read(buffer, 0, (int) Math.min(remaining, BUFFER_SIZE));
                        if (read == -1) break;
                        fileOut.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
                System.out.println(in.readLine());
            } else {
                System.out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FTPClient client = new FTPClient();
        try {
            client.connect("localhost", 2121);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] parts = input.split(" ", 3);
                String cmd = parts[0].toLowerCase();

                switch (cmd) {
                    case "list":
                        client.handleList();
                        break;
                    case "cd":
                        if (parts.length > 1) {
                            client.handleCwd(parts[1]);
                        } else {
                            System.out.println("Usage: cd <directory>");
                        }
                        break;
                    case "put":
                        if (parts.length > 1) {
                            String localFile = parts[1];
                            String remoteFile = parts.length > 2 ? parts[2] : Paths.get(localFile).getFileName().toString();
                            client.handlePut(localFile, remoteFile);
                        } else {
                            System.out.println("Usage: put <local_file> [remote_filename]");
                        }
                        break;
                    case "get":
                        if (parts.length > 1) {
                            String remoteFile = parts[1];
                            String localFile = parts.length > 2 ? parts[2] : remoteFile;
                            client.handleGet(remoteFile, localFile);
                        } else {
                            System.out.println("Usage: get <remote_file> [local_filename]");
                        }
                        break;
                    case "quit":
                        client.disconnect();
                        return;
                    default:
                        System.out.println("Invalid command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package ftp.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FtpServer implements Runnable {
    private int port;
    private ExecutorService executor = Executors.newCachedThreadPool();

    public FtpServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FTP Server 端口 : " + port);
            while (true) {
                System.out.println("等待客户端连接...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("连接成功");
                executor.submit(new FtpClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


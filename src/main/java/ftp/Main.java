package ftp;

import ftp.Server.FtpClientHandler;
import ftp.Server.FtpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Main {
    private int port;
    public static void main(String[] args){
//        try (ServerSocket serverSocket = new ServerSocket(2121)) {
//            System.out.println("FTP Server started on port " + 2121);
//            while (true) {
//                System.out.println("等待客户端连接...");
//                Socket clientSocket = serverSocket.accept();
//                FtpClientHandler ftpClientHandler=new FtpClientHandler(clientSocket);
//                System.out.println(ftpClientHandler.currentDir);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        SpringApplication.run(Main.class, args);
        new Thread(new FtpServer(2121)).start(); // 启动 FTP 服务线程
    }
}

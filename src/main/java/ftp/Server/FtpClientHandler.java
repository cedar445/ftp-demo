package ftp.Server;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;

public class FtpClientHandler implements Runnable {
    private final Socket socket;
    private Path currentDir = Paths.get("src/main/resources/ftp_root");
    private int buffer_size=1024;
    private  final int DATA_PORT=30000;

    public FtpClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        try {
            Files.createDirectories(currentDir); // 确保目录存在
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerSocket passiveDataSocket = new ServerSocket(0);
    private Socket dataConnectionSocket = new ServerSocket(0).accept();

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                String cmd = parts[0].toUpperCase();
                String cmdHelp = "LIST : 列出当前目录文件\n" +
                        "CWD : 切换当前目录,参数为目标地址\n" +
                        "STOR : 将本地文件发送到服务器，参数为目标文件\n" +
                        "RETR : 将服务器文件下载到本地，参数为目标文件\n" +
                        "QUIT : 退出当前连接\n" +
                        "HELP : 获取命令帮助\n";
                switch (cmd) {
                    case "LIST":
                        handleList(parts,writer);
                        break;
                    case "CWD":
                        handleCwd(parts, writer);
                        break;
                    case "STOR":
                        handleStor(parts, writer);
                        break;
                    case "RETR":
                        handleRetr(parts, writer);
                        break;
                    case "HELP":
                        writer.write("命令如下:"+"\n");
                        writer.write(cmdHelp+"\n");
                        writer.flush();
                        break;
                    case "QUIT":
                        writer.write("再见!\n");
                        writer.write("按下enter以继续\n");
                        writer.flush();
                        return;
                    default:
                        writer.write("无法识别的命令\n");
                        writer.write("输入help查看命令格式"+"\n");
                        writer.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleList(String[] parts,BufferedWriter writer) throws IOException {
        //命令格式判断
        if(parts.length!=1){
            writer.write("输入help查看命令格式"+"\n");
            writer.write("\n");
            writer.flush();
            return;
        }
        //命令处理
        File[] files = currentDir.toFile().listFiles();
        writer.write("当前目录:\n"+currentDir+"\n");
        writer.write("----------\n");
        if (files != null && files.length > 0) {
            for (File f : files) {
                writer.write(f.getName() + (f.isDirectory() ? "/" : "") + "\n");
            }
        }
        else{
            writer.write("当前目录为空\n");
        }
        writer.write("----------\n");
        writer.write("\n");
        writer.flush();
    }
    private void handleCwd(String[] parts, BufferedWriter writer) throws IOException {
        //命令格式判断
        if(parts.length!=2){
            writer.write("输入help查看命令格式"+"\n");
            writer.write("\n");
            writer.flush();
            return;
        }
        String tarDir=parts[1];
        String[] tarDirParts = tarDir.trim().split("/");
        writer.write("----------\n");
        String[] curDirParts=new String[currentDir.getNameCount()];
        for (int i = 0; i < currentDir.getNameCount(); i++) {
            curDirParts[i] = currentDir.getName(i).toString();
        }
        for (String p : tarDirParts) {
            if(Objects.equals(p, "..")){
                if(!Objects.equals(curDirParts[curDirParts.length - 1], "ftp_root")) {
                    curDirParts = Arrays.copyOf(curDirParts, curDirParts.length - 1);
                }
                else{
                    writer.write("无管理权限\n");
                }
            }
            else if(!Objects.equals(p, ".")){
                String[] extended = Arrays.copyOf(curDirParts, curDirParts.length + 1);
                extended[extended.length - 1] = p;
                curDirParts=extended;
            }
        }
        String res=curDirParts[0];
        for(int i=1;i<curDirParts.length;i++){
            res+="/"+curDirParts[i];
        }
        currentDir= Paths.get(res);
        if(Files.exists(currentDir)) {

            writer.write("切换到目录:\n" + currentDir + "\n");
        }
        else{
            writer.write("目标路径不存在");
        }
        writer.write("----------\n");
        writer.write("\n");
        writer.flush();
    }
    private void handleStor(String[] parts, BufferedWriter writer) throws IOException {
        //命令格式判断
        if(parts.length!=2){
            writer.write("输入help查看命令格式"+"\n");
            writer.write("\n");
            writer.flush();
            return;
        }
        writer.write("开始传输\n");
        writer.flush();
        Path filePath = currentDir.resolve(parts[1]).normalize();
        if (!filePath.startsWith(currentDir)) {
            writer.write("非法路径，访问被拒绝\n\n");
            writer.flush();
            return;
        }

        try (ServerSocket dataServer = new ServerSocket(DATA_PORT)) {
            Socket dataSocket = dataServer.accept(); // 阻塞直到客户端连接
            OutputStream dataOut = dataSocket.getOutputStream();
            Files.copy(filePath, dataOut); // 文件传输
            dataOut.flush();
            dataSocket.close();
        } catch (IOException e) {
            writer.write("数据传输失败：" + e.getMessage() + "\n\n");
            writer.flush();
            return;
        }

        writer.write("传输结束\n");
        writer.write("\n");
        writer.flush();
    }
    private void handleRetr(String[] parts, BufferedWriter writer) throws IOException {
        //命令格式判断
        if(parts.length!=2){
            writer.write("输入help查看命令格式"+"\n");
            writer.write("\n");
            writer.flush();
            return;
        }
        Path filePath = currentDir.resolve(parts[1]).normalize();
        if (!filePath.startsWith(currentDir)) {
            writer.write("非法路径，访问被拒绝\n\n");
            writer.flush();
            return;
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            writer.write("文件不存在或不可读\n\n");
            writer.flush();
            return;
        }

        writer.write("开始传输\n");
        writer.flush();

        try (ServerSocket dataServer = new ServerSocket(DATA_PORT)) {
            Socket dataSocket = dataServer.accept();
            InputStream dataIn = dataSocket.getInputStream();
            Files.copy(dataIn, filePath, StandardCopyOption.REPLACE_EXISTING);
            dataSocket.close();
        } catch (IOException e) {
            writer.write("数据接收失败：" + e.getMessage() + "\n\n");
            writer.flush();
            return;
        }

        writer.write("传输结束\n");
        writer.write("\n");
        writer.flush();
    }
}


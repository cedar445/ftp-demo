package ftp.Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FtpClientHandler implements Runnable {
    private final Socket socket;
    private Path currentDir = Paths.get("src/main/resources/ftp_root").toAbsolutePath();

    public FtpClientHandler(Socket socket) {
        this.socket = socket;
        try {
            Files.createDirectories(currentDir); // 确保目录存在
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                String cmd = parts[0].toUpperCase();
                String cmdHelp = "LIST : 列出当前目录文件\n" +
                        "CWD : 切换当前目录,参数为目标地址\n" +
                        "STOR : \n" +
                        "RETR : \n" +
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
                        handleStor(parts, writer, socket.getInputStream());
                        break;
                    case "RETR":
                        handleRetr(parts, writer, socket.getOutputStream());
                        break;
                    case "HELP":
                        writer.write("命令如下:"+"\n");
                        writer.write(cmdHelp+"\n");
                        writer.flush();
                        break;
                    case "QUIT":
                        writer.write("再见！\n");
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
        writer.write("当前目录:"+currentDir+"\n");
        if (files != null && files.length > 0) {
            for (File f : files) {
                writer.write(f.getName() + (f.isDirectory() ? "/" : "") + "\n");
            }
        }
        else{
            writer.write("当前目录为空\n");
        }
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
        writer.write("\n");
        writer.flush();
    }
    private void handleStor(String[] parts, BufferedWriter writer, InputStream inputStream) throws IOException {
        //命令格式判断
        if(parts.length!=2){
            writer.write("输入help查看命令格式"+"\n");
            writer.write("\n");
            writer.flush();
            return;
        }
        writer.write("\n");
        writer.flush();
    }
    private void handleRetr(String[] parts, BufferedWriter writer, OutputStream outputStream) throws IOException {
        //命令格式判断
        if(parts.length!=2){
            writer.write("输入help查看命令格式"+"\n");
            writer.write("\n");
            writer.flush();
            return;
        }
        writer.write("\n");
        writer.flush();
    }
}


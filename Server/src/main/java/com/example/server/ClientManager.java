package com.example.server;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientManager {
    private static final int PORT = 50000;
    private static List<Socket> clientList = new ArrayList<>();
    private static ServerSocket server = null;
    private static ExecutorService clientES = null;//thread pool

    public static void main(String[] args){
        try{
            server = new ServerSocket(PORT);
            clientES = Executors.newCachedThreadPool();
            System.out.println("server is running");
            while (true){
                Socket client = server.accept();
                System.out.println("Accept new connection from "+client.getPort());


                clientList.add(client);
                System.out.println("online number:"+clientList.size());
                clientES.execute(new ServerRunnable(client));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static class ServerRunnable implements Runnable{
        private Socket socket;
        public ServerRunnable(Socket socket){
            this.socket = socket;
        }

        public void sendMessageAll(String content, String name,String port,String time, int type, int num){
            for(Socket sk:clientList){
                try{
                    // 重新组装成json，发送给client
                    OutputStream outputStream = sk.getOutputStream();
                    ChatBean chatBean_new = new ChatBean(content, name, port, time, type, num);
                    outputStream.write((chatBean_new.toJsonString() + "\r\n").getBytes(StandardCharsets.UTF_8));
                    System.out.println((chatBean_new.toJsonString() + "\r\n"));
                    outputStream.flush();

                    Thread.sleep(200);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try{
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(os,StandardCharsets.UTF_8));
                while(true){
                    String message = reader.readLine();
                    ChatBean chatBean = new Gson().fromJson(message,ChatBean.class);
                    // 拆开json，获取客户端发送的信息
                    String content = chatBean.getContent();
                    String name = chatBean.getName();
                    String port = chatBean.getHisPort();
                    String time = chatBean.getTime();
                    int type = chatBean.getType();
                    int num = chatBean.getNum();

                    if(type == -1) {
                        // 群发某用户加入聊天室的信息
                        String str = chatBean.getName() + " is online.";
                        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
                        this.sendMessageAll(str, "server", String.valueOf(socket.getLocalPort()), date_format.format(new Date()), 0, clientList.size());
                    }else if(content.equals("exit")){
                        // 群发某用户离开的信息
                        String str1 = chatBean.getName()+" is offline.";
                        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm:ss");
                        this.sendMessageAll(str1, name, String.valueOf(socket.getLocalPort()), date_format.format(new Date()), 0, clientList.size()-1);
                        // 关闭socket
                        clientList.remove(socket);  // 从列表中删除socket
                        reader.close();
                        socket.close();
                        break;
                    }else{
                        // 群发聊天信息
                        this.sendMessageAll(content, name, port, time, type, clientList.size());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

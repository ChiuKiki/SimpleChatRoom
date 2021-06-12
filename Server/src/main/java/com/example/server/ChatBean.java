package com.example.server;

public class ChatBean {
    private String content;
    private String name;
    private String hisPort;
    private String time;
    private int type;
    private int num;

    public ChatBean(String content, String name, String hisPort, String time,int type, int num) {
        this.content = content;
        this.name = name;
        this.hisPort = hisPort;
        this.time = time;
        this.type = type;
        this.num = num;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public String getHisPort() {
        return hisPort;
    }

    public String getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public int getNum() {
        return num;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHisPort(String hisPort) {
        this.hisPort = hisPort;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setNum(int num) {
        this.num = num;
    }


    public String toJsonString(){
        String res = "{\"content\":\""+content+"\",\"name\":\""+name+"\",\"hisPort\":\""+hisPort+"\",\"time\":\""+time+"\",\"type\":"+type+",\"num\":"+num+"}";
        return res;
    }
}

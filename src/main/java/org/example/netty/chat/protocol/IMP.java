package org.example.netty.chat.protocol;

public enum IMP {
    SYSTEM ("SYSTEM"),
    LOGIN ("LOGIN"),
    LOGOUT ("LOGOUT"),
    CHAT ("CHAT"),
    FLOWER ("FLOWER");

    private String name;
    public static boolean isIMP (String content){
        return content.matches("^\\[(SYSTEM|LOGIN|LOGOUT|CHAT)\\]");
    }

    IMP (String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public String toString(){
        return this.name;
    }
}

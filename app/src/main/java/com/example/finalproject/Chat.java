package com.example.finalproject;

import androidx.annotation.Nullable;

public class Chat {
    private String from;
    private String txt;
    private long timestamp;
    public Chat(){}

    public Chat(String from,String txt,long timestamp){
        this.from = from;
        this.txt = txt;
        this.timestamp=timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        System.out.println("----------------> equals");
        Chat clone  = (Chat)obj;
        if(clone.getFrom().equals(this.getFrom()) && clone.getTxt().equals(this.getTxt()) && clone.getTimestamp() == this.getTimestamp())
            return true;
        else
            return false;
    }
}

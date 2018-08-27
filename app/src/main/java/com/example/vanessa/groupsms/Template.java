package com.example.vanessa.groupsms;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Template {
    public String title;
    public String content;
    private static int ID = 0;
    public String id;
    public Template() {
      /*Blank default constructor essential for Firebase*/
    }

    //Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Template(String title, String content, String id) {
        this.title = title;
        this.content=content;
        this.id = id;
        //this.id = ID;
        //ID++;
    }
}







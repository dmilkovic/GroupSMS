package com.example.vanessa.groupsms;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Group {
    public String name;
    public ArrayList<String> members= new ArrayList<String>();

    public Group() {
      /*Blank default constructor essential for Firebase*/
    }

    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getMembers() {
        return members;
    }

    public void addMember(String member) {
        this.members.add(member);
    }

    public Group(String name, ArrayList members) {
        this.name = name;
        this.members=members;
    }
}







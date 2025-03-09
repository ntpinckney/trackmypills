package com.example.trackmypills;

public enum AdminType {
    PILLS("pill(s)"),
    CAPSULES("capsule(s)"),
    ML("mL"),
    TSP("tsp(s)"),
    INHALERS("puff(s)"),
    CREAMS("cream(s)"),
    PATCHES("patch(es)");

    private final String displayName;

    AdminType(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }

}
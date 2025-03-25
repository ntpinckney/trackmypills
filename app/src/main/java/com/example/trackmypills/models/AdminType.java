package com.example.trackmypills.models;

public enum AdminType {
    PILLS("pill(s)"),
    CAPSULES("capsule(s)"),
    ML("mL"),
    TSP("tsp(s)"),
    INHALERS("puff(s)"),
    CREAMS("cream(s)"),
    PATCHES("patch(es)");

    private final String label;

    AdminType(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
package com.example.trackmypills.models;

public enum Frequency {
    THIRTY_MINUTES(0.5, "Every 30 minutes"),
    ONE_HOUR(1, "Every hour"),
    TWO_HOURS(2, "Every 2 hours"),
    FOUR_HOURS(4, "Every 4 hours"),
    SIX_HOURS(6, "Every 6 hours"),
    EIGHT_HOURS(8, "Every 8 hours"),
    TEN_HOURS(10, "Every 10 hours"),
    TWELVE_HOURS(12, "Every 12 hours"),
    TWENTY_FOUR_HOURS(24, "Every 24 hours");

    private final double intervalHours;
    private final String label;

    Frequency(double intervalHours, String label){
        this.intervalHours = intervalHours;
        this.label = label;
    }

    public double getIntervalHours() {
        return intervalHours;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}

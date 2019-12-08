package main.java.logic;

import javafx.application.Platform;

public class Planet {

    private double masse;
    private double radius;

    public Planet(double masse, double radius){
        this.masse = masse;
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getMasse() {
        return masse;
    }

    public void setMasse(double masse) {
        this.masse = masse;
    }
}

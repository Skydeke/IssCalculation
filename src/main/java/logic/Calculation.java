package main.java.logic;

import javax.swing.*;

public class Calculation extends SwingWorker {

    private final double zeitschritt;
    private Planet planet;

    private Satellit satellit = new Satellit(0, 408*1000+12748.3/2*1000, 7660, 0);

    private double zeit = 0;
    private double lastZeit = 0;

    public Calculation(double zeitschritt, Planet planet, Satellit satellit){
        this.zeitschritt = zeitschritt;
        this.planet = planet;
        this.satellit = satellit;
    }

    @Override
    public Satellit doInBackground() {
        //System.out.println("Starting calculations...");
        double deltaZeit = zeit-lastZeit;
        //Radius Satellit Massepunkt(0, 0)
        double r = Math.sqrt((satellit.getX() * satellit.getX() + satellit.getY() * satellit.getY()));

        //Die Beschleunigung in X-Richtung
        double ax = (-1 * Konstanten.G * planet.getMasse() * satellit.getX())/(r*r*r);
        satellit.setVx(satellit.getVx() + ax * deltaZeit);
        satellit.setX(satellit.getX() + satellit.getVx() * deltaZeit);

        //Die Beschleunigung in X-Richtung
        double ay = (-1 * Konstanten.G * planet.getMasse() * satellit.getY())/(r*r*r);
        satellit.setVy(satellit.getVy() + ay * deltaZeit);
        satellit.setY(satellit.getY() + satellit.getVy() * deltaZeit);

        satellit.setTime(deltaZeit);

        lastZeit = zeit;
        zeit = zeit + zeitschritt;
        return satellit;
    }

}

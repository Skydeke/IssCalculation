package main.java;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.java.logic.Calculation;
import main.java.logic.Planet;
import main.java.logic.Satellit;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class Controller implements Initializable {

    private JFreeChart chart;
    private XYPlot plot;
    private XYLineAndShapeRenderer renderer;
    private XYSeriesCollection dataset;
    private Thread calculateThread;
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    @FXML
    ChartViewer viewer;
    @FXML
    Button resetCalculation;
    @FXML
    Button startCalculation;
    @FXML
    Button stopCalculation;
    @FXML
    TextField zeitSchritt;
    @FXML TextField planetMass;
    @FXML TextField planetRadius;
    @FXML TextField sStartX;
    @FXML TextField sStartY;
    @FXML TextField sGeschX;
    @FXML TextField sGeschY;
    @FXML
    Label errorLabel;

    public Controller() {
        calculateThread = new Thread(() -> {
            System.out.println("Ready!! To do some Math!");
        });
        calculateThread.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startCalculation.setDisable(true);
        stopCalculation.setDisable(true);
        resetUI();
        resetCalculation.setOnAction(e -> {
            if (calculateThread.isAlive())
                calculateThread.stop();
            Platform.runLater(() -> {
                dataset.getSeries(0).clear();
                calculateThread = new Thread(() -> {
                    double radius = Double.parseDouble(planetRadius.getText());
                    Planet planet = new Planet(Double.parseDouble(planetMass.getText()), radius);
                    plot.clearAnnotations();
                    plot.addAnnotation(new XYShapeAnnotation(new Ellipse2D.Double(0 - radius, 0 - radius, radius + radius, radius + radius)));
                    final Calculation ct = new Calculation(getZeitschrittAsNumber(),
                            planet, new Satellit(Double.parseDouble(sStartX.getText()),
                            Double.parseDouble(sStartY.getText()),
                            Double.parseDouble(sGeschX.getText()), Double.parseDouble(sGeschY.getText())));
                    while (calculateThread.isAlive()) {
                        Satellit s = ct.doInBackground();
                        add(s.getX(), s.getY(), s.getTime(), s.getVx(), s.getVy());
                        try {
                            sleep(10);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                });
                startCalculation.setDisable(false);
                stopCalculation.setDisable(true);
            });
        });
        startCalculation.setOnAction(e -> {
            if (calculateThread != null && !calculateThread.isInterrupted()
                    && !calculateThread.isAlive()) {
                try {
                    calculateThread.start();
                    System.out.println("Starting to calculate...");
                    startCalculation.setDisable(true);
                    stopCalculation.setDisable(false);
                } catch (IllegalThreadStateException e1) {
                    System.out.println("Shit, buggy");
                }
            }
        });
        stopCalculation.setOnAction(e -> {
            calculateThread.stop();
            startCalculation.setDisable(true);
            stopCalculation.setDisable(true);
        });
    }

    private void resetUI() {
        dataset = new XYSeriesCollection();
        XYSeries bahndaten = new XYSeries("Umlaufbahn");
        dataset.addSeries(bahndaten);
        xAxis = new NumberAxis();
        xAxis.setLowerMargin(0.00); // percentage to be added to lower part of axis when recalculating
        xAxis.setAutoRange(true);
        xAxis.setAutoRangeMinimumSize(new Integer(5));
        yAxis = new NumberAxis();
        yAxis.setLowerMargin(0.00); // percentage to be added to lower part of axis when recalculating
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeMinimumSize(new Integer(5));
        renderer = new XYLineAndShapeRenderer(false, true);
        plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        chart = new JFreeChart("ISS-Flugbahn", plot);
        renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
        viewer.setChart(chart);
        viewer.addChartMouseListener(new ChartMouseListenerFX() {
            @Override
            public void chartMouseClicked(ChartMouseEventFX e) {
                ChartEntity ce = e.getEntity();
                if (ce instanceof XYItemEntity) {
                    XYItemEntity item = (XYItemEntity) ce;
                    renderer.setSeriesVisible(item.getSeriesIndex(), false);
                } else if (ce instanceof LegendItemEntity) {
                    LegendItemEntity item = (LegendItemEntity) ce;
                    Comparable key = item.getSeriesKey();
                    renderer.setSeriesVisible(dataset.getSeriesIndex(key), false);
                } else {
                    for (int i = 0; i < dataset.getSeriesCount(); i++) {
                        renderer.setSeriesVisible(i, true);
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEventFX e) {
            }
        });
        zeitSchritt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)?")) {
                    zeitSchritt.setText(newValue.replaceAll("[^\\d*(\\.\\d*)?]", ""));
                }
            }
        });
        planetMass.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)e\\d*")) {
                    planetMass.setText(newValue.replaceAll("[^\\d*(\\.\\d*)e\\d*]", ""));
                }
            }
        });
        planetRadius.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)e\\d*")) {
                    planetRadius.setText(newValue.replaceAll("[^\\d*(\\.\\d*)e\\d*]", ""));
                }
            }
        });
        sStartX.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)e\\d*")) {
                    sStartX.setText(newValue.replaceAll("[^\\d*(\\.\\d*)e\\d*]", ""));
                }
            }
        });
        sStartY.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)e\\d*")) {
                    sStartY.setText(newValue.replaceAll("[^\\d*(\\.\\d*)e\\d*]", ""));
                }
            }
        });
        sGeschX.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)e\\d*")) {
                    sGeschX.setText(newValue.replaceAll("[^\\d*(\\.\\d*)e\\d*]", ""));
                }
            }
        });
        sGeschY.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*(\\.\\d*)e\\d*")) {
                    sGeschY.setText(newValue.replaceAll("[^\\d*(\\.\\d*)e\\d*]", ""));
                }
            }
        });
    }

    private double getZeitschrittAsNumber() {
        double d = 0.01;
        try {
            d = Double.parseDouble(zeitSchritt.getText().replace(",", "."));
        } catch (NumberFormatException e3) {
            Platform.runLater(() -> errorLabel.setText("Bitte den Zeitschritt überprüfen."));
        }
        return d > 0 ? d : 0.01;
    }

    public synchronized void add(final double x, final double y, double time, double vx, double vy) {
        if (dataset.getSeries().get(0) == null) {
            System.out.println("Thats bad.");
        }
        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            Platform.runLater(() -> {
                synchronized (dataset.getSeries().get(0)) {
                    ((XYSeries) dataset.getSeries().get(0)).add(x, y);
                    errorLabel.setText("Bahngeschwindigkeit: " + Math.sqrt(vx * vx + vy * vy) +
                                " Vx: " + vx + " Vy: " + vy);

                }
            });
        }
    }
}

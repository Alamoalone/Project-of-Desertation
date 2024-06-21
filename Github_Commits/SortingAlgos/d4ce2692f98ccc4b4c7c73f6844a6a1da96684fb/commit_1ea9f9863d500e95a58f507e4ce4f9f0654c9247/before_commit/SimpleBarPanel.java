package graphics;

import javafx.animation.Animation;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;



public class SimpleBarPanel extends Application {

    private ExecutorService exec = Executors.newCachedThreadPool(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    private ExecutorService exec1 = Executors.newCachedThreadPool(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    private ExecutorService exec2 = Executors.newCachedThreadPool(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    static Series<String, Number> series;
    static Series<String, Number> series1;
    static Series<String, Number> series2;

    //@Override
    public void start(Stage stage) {
        //Defining the axes
        CategoryAxis xAxis = new CategoryAxis();
        CategoryAxis xAxis1 = new CategoryAxis();
        CategoryAxis xAxis2 = new CategoryAxis();

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("value");
        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("value");
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("value");

        //Creating the Bar chart
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        BarChart<String, Number> barChart1 = new BarChart<>(xAxis1, yAxis1);
        BarChart<String, Number> barChart2 = new BarChart<>(xAxis2, yAxis2);


        //Prepare XYChart.Series objects by setting data
        series = createSeries();
        series1 = createSeries();
        series2 = createSeries();

        //Setting the data to bar chart
        barChart.getData().addAll(series);
        barChart1.getData().addAll(series1);
        barChart2.getData().addAll(series2);
        
        barChart.setBarGap(0);
        barChart1.setBarGap(0);
        barChart2.setBarGap(0);

        Task<Void> sortingAlgoTask = createSelectionSortingTask(series);
        Task<Void> sortingAlgoTask1 = createBubbleSortingTask(series1);
        Task<Void> sortingAlgoTask2 = createInsertionSortingTask(series2);



        //Creating a Group object
        BorderPane root = new BorderPane(barChart);
        BorderPane root1 = new BorderPane(barChart1);
        BorderPane root2 = new BorderPane(barChart2);

        HBox hbox = new HBox(0);
        hbox.getChildren().addAll(root);
        hbox.getChildren().addAll(root1);
        hbox.getChildren().addAll(root2);
       
        //Creating a scene object
        Scene scene = new Scene(hbox, 1000, 800);

        //Setting title to the Stage
        stage.setTitle("Bar Chart");

        //Adding scene to the stage
        stage.setScene(scene);


        //Displaying the contents of the stage
        stage.show();

        try {
            exec.submit(sortingAlgoTask);
            exec1.submit(sortingAlgoTask1);
            exec2.submit(sortingAlgoTask2);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Program executed successfully!");
        }

    }

    private Task<Void> createBubbleSortingTask(Series<String, Number> seriesCur) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                ObservableList<Data<String, Number>> data = seriesCur.getData();
                for (int i = data.size() - 1; i >= 0; i--) {
                    for (int j = 0 ; j < i; j++) {

                        Data<String, Number> first = data.get(j);
                        Data<String, Number> second = data.get(j + 1);


                       first.getNode().setStyle("-fx-background-color: green ;");
                       second.getNode().setStyle("-fx-background-color: green ;");
                       
                        if (first.getYValue().doubleValue() > second.getYValue().doubleValue()) {
                            Number temp = first.getYValue().doubleValue();
                            first.setYValue(second.getYValue().doubleValue());
                            second.setYValue(temp);
                        }
                        Thread.sleep(75);
                        
                        first.getNode().setStyle("-fx-background-color: red ;");
                        second.getNode().setStyle("-fx-background-color: red ;");

                    }
                }
                return null;
            }
        };
    }
    
    private Task<Void> createInsertionSortingTask(Series<String, Number> seriesCur) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                ObservableList<Data<String, Number>> data = seriesCur.getData();
                
                int n = data.size();  
                for (int j = 1; j < n; j++) {  
                    Double key = data.get(j).getYValue().doubleValue(); 
                    int i = j-1; 
                    
                    Data<String, Number> first = data.get(j);
                    Data<String, Number> second = data.get(j - 1);

                   Thread.sleep(50);

                   first.getNode().setStyle("-fx-background-color: green ;");
                   //second.getNode().setStyle("-fx-background-color: green ;");
                   
                    while ( (i > -1) && (data.get(i).getYValue().doubleValue() > key ) ) { 
                    	Data<String, Number> currentFirst = data.get(i);
                    	currentFirst.getNode().setStyle("-fx-background-color: green ;");
                    	data.get(i+1).setYValue(data.get(i).getYValue());  
                        i--;  
                    }  
                    
                    data.get(i+1).setYValue(key);
                    
                    Thread.sleep(75);
                    

                   // second.getNode().setStyle("-fx-background-color: red ;");
                }  
                
                return null;
            }
        };
    }

    private Task<Void> createSelectionSortingTask(Series<String, Number> seriesCur) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                ObservableList<Data<String, Number>> data = seriesCur.getData();

                int n = data.size();

                for (int i = 0; i < n; i++) {
                    //Double smallestNum = Double.MAX_VALUE;
                    Data<String, Number> key = data.get(i);
                    for (int j = i+1; j < n; j++) {
                        Double temp;

                        data.get(j).getNode().setStyle("-fx-background-color: green ;");
                        data.get(i).getNode().setStyle("-fx-background-color: green ;");
                        Thread.sleep(75);


                        if ( key.getYValue().doubleValue() > data.get(j).getYValue().doubleValue() )  {
//                            System.out.println(data.get(i));
//                            System.out.println(data.get(j));

                            temp = key.getYValue().doubleValue();
                            key.setYValue(data.get(j).getYValue());
                            data.get(j).setYValue(temp);
                        }

                        data.get(i).getNode().setStyle("-fx-background-color: red ;");
                        data.get(j).getNode().setStyle("-fx-background-color: red ;");

                    }
                    data.get(i).getNode().setStyle("-fx-background-color: purple ;");
                    System.out.println(data.get(i));
                }

                return null;
            }
        };
    }
    
    public static Series<String,Number> createSeries() {
        Series<String, Number> seriesL = new Series<>();
        seriesL.setName("Index");
        for (int i = 0; i < 15; i++) {
//            xAxis.setCategories(FXCollections.<String>
//                    observableArrayList(Arrays.asList("Order")));
            Number randomNumber = (int) (Math.random() * 1000);
            seriesL.getData().add(new Data<>("" + i, randomNumber));
        }

        return seriesL;
    }

    
    public static void main(String args[]) throws InterruptedException {
        launch(args);
    }

}
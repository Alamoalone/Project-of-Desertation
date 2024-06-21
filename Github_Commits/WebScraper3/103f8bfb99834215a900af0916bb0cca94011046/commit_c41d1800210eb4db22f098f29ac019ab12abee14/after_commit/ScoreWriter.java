package me.stephenminer.writers;

import me.stephenminer.writers.OutputWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class ScoreWriter extends OutputWriter<HashMap<String,Double>> {
    public ScoreWriter(String className, HashMap<String,Double> scores){
        super("reflection-scores",className,scores);
    }

/*
    public void output(){
        try {
            String path = WebScraper3.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replace("WebScraper3-1.0-SNAPSHOT-shaded.jar", "reflection-scores-" + className + ".csv");
            System.out.println(path);
            File file = new File(path);
            boolean created = file.createNewFile();
            if (created) {
                System.out.println("Created a new file" + file.getName() + " at " + file.getCanonicalPath());
            }else{
                file.delete();
                if (file.createNewFile()) System.out.println("Created a new file" + file.getName() + " at " + file.getCanonicalPath());
            }
            writeData(file);
        }catch (Exception e){e.printStackTrace();}
    }
 */



    @Override
    protected void writeData(File file){
        try {
            FileWriter writer = new FileWriter(file, false);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write("Student-Name,Reflection-Points");
            bufferWriter.newLine();
            for (String name : data.keySet()){
                double points = data.get(name);

                bufferWriter.write(name + "," + points + ",");
                bufferWriter.newLine();
            }
            bufferWriter.close();
            writer.close();
        }catch (Exception e){e.printStackTrace();}
    }





    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        for (String name : data.keySet()){
            double score = data.get(name);
            builder.append(name + "," + score);
        }
        return builder.toString();
    }





}

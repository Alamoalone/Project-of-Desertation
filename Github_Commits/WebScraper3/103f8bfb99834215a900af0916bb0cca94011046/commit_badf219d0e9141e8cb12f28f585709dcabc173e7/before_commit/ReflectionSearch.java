package me.stephenminer.search;

import me.stephenminer.search.Search;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionSearch implements Search<HashMap<String,Double>> {
    private final WebDriver driver;
    private HashMap<String, Double> reflectionPoints;

    public ReflectionSearch(WebDriver driver){
        this.driver = driver;
        reflectionPoints = new HashMap<>();
    }

    @Override
    public void search() {
        try {
            Thread.sleep(8000);
            WebElement root = driver.findElement(By.xpath("//div[@jscontroller='yidvwe']"));
            String assignmentClass = "qhnNic LBlAUc Aopndd TIunU ZoT1D idtp4e DkDwHe";
            List<WebElement> assignmentStream = root.findElements(By.xpath(".//div[@class='" + assignmentClass + "']")).stream().filter(e -> e.getAttribute("data-stream-item-id") != null).collect(Collectors.toList());
            List<String> ids = new ArrayList<>(assignmentStream.stream().map(assignment -> assignment.getAttribute("data-stream-item-id")).toList());
            System.out.println(ids.size());

            try {
                for (int i = 0; i < ids.size(); i++) {
                    final int index = i;
                    String id = ids.get(index);
                    //Program is grabbing correct ids
                    try {
                        WebElement assignment = root.findElement(By.xpath(".//div[@data-stream-item-id='" + id + "']"));
                        //Should be finding correct element, but seems as if it is grabbing not right element
                        // assignment.findElements(By.xpath(".//div[@class='PazDv']")).forEach(e-> System.out.println(e.getAttribute("aria-label")));
                        WebElement wantClick = assignment.findElements(By.xpath(".//div[@class='PazDv']")).stream().filter(e -> e.getAttribute("aria-label") != null).findFirst().orElse(null);
                        String title = wantClick.getAttribute("aria-label");
                        if (title.contains("Week") && title.contains("Reflection")) {
                            System.out.println(title);
                            //  Thread.sleep(1000); // was 1000
                            assignment.click();
                            //    Thread.sleep(750);
                            gatherEntries();
                            //   Thread.sleep(500); // was 2000
                            updateIds(ids);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Skipping element with data-stream-id of " + id);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        /*
        try {
            for (int i = assignmentStream.size()-1; i >=0;i--) {
                WebElement assignment = assignmentStream.get(i);
                WebElement wantClick = assignment.findElement(By.xpath(".//div[@class='PazDv']"));
                String title = wantClick.getAttribute("aria-label");
                if (title.contains("Week") && title.contains("Reflection")) {
                    wantClick.click();
                    gatherEntries();
                }
                Thread.sleep(3000);
            }
        }catch (Exception e){e.printStackTrace();}
      */
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateIds(List<String> ids){
        WebElement root = driver.findElement(By.xpath("//div[@jscontroller='yidvwe']"));
        String assignmentClass = "qhnNic LBlAUc Aopndd TIunU ZoT1D idtp4e DkDwHe";
        List<WebElement> assignmentStream = root.findElements(By.xpath(".//div[@class='" + assignmentClass + "']")).stream().filter(e -> e.getAttribute("data-stream-item-id") != null).toList();
        List<String> updated = new ArrayList<>(assignmentStream.stream().map(assignment -> assignment.getAttribute("data-stream-item-id")).filter(s->!ids.contains(s)).toList());
        ids.addAll(updated);
    }

    //get elements after some delay
    private void gatherEntries(){

        try {
            //Get elements on page with 'tr'
            Thread.sleep(1000);
            List<WebElement> tables = driver.findElements(By.xpath("//table[@aria-label='Students']"));
            WebElement root;
            if (tables.size() > 1) root = tables.get(tables.size()-1);
            else root = tables.get(0);

            List<WebElement> elements = root.findElements(By.xpath(".//tr")).stream().filter(e -> e.getAttribute("data-student-id") != null).toList();

            for (WebElement element : elements) {
                try {
                    String name = element.findElement(By.xpath(".//span[@class='YVvGBb']")).getAttribute("textContent");
                    boolean check = name.equalsIgnoreCase("CHRISTABEL CAMPBELL");
                    String xPath = ".//span[@class='PazDv']";//".//span[@role='button']";
                    List<WebElement> e2 = element.findElements(By.xpath(xPath));
                    double grade = -1;
                    for (WebElement e : e2) {
                        String gradeText = e.getAttribute("aria-label");
                        System.out.println(gradeText);
                        if (gradeText != null){
                            if (gradeText.equalsIgnoreCase("Add grade")){
                                if (check) System.out.println(10);
                                continue;
                            }
                            String sGrade = gradeText.replace(" Edit grade","");

                            grade = Double.parseDouble(sGrade);
                        }
                    }

                    if (name != null && !name.isEmpty() && grade > -1){
                        updateStudentScore(name,grade);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            driver.findElement(By.xpath("//a[@class='onkcGd OGhwGf cGvavf juzHj']")).click();

        }catch (Exception ignored){}
    }

    private void updateStudentScore(String student, double score){
        double currentScore = reflectionPoints.getOrDefault(student,0d);
        currentScore+=score;
        reflectionPoints.put(student, currentScore);
    }





    @Override
    public HashMap<String, Double> searchData() {
        return reflectionPoints;
    }
}

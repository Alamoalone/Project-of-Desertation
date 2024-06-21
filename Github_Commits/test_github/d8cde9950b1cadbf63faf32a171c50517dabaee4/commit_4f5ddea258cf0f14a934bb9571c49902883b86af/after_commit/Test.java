package geoMetry;

public class Test {

	public static void main(String[] args) {
		
		Coordinate c1 = new Coordinate(2, 4);
		Coordinate c2 = new Coordinate(5, 2);
		Coordinate c3 = new Coordinate(7, 3);
		
		//Circle test
		System.out.println("-----Circle testrun-----");
		Circle c = new Circle(c1, c2);
		
		double radius = c.calculateRadius(c1, c2);
        System.out.println("Der Radius des Kreises beträgt: " + radius);
        
        System.out.println("Die Fläche des Kreises beträgt: " + c.calculateArea());
        System.out.println("Der Umfang des Kreises beträgt: " + c.calculateCircumference());
        
        System.out.println(" ");
        
        //Rectangle test
        System.out.println("-----Rectangle testrun-----");
        Rectangle r = new Rectangle(c1, c2);
        System.out.println("Die Fläche des Rechtecks beträgt: " + r.calculateArea());
        System.out.println("Der Umfang des Rechtecks beträgt: " + r.calculateCircumference());
        
        System.out.println(" ");
        
        //Triangle test
        System.out.println("-----Triangle testrun-----");
        Triangle t = new Triangle(c1, c2, c3);
        System.out.println(t.calculateArea());
        System.out.println(t.calculateCircumference());
        System.out.println(t.calculateSide(0));
        System.out.println(t.calculateSide(1));
        System.out.println(t.calculateSide(2));
        
	}

}

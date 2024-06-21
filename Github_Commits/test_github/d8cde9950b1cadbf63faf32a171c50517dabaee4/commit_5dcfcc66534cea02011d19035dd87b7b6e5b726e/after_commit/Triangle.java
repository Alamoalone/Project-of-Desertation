package geoMetry;

public class Triangle extends Shapes {

	Coordinate point1;
	Coordinate point2;
	Coordinate point3;
	double[] sideLengths;
	
	public Triangle(Coordinate point1, Coordinate point2, Coordinate point3) {
		this.point1 = point1;
	    this.point2 = point2;
	    this.point3 = point3;
	 }
	
	public double calculateSide(int side) {
		switch(side) {
		case 0 : this.sideLengths[0] = Math.sqrt(Math.pow(this.point2.getX() - this.point1.getX(), 2) +
				Math.pow(this.point2.getY() - this.point1.getY(), 2));
				return this.sideLengths[side];
				
		case 1 : this.sideLengths[1] = Math.sqrt(Math.pow(this.point3.getX() - this.point2.getX(), 2) +
				Math.pow(this.point3.getY() - this.point2.getY(), 2));
				return this.sideLengths[side];
				
		case 2 : this.sideLengths[2] = Math.sqrt(Math.pow(this.point1.getX() - this.point3.getX(), 2) +
				Math.pow(this.point1.getY() - this.point3.getY(), 2));
				return this.sideLengths[side];
				
		default : System.err.println("Fehler!"); break;
		}
		return this.sideLengths[side];
		
	}
	
	
	@Override
	public double calculateCircumference() {
		double umfang = 0;
		for(int i = 0; i <= 3; i++) {
			umfang += this.calculateSide(i);
		}
		return umfang;
		}

	@Override
	public double calculateArea() {
		double s = calculateCircumference() / 2;
		double area = Math.sqrt(s * (s - this.calculateSide(0)) * (s - this.calculateSide(1)) * (s - this.calculateSide(2)));
		return area;
		}
}

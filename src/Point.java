// Clasa ce retine coordonatele unui punct
public class Point {
    private byte x;    // Axa Ox
    private byte y;    // Axa Oy
	
    public Point (byte x, byte y) {
        this.x = x;
        this.y = y;
    }
	
    public byte getX () {
        return this.x;
    }
	 
    public void setX (byte x) {
        this.x = x;
    }
	
    public byte getY () {
        return this.y; 
    }
	 
    public void setY (byte y) {
        this.y = y;
    }
    
    public String toString () {
        return "(" + this.getX() + " , " + this.getY() + ")";
    }
}

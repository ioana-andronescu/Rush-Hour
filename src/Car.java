import grandcentral.security.SimpleCrypto;

import java.util.HashMap;

// Directia masinii
enum Direction {
    HORIZONTAL,
    VERTICAL
}

// Clasa ce retine informatii despre o masina
public class Car {
    private Point point;            // Coordonatele masinii
    private Direction direction;    // Directia masinii
    private byte length;            // Lungimea masinii
    private byte colour;            // Culoarea masinii
	
	// Un hash care retine referinte catre toate instantele de Car create
    private static HashMap<String, Car> ALLCARS = new HashMap<String, Car>(); 
	
    private Car (byte x, byte y, Direction direction, byte length, byte colour) {
        this.point     = new Point(x, y);
        this.direction = direction;
        this.length    = length;
        this.colour    = colour;
	}
	
    private Car (Car oldCar, byte newX, byte newY) {
        this.point     = new Point(newX, newY);
        this.direction = oldCar.getCarDirection();
        this.length    = oldCar.getCarLength();
        this.colour    = oldCar.getColour();
    }
	
    // Creaza o noua instanta Car (pentru evitarea aparitiei unor instante duplicate)
    public static Car createNewCar (Car oldCar, byte newX, byte newY) {
        return createNewCar(newX, newY, oldCar.getCarDirection(), oldCar.getCarLength(), oldCar.getColour());	
    }

    public static Car createNewCar (byte x, byte y, Direction direction, byte length, byte colour) {
        // Calculeaza ce valoare hash ar corespunde noii instante de Car
        String newHash = getHashCode(x, y, direction, length, colour);
		
        // Verifica daca exista deja o instanta echivalenta in memorie
        Car newCar = ALLCARS.get(newHash);
			
        // Daca nu, construieste una noua folosind constructorul privat
        if (newCar == null) {
            newCar = new Car(x, y, direction, length, colour);	
            ALLCARS.put(newHash, newCar);	// salvez global o referinta la noua instanta
		}
		
        // Intoarce o referinta la instanta corespunzatoare celor trei parametrii
        return newCar;
    }
	
    // Getter pentru coordonatele masinii
    public Point getCarPoint () {
        return this.point;
    }
	
    // Setter pentru coordonatele masinii
    public void setCarPoint (byte x, byte y) {
        this.point = new Point(x, y);
    }
	
    // Coordonatele din nord ale masinii
    public byte getUpCarPoint () {
        return this.point.getY();
    }
	
    // Coordonatele din sud ale masinii
    public byte getDownCarPoint () {
        byte downPoint = this.point.getY();
        if (this.direction == Direction.VERTICAL)
            downPoint += this.length;
        return downPoint;
    }
	
    // Coordonatele din vest ale masinii
    public byte getLeftCarPoint () {
        return this.point.getX();
    }
	
    // Coordonatele din est ale masinii
    public byte getRightCarPoint () {
        byte rightPoint = this.point.getX();
        if (this.direction == Direction.HORIZONTAL)
            rightPoint += this.length;
        return rightPoint;
    }
	
    // Getter pentru directia masinii
    public Direction getCarDirection () {
        return this.direction;
    }
	
    // Setter pentru directia masinii
    public void setCarDirection (Direction direction) {
        this.direction = direction;
    }
	
    // Getter pentru lungimea masinii
    public byte getCarLength () {
        return this.length;
    }
	
    // Setter pentru lungimea masinii
    public void setCarLength (byte length) {
        this.length = length;
    }
	
    // Getter pentru culoarea masinii
    public byte getColour() {
        return colour;
    }

    // Setter pentru culoarea masinii
    public void setColour(byte colour) {
        this.colour = colour;
    }
	
    // Functia de hash pentru masini
	public static String getHashCode (byte x, byte y, Direction direction, byte length, byte colour) {
        // Seteaza algoritmul folosit
        SimpleCrypto.setAlgorithm("MD5");
        SimpleCrypto.setPath(null);
		
        // Textul clar - lungime directie + lungime + coordonate + culoare
        StringBuilder plaintext = new StringBuilder();
        plaintext.append(direction);
        plaintext.append(length);
        plaintext.append(new Point(x, y).toString());
        plaintext.append(colour);
	
        // Aplica functia asupra textului clar si intoarce textul cifrat
		SimpleCrypto.setText(plaintext.toString());
		return (new SimpleCrypto().generateDigest());
	}
}

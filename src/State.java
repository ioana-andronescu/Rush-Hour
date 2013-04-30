// Clasa pentru stari
public class State implements Comparable<State> {
    private byte[][] board;     // Harta de joc
    private Car[] cars;         // Masinile
    private int pathCost;       // Lungimea caii de la starea initiala la cea curenta
    private State parent;       // Starea precedenta
	
    public State (byte[][] board, Car[] cars, int pathCost, State parent) {
        this.board    = board;
        this.cars     = cars;
        this.pathCost = pathCost;
        this.parent   = parent;
    }
	
    // Getter pentru starea precedenta
    public State getParent(){
        return this.parent;
    }
	
    // Getter pentru masini
    public Car[] getCars() {
        return this.cars;
    }
	
    // Getter pentru harta
    public byte[][] getBoard () {
        return this.board;
    }
	
    // Afla costul estimativ (folosind distanta Manhattan) de la masina rosie pana la iesire
    public int getEstimateCost () {
        Point redCar = cars[0].getCarPoint();
        Point target = RushHour.exit;
        return Math.abs(redCar.getX() - target.getX()) + Math.abs(redCar.getY() - target.getY());
    }
	
    // Getter pentru costul curent
    public int getPathCost () {
        return this.pathCost;
    }

    // Afla daca a ajuns la starea finala
    public boolean isSatisfied () {
        Car redCar = cars[0];
		
        byte exitX = RushHour.exit.getX();
        byte exitY = RushHour.exit.getY();
        byte redCarUp = redCar.getUpCarPoint();
        byte redCarDown = redCar.getDownCarPoint();
        byte redCarLeft = redCar.getLeftCarPoint();
        byte redCarRight = redCar.getRightCarPoint();
		
        if (redCar.getCarDirection() == Direction.VERTICAL) 
            return (redCarUp - 1 == exitY && redCarLeft == exitX) || (redCarDown == exitY && redCarLeft == exitX);
        if (redCar.getCarDirection() == Direction.HORIZONTAL) 
            return (redCarLeft - 1 == exitX && redCarUp == exitY) || (redCarRight == exitX && redCarUp == exitY);
        return false;
    }
	
    // Verifica daca o mutare este valida
    public boolean isValidMove (Car car, byte x, byte y) {	
        if (car.getCarDirection() == Direction.VERTICAL) {
            for (byte i = y; i < y + car.getCarLength(); i++)
                if (board[i][x] != RushHour.SPACE)
                    return false;
        }
		
        if (car.getCarDirection() == Direction.HORIZONTAL) {
            for (byte i = x; i < x + car.getCarLength(); i++)
                if (board[y][i] != RushHour.SPACE)
                    return false;
        }
		
        return true;
    }
	
    // Returneaza o stare ca sir de caractere
    public String toString(){
        return printState();
    }
		
    public String printState () {
        String stringBoard = "";
		
        for (byte i = 0; i < RushHour.height; i++) {
            for (byte j = 0; j < RushHour.width; j++) {
                switch (board[i][j]) {
                    // Daca e margine, pune spatiu dupa '0'
                    case RushHour.BORDER: {
                            if (j < RushHour.width)
                                stringBoard += "0 ";
                            else
                                stringBoard += "0";
                        }
                        break;
				
                    // Daca e iesire, adauga '*'
                    case RushHour.EXIT: {
                            if (j < RushHour.width)
                                stringBoard += "* ";
                            else
                                stringBoard += "*";
                        } 
                        break;
				
                    // Daca e loc gol, pune '-'
                    case RushHour.SPACE: {
                            stringBoard += "- ";
                        }
                        break;
				
                    // Daca e masina rosie, pune '?'
                    case RushHour.RED_CAR: {
                            stringBoard += "? ";
                        }
                        break;

                    // Daca e masina obisnuita, pune caractere in functie de directie
                    default: {
                        for(int k = 1; k < cars.length; k++) {
                            if(board[i][j] == cars[k].getColour()) {
                                Car car = cars[k];
                                Point p = car.getCarPoint();
                                Direction d = car.getCarDirection();

                                // Masina pe directia orizontala
                                if (d == Direction.HORIZONTAL) {
                                    // Margine stanga
                                    if (p.getY() == i && p.getX() == j) {
                                        stringBoard += "< ";
                                        break;
                                    }
                                    else {
                                        // Margine dreapta
                                        if (p.getY() == i && car.getRightCarPoint() - 1 == j) {
                                            stringBoard += "> ";
                                            break;
                                        }
                                    }

									// Interiorul masinii de pe orizontala
                                    if (p.getY() == i && car.getLeftCarPoint() < j && j < car.getRightCarPoint() - 1) {
                                        stringBoard += "- ";
                                        break;
                                    }
                                }
 
                                // Masina pe directia verticala
                                if (d == Direction.VERTICAL) {

                                    // Margine sus
                                    if (p.getY() == i && p.getX() == j) {
                                        stringBoard += "^ ";
                                        break;
                                    }
                                    else {
                                        // Margine jos
                                        if (car.getDownCarPoint() - 1 == i && p.getX() == j) {
                                            stringBoard += "v ";
                                            break;
                                        }
                                    }
 
                                    // Interiorul masinii de pe verticala
                                    if (car.getUpCarPoint() < i && i < car.getDownCarPoint() && p.getX() == j) {
                                        stringBoard += "| ";
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            stringBoard += "\n";
        }
        return stringBoard;		
    }

	@Override
    // Compara o stare cu alta stare (pentru TreeSet-ul din RushHour)
    public int compareTo (State otherState) {
        // Costurile totale ale celor 2 stari, costul curent + costul aproximativ
        int totalCost1 = this.getEstimateCost() + this.getPathCost();
        int totalCost2 = otherState.getEstimateCost() + otherState.getPathCost();
		
        if(totalCost1 != totalCost2)
            return totalCost1 - totalCost2;
		
        byte otherBoard[][] = otherState.getBoard();
		
        // Compara hartile celor 2 stari
        for (byte i = 0; i < RushHour.height; i++)
            for (byte j = 0; j < RushHour.width; j++)
                if (this.board[i][j] != otherBoard[i][j])
                    return this.board[i][j] - otherBoard[i][j];

        return 0;
    }
}

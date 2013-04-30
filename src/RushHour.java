import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

// Clasa principala
public class RushHour {
    private State initialState;    // Starea initiala (harta initiala)
    private State currentState;    // Starea curenta (harta curenta)
	
    private int currentCost;       // Costul curent
	
    private byte[][] board;        // Harta de joc
    private Car[] cars;            // Masinile din joc
    private byte carNum;           // Numarul de masini din joc
	
    public static Point exit;      // Coordonatele iesirii
	
    public static byte width;      // Latimea hartii
	public static byte height;     // Lungimea hartii
	
    public final static byte BORDER  = 66;      // Marginea de pe harta
    public final static byte EXIT    = 65;      // Iesirea de pe harta
    public final static byte SPACE   = -1;      // Locul gol de pe harta
    public final static byte RED_CAR = 0;       // Masina rosie de pe harta
	
    // Getter pentru harta
    public byte[][] getBoard () {
        return this.board;
    }
 
    // Getter pentru masini
    public Car[] getCars () {
        return cars;
    }
	
    // Getter pentru numarul de masini
    public byte getCarNum () {
        return this.carNum;
    }
	
    // Citirea datelor din fisierul de intrare
    public State readFromFile () {
        try {
            Scanner file = new Scanner(new FileReader("in.txt"));
            try {
                height = file.nextByte();
                width  = file.nextByte();
                file.nextLine();
                file.nextLine();
							
                // Memorarea hartii si a masinilor aflate pe verticala, cat si a masinii rosii
                Character[][] initialBoard = new Character[height][width];
                board = new byte[height][width];
                for (byte i = 0; i < height; i++) {
                    String line = file.nextLine();
                    for (byte j = 0; j < width; j++) {
                        initialBoard[i][j] = line.charAt(2 * j);
                        if (i == 0 || i == height - 1 || j == 0 || j == width - 1)
                            board[i][j] = BORDER;
                        else
                            board[i][j] = SPACE;
                    }
                }
				
                byte redCarLen = 0;
                Car redCar = null;
                carNum = 0;
                ArrayList<Car> carList = new ArrayList<Car>();
                for (byte i = 0; i < height; i++) {
                    for (byte j = 0; j < width; j++) {
                        char symbol = initialBoard[i][j];
                        switch (symbol) {
                            case '<': {
                                    carList.add( Car.createNewCar(j, i, Direction.HORIZONTAL, (byte) 0, carNum++));
                                    board[i][j] = carNum;
                                }
                                break;
                            case '>': {
                                    Point p = carList.get(carNum - 1).getCarPoint();
                                    Direction d = carList.get(carNum - 1).getCarDirection();
                                    byte carLen = (byte) (j - p.getX());
                                    Car newCar = Car.createNewCar(p.getX(), p.getY(), d, ++carLen, carNum);
                                    carList.set(carNum - 1, newCar);
                                    for (byte k = newCar.getLeftCarPoint(); k < newCar.getRightCarPoint(); k++)
                                        board[p.getY()][k] = carNum;
                                }
                                break;
                             case '?': {
                                    if (redCarLen == 0) 
                                        redCar = Car.createNewCar(j, i, Direction.HORIZONTAL, (byte) 0, RED_CAR);
                                    else {
                                        Point p = redCar.getCarPoint();
                                        if (p.getX() == j) 
                                            redCar.setCarDirection(Direction.VERTICAL);
                                    }
                                    redCarLen++;
                                    board[i][j] = RED_CAR;
                                }
                                break;
                             case '*': {
                                    exit = new Point(j, i);
                                    board[i][j] = EXIT;
                                }
                                break;
                         }
                    }    
                }
                redCar.setCarLength(redCarLen);
                
                Character[][] transpBoard = new Character[width][height];
                for (byte i = 0; i < height; i++) {
                    for (byte j = 0; j < width; j++) {
                        transpBoard[j][i] = initialBoard[i][j];
                    }
                }
				
                // Memoreaza masinile de pe verticala
                for (byte i = 0; i < width; i++) {
                    for (byte j = 0; j < height; j++) {
                        char symbol = transpBoard[i][j];
                        switch (symbol) {
                            case '^': {
                                    carList.add(Car.createNewCar(i, j, Direction.VERTICAL, (byte) 0, carNum++));
                                    board[j][i] = carNum;
                                }
                                break;
                            case 'v': {
                                    Point p = carList.get(carNum - 1).getCarPoint();
                                    Direction d = carList.get(carNum - 1).getCarDirection();
                                    byte carLen = (byte) (j - p.getY());
                                    Car newCar = Car.createNewCar(p.getX(), p.getY(), d, ++carLen, carNum);
                                    carList.set(carNum - 1, newCar);
                                    for (byte k = newCar.getUpCarPoint(); k < newCar.getDownCarPoint(); k++)
                                        board[k][p.getX()] = carNum;
                                }
                                break;
                         }
                    }    
                }
                
                cars = new Car[this.getCarNum() + 1];
                cars[0] = redCar;
                for (byte i = 0; i < this.getCarNum(); i++)
                    cars[i + 1] = carList.get(i);
				
            } catch (NumberFormatException e) {
                 e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }	
        // Returneaza starea initiala
        return new State(board, cars, 0, null);
    }
	
    // Creaza o noua instanta State si o pune in open
    public void newState (TreeSet<State> open, Car newCar, byte colour) { 
        Car[] cars = currentState.getCars();
        byte[][] board = currentState.getBoard();
		
        byte[][] newBoard = new byte[height][width];
		
        for( byte i = 0; i < height; i++) 
            for ( byte j = 0; j < width; j++) 
                newBoard[i][j] = board[i][j]; 
		
        // Pune masina pe harta	
        markCarOnBoard(newBoard, newCar, newCar.getColour());
		
        Car[] newCars = new Car[getCarNum() + 1];
        System.arraycopy(cars, 0, newCars, 0, getCarNum() + 1);
        newCars[colour] = newCar;
		
        State newState = new State(newBoard, newCars, currentCost, currentState);
		
        open.add(newState);			
    }
	
    // Pune masina pe harta
	public void markCarOnBoard( byte[][] board, Car car, byte colour) {
        if (car.getCarDirection() == Direction.HORIZONTAL) {
            for (byte i = car.getLeftCarPoint(), y = car.getCarPoint().getY(); i < car.getRightCarPoint(); i++)
                board[y][i] = colour;
        }
		
        if (car.getCarDirection() == Direction.VERTICAL) {
            for (byte i = car.getUpCarPoint(), x = car.getCarPoint().getX(); i < car.getDownCarPoint(); i++)
                board[i][x] = colour;
        }
    }
	
    // Sterge masina de pe harta (pune '-' in spatiile goale)
    public void removeCarFromBoard (byte[][] board, Car car) {
        markCarOnBoard(board, car, SPACE);
    }
	
    // Incearca mutarile pe harta
    public void moveCarToBoard (TreeSet<State> open, Car car) {
        byte[][] board = currentState.getBoard();
		
        // Datele curente ale masinii
        byte currentX = car.getCarPoint().getX();
        byte currentY = car.getCarPoint().getY();
        Direction direction = car.getCarDirection();
        byte colour = board[currentY][currentX];
		
        // Sterge masina de pe harta si incearca alte mutari
        removeCarFromBoard(board, car);
		
        // Incearca mutarile pentru masina daca e pe verticala
        if (direction == Direction.VERTICAL) {
            for (byte i = (byte) (car.getUpCarPoint() + 1); i < height; i++) {
                if(!currentState.isValidMove(car, currentX, i))
                    break;
				
                Car newCar = Car.createNewCar(car, currentX, i); 
                this.newState(open, newCar, colour);
            }
			
            for (byte i = (byte) (car.getUpCarPoint() - 1); i > 0; i--) {
                if(!currentState.isValidMove(car, currentX, i))
                    break;

                Car newCar = Car.createNewCar(car, currentX, i); 
                this.newState(open, newCar, colour);
            }
        }
		
        // Incearca mutarile pentru masina daca e pe orizontala
        if (direction == Direction.HORIZONTAL) {
            for (byte i = (byte) (car.getLeftCarPoint() + 1) ; i < width; i++) {
                if(!currentState.isValidMove(car, i, currentY))
                    break;
				
                Car newCar = Car.createNewCar(car, i, currentY); 
                this.newState(open, newCar, colour);
            }
			
            for (byte i = (byte) (car.getLeftCarPoint() - 1); i > 0; i--) {
                if(!currentState.isValidMove(car, i, currentY))
                    break;
				
                Car newCar = Car.createNewCar(car, i, currentY); 
                this.newState(open, newCar, colour);
            }	
        }
 
        // Pune masina la loc (poate ca mutarea sa nu fie valida)
        markCarOnBoard(board, car, car.getColour());
	}
	
    // Expandarea spatiului starilor
	public void expandState (TreeSet<State> open) {
        currentCost = currentState.getPathCost() + 1;
		
        Car[] cars = currentState.getCars();
        
        // Incearca mutari pentru fiecare masina
        for (byte k = 0; k < cars.length; k++) {
            Car currentCar = cars[k];
            moveCarToBoard(open, currentCar);
        }
	}
	
    // Algoritmul de gasire a drumului catre solutie = A*
    public void getSolution () {
        TreeSet<State> closed = new TreeSet<State>();    // Multimea closed
        TreeSet<State> open = new TreeSet<State>();      // Multimea open
		
        open.add(initialState);
		 
        do {
            // Extrage starea cu cel mai mic cost
            State current = open.first(); 
            open.remove(current);
			
            currentState = current;
			
            // Verifica daca a ajuns la solutie
            if (current.isSatisfied()) {
                // Memoreaza drumul de la starea finala catre cea initiala
                State[] states = new State[current.getPathCost() + 1];
			
				// Pleaca de la frunza finala, prin parinte, catre radacina
                int inc = 0;
                State state = current;
                while (state != null) {
                     states[inc++] = state;
                     state = state.getParent();
                }

                StringBuilder buff = new StringBuilder(); 
				
                for (int i = inc - 1; i >= 0; --i) {
                    buff.append(states[i].toString());
                    if (i != 0)
                    buff.append("\n");
                }
				
                printPath(states.length - 1 , buff);
                break;
            }
			
            if (!closed.contains(currentState)) {
                closed.add(current);
                expandState(open);
            }
        } while (!open.isEmpty());
    }
	
    // Afiseaza calea de la starea initiala catre starea finala 
    public void printPath (int length, StringBuilder buff) {
        try {
            BufferedWriter file = new BufferedWriter(new FileWriter("out.txt"));
            file.write(length + "\n\n");
            file.write(buff.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) {
        // long ms = System.currentTimeMillis();
        RushHour rushHour = new RushHour();
        rushHour.initialState = rushHour.readFromFile();
        rushHour.getSolution();
        
        // long diff = System.currentTimeMillis() - ms;
        // System.out.println("Time: " + diff);
    }
} 

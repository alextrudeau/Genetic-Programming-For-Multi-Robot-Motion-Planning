import java.awt.Color;
import java.awt.Point;
import java.util.Random;

public class Main {
	public static void main(String[] args) {
		Random rand = new Random();
        int numBots = 0;
        
        while (numBots < 2) {
        	numBots = rand.nextInt(5);
        	System.out.println(numBots);
        }
	}
	
}

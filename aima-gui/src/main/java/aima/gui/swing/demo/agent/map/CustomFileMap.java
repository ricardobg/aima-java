package aima.gui.swing.demo.agent.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import aima.core.environment.map.ExtendableMap;
import aima.core.util.math.geom.shapes.Point2D;

/**
 * Represents a map existent in a file. The format of the files is as above.
 * <map name>
 * <number of cities>
 * <city_1> <x_1> <y_1>
 * ...
 * <city_n> <x_n> <y_n>
 * <number of routes>
 * <origin_city_1> <destiny_city_1> <distance_1>
 * ...
 * <origin_city_m> <destiny_city_m> <distance_m>
 * 
 */
public class CustomFileMap extends ExtendableMap {
	/**
	 * Load map from file
	 * @param filename the name of the file
	 * @throws FileNotFoundException 
	 */
	public CustomFileMap(String filename) throws FileNotFoundException, NoSuchElementException {
		clear();
		Scanner sc = null;
		try {
			sc = new Scanner(new File(filename));
			mapName = sc.nextLine();
			//Read cities
			int nCities = sc.nextInt();
			citiesNames = new String[nCities];
			for (int i = 0; i < nCities; i++) {
				citiesNames[i] = sc.next();
				double x = sc.nextDouble();
				double y = sc.nextDouble();
				setPosition(citiesNames[i], x, y);
			}
			//Read routes
			int nRoutes = sc.nextInt();
			for (int i = 0; i < nRoutes; i++) {
				String fromCity = sc.next();
				String toCity = sc.next();
				double distance = sc.nextDouble();
				addBidirectionalLink(fromCity, toCity, distance);
			}
		}
		finally {
			if (sc != null)
				sc.close();
		}
		
	}
	
	private String mapName;
	private String[] citiesNames;
	
	/**
	 * Get the name of the loaded map
	 * @return
	 */
	public String getMapName() {
		return mapName;
	}
	
	/**
	 * Change clear to remove cities names and the map name
	 */
	@Override
	public void clear() {
		super.clear();
		mapName = null;
		citiesNames = null;
	}
	
	/**
	 * Main method to test
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//If you want to test locally, change the file name
			CustomFileMap map = new CustomFileMap("/home/ricardo/Git/city_graph/test.txt");
			System.out.println("Loaded map " + map.getMapName());
			System.out.println("Cities:");
			for (String loc : map.getLocations()) {
				Point2D position = map.getPosition(loc);
				System.out.println(loc + " (" + position.getX() + ", " + position.getY() + "):");
				for (String to : map.getPossibleNextLocations(loc)) {
					double dist = map.getDistance(loc, to);
					System.out.println("	" + to + " (" + dist + ")");
				}
				System.out.println();
			}
			
		} catch (FileNotFoundException | NoSuchElementException e) {
			e.printStackTrace();
		}
	}
	
}

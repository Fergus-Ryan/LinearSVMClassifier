package Classifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.List;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileSystemView;

public class LinearSvm4D5 extends JFrame implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1485835507044701454L;
	File selectedFile = null;
    File file = null;
    File testFile = null;
    double[] max;
    int trainSize = 0;
    int testSize = 0;
    int testSetSize;
    String[][][] trainData;
    String[][] testData;
    Container container = getContentPane();

	JButton oneFile = new JButton("One combined file");
	JButton twoFile = new JButton("Two seperate files");
	private int numFiles = 0;
	
	public LinearSvm4D5() {
        super("Machine Learning Assignment 3");
		this.run();
		
	}
	
	//get number of files needed (Single file containing both training and testing data or separate files)
	private class ButtonHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == oneFile) {
				numFiles = 1;
			}else {
				numFiles = 2;
			}
		}
	}
	
	//getter methods
	public File getFile() {
        return this.file;
    }
	
	public File getTestFile() {
        return this.testFile;
    }
	
	//Browse files and choose the required data
	public void chooseFile(int numFiles) throws Exception {
		//File defaultTestFile = new File("C:\\Users\\F\\Documents\\MachineLearning and DataMining\\Assignment3\\owls-1.csv");
	    
		
        JFileChooser choose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        choose.setBorder(new CompoundBorder(new LineBorder(new Color(66,133,244), 5), new EmptyBorder(5, 5, 5, 5)));
        choose.setBackground(Color.WHITE);
        if(numFiles == 2) {
        	choose.setDialogTitle("Choose train data: ");
        }else {
        	choose.setDialogTitle("Seect file containing data ");
        }
		int returnValue = choose.showOpenDialog(null);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            selectedFile = choose.getSelectedFile();
            if(!selectedFile.getPath().substring(selectedFile.getPath().lastIndexOf('.')+1).equals("csv")) {
            	throw new Exception("Error file type must be csv. Please try again.");
            }else {
            	this.file = selectedFile;
            }
            //System.out.println(selectedFile.getAbsolutePath());
        }else {
        	throw new Exception("Error with chosen file. Please try again.");
        }
        
        if(numFiles == 2) {
        	JFileChooser choose2 = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        	choose2.setDialogTitle("Choose test data: ");
        	choose2.setBorder(new CompoundBorder(new LineBorder(new Color(219,68,55), 5), new EmptyBorder(5, 5, 5, 5)));
        	choose2.setBackground(Color.WHITE);
        	int returnValue2 = choose2.showOpenDialog(null);
            if(returnValue2 == JFileChooser.APPROVE_OPTION) {
                testFile = choose2.getSelectedFile();
                if(!testFile.getPath().substring(testFile.getPath().lastIndexOf('.')+1).equals("csv")) {
                	throw new Exception("Error file type must be csv. Please try again.");
                }
            }else{
            	throw new Exception("Error with chosen file. Please try again.");
            }
		}
	}
	
	//Count number of classes and attributes in the file
	public int[] countLinesColumns(File f) {
		int lines = 1;
		int columns = 0;
		//count the number of rows and attributes in the data set
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			
			String[] first = reader.readLine().split(",");
			columns = first.length;
			while (reader.readLine() != null) lines++;
			reader.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		int[] ans = {lines, columns};
		
		return ans;
	}
	//Read data from file
	public String[][] readData(File f){
		ArrayList<String[]> rows = new ArrayList<String[]>();
		
		try {
			//scanner for reading file line by line
			Scanner sc = new Scanner(f);
			
			//read in 150 lines
			while(sc.hasNextLine()) {
				//the 5 "columns" of the ith row of the array now contains the comma-deliminated line split up
				rows.add(sc.nextLine().split(","));
			}
			
			sc.close();
			
			//catch the error if the wrong location or filename is passed in
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
			System.exit(1);
		}
		
		return rows.toArray(new String[0][0]);
	}
	//Count number of classes present in data
	public int countClasses(String[][] rows) {
		Set<String> set = new HashSet<String>();
		int count = 0;
		for(int i = 0; i<rows.length; i++) {
			if(set.add(rows[i][rows[i].length-1])) {
            count++;
			}
		}
		if(!(count>1)) {
			System.out.println("Error: File must include more than one class for classification");
			System.exit(2);
		}
		return count;
	}
	//Split file into individual classes
	public String[][][] splitData(int dataSetSize, int count, int columns, String[][] rows){
		String[][][] data = null;
		//individual train and test size per class
		if(numFiles == 1) {
			this.trainSize = (int)( (((double)dataSetSize/count)/100)*66);
			this.testSize = (dataSetSize/count)-trainSize;
		
			//variable for holding all data split into separate classes
			data = new String[count][trainSize+testSize][columns];
			
			//split the data into their individual classes
			for(int i=0;i<count;i++) {
				for(int j=0;j<dataSetSize/count;j++) {
					data[i][j] = rows[j+(dataSetSize/count)*i];
				}
			}
		}else {
			this.trainSize = dataSetSize/count;
			this.testSize = testSetSize;
			
			data = new String[count][trainSize][columns];
			
			//split the data into their individual classes
			for(int i=0;i<count;i++) {
				for(int j=0;j<trainSize;j++) {
					data[i][j] = rows[j+trainSize*i];
				}
			}
		}
		//shuffle the data to ensure no bias
		for(int i=0;i<data.length;i++) {
			Collections.shuffle(Arrays.asList(data[i]));
		}
		return data;
	}
	//split data into training and testing data
	public String[][] getTestData(String[][][] data, int count, int columns){
		String[][] testData; 
		if(numFiles == 2) {
			testData = new String[this.testSetSize][columns];
			
			try {
				Scanner sc = new Scanner(this.testFile);
				for(int i=0;i<this.testSetSize;i++) {
					testData[i] = sc.nextLine().split(",");
				}
				sc.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		}else {
			testData = new String[this.testSize*count][columns];
			//breaks up the rest of the data into test data for each class type
			int testDataCount = 0;
			for(int j=0;j<count;j++) {
				for(int i=this.trainSize;i<this.trainSize + this.testSize;i++) {
					testData[testDataCount] = data[j][i];
					testDataCount++;
				}
			}
			//Normalize test data
			for(int j=0;j<columns-1;j++) {
					for(int l=0;l<this.testSize*count;l++) {
						testData[l][j] = Double.toString(Double.parseDouble(testData[l][j])/this.max[j]);
					}
			}
		}
		
		return testData;
	}
	//split data into training and testing data
	public String[][][] getTrainData(String[][][] data, int count, int columns){
		String[][][] trainData = new String[count][this.trainSize][columns];
		//breaks up the data into training data for each class type
		for(int i=0;i<this.trainSize;i++) {
			for(int j=0;j<count;j++) {
				trainData[j][i] = data[j][i];
			}
		}
		
		//System.out.println(Arrays.deepToString(trainData));
		
		//get the max of each feature for data normalisation
		this.max = new double[columns-1];
		//Normalize training data
		for(int j=0;j<columns-1;j++) {
			for(int i=0;i<count;i++) {
				for(int l=0;l<this.trainSize;l++) {
					if(Double.parseDouble(trainData[i][l][j]) > this.max[j]) {
						this.max[j] = Double.parseDouble(trainData[i][l][j]);
					}
				}
			}
		}		
		
		//System.out.println(Arrays.toString(max));
		
		for(int j=0;j<columns-1;j++) {
			for(int i=0;i<count;i++) {
				for(int l=0;l<this.trainSize;l++) {
					trainData[i][l][j] = Double.toString(Double.parseDouble(trainData[i][l][j])/this.max[j]);
				}
			}
		}
		return trainData;
	}
	//Get the mean point for each class on each classifier
	public double[][][] getAvgPoints(int numClassifiers, int columns, String[][][] trainData, int count){
		//variables that will store the average points for each of the two data sets (a and b)
				double[][] avgA = new double[numClassifiers][columns-1];
				
				double[][] avgB = new double[numClassifiers][columns-1];
				
				//creating 3 modles, each plots the points of training data belonging to two classes (e.g model[0] plots the training points of 
				//classes 0 and 1)
				for(int i=0;i<this.trainSize;i++) {
					
					for(int j=0;j<numClassifiers;j++) {
						for(int k=0;k<columns-1;k++) {
							avgA[j][k] += Double.parseDouble(trainData[j][i][k]);
										
							avgB[j][k] += Double.parseDouble(trainData[(j+1)%count][i][k]);
						}
					}
				}
				
				
				//average out the coordinates
				for(int i=0;i<numClassifiers;i++) {
					for(int j=0;j<columns-1;j++) {
						avgA[i][j] = avgA[i][j]/this.trainSize;
					
						avgB[i][j] = avgB[i][j]/this.trainSize;
					}
				}
				double[][][] ans = {avgA, avgB};
				return ans;
	}
	//Plot the training data accross all classifiers to create the models required
	public double[][][] getModels(int numClassifiers, int columns, String[][][] trainData, int count){
		double[][][] models = new double[numClassifiers][this.trainSize*2][columns];
		//creating 3 modles, each plots the points of training data belonging to two classes (e.g model[0] plots the training points of 
		//classes 0 and 1)
		for(int i=0;i<this.trainSize;i++) {
			
			for(int j=0;j<numClassifiers;j++) {
				for(int k=0;k<columns-1;k++) {
					models[j][i][k] = Double.parseDouble(trainData[j][i][k]);
								
					models[j][i+this.trainSize][k] = Double.parseDouble(trainData[(j+1)%count][i][k]);
				}
			}
		}
		return models;
	}
	//Find the data points of each class that are closest to the mean point of the opposing class for each classifier
	public double[][][] getClosestPoints( double[][][] models, int numClassifiers, int columns, double[][][] avg){
		//variables to store the closest point of each set to the avg point of the opposing set
				//i.e. closestA[0][1 to 4] will hold the w,x,y,z coordinates for the closest point of the first set to the avg point of the second set
				//and closestA[1][1 to 4] holds the w,x,y,z coordinates for the closest point of the second set to the avg point of the first set
				double[][][] closest = new double[numClassifiers][2][columns-1];
				double[][] distance = new double[numClassifiers][2];
				
				//find the closest points
				for(int j=0;j<numClassifiers;j++) {
					for(int i=0;i<this.trainSize;i++) {
						double valA = 0;
						for (int k=0; k<columns-1; k++) {
							valA += (avg[1][j][k]-models[j][i][k])*(avg[1][j][k]-models[j][i][k]);
						}
						double distA = Math.sqrt(valA);
						
						//if the distance for the current point is greater than the distance for the previous point, the current 
						//point is set to be the current closest point until it is overtaken by a closer point (or not)
						if(distA < distance[j][0] || distance[j][0] == 0) {
							distance[j][0] = distA; 
							for(int k=0;k<columns-1;k++) {
								closest[j][0][k] = models[j][i][k];
							}
						}
						
						double valB = 0;
						for(int k=0;k<columns-1;k++) {
							valB += (avg[0][j][k]-models[j][i+this.trainSize][k])*(avg[0][j][k]-models[j][i+this.trainSize][k]);
						}
						
						//same as above but the opposite direction
						double distB = Math.sqrt(valB);
						
						if(distB < distance[j][1] || distance[j][1] == 0) {
							distance[j][1] = distB;
							for(int k=0;k<columns-1;k++) {
								closest[j][1][k] = models[j][i+this.trainSize][k];
							}
						}
					}
				}
				return closest;
	}
	//Generate the equation for the hyperplane of each classifier
	public double [][] getEquation(int numClassifiers, int columns, double[][][] closest) {
		//variables for holding the midpoint, perpendicular vector, "e", and hyperplane equation for each model.
		double[][] midpoint = new double[numClassifiers][columns-1];
		double[][] perpendicular = new double[numClassifiers][columns-1];
		double[] e = new double[numClassifiers];
		double[][] equation = new double[numClassifiers][columns];
		
		for(int i=0;i<numClassifiers;i++) {
			for(int j=0;j<columns-1;j++) {
				//get the midpoint between the two closest points
				midpoint[i][j] = (closest[i][1][j]+closest[i][0][j])/2;
				
				//get a vector perpendicular to the hyperplane, i.e. one of the closest points minus the other one
				perpendicular[i][j] = (closest[i][0][j]-closest[i][1][j]);
				
				//find the value for e (aw + bx + cy + dz + e = 0)
				e[i] += perpendicular[i][j]*midpoint[i][j];
				
				//this variable stores the parts of the equation (a , b , c , d , e)
				equation[i][j] = perpendicular[i][j];
			}
			 
			equation[i][columns-1] = e[i]*-1;
			
		}
		return equation;
	}
	//Classify the training data (Test the accuracy of the model)
	public int[] testModel( String[][] testData, double[][] equation, int columns, int numClassifiers, String[][][] trainData) throws Exception {
		
		int correct = 0;
		int wrong = 0;
		for(int i=0;i<testData.length;i++) {
			
			double ans =0;
			for(int j=0;j<columns-1;j++) {
				ans += equation[0][j]*Double.parseDouble(testData[i][j]);
			}
			ans += equation[0][columns-1];
			
			/*System.out.println("Equation 1: " + Arrays.toString(equation[0]));
			System.out.println("Test Data : " + Arrays.toString(testData[i]));
			System.out.println("Answer 1: " + ans);*/
			
			
			for(int k=1;k<numClassifiers-1;k++) {
				if(ans > 0) {					
					double ans2 =0;
					for(int j=0;j<columns-1;j++) {
						ans2 += equation[(k+1)%numClassifiers][j]*Double.parseDouble(testData[i][j]);
					}
					ans2 += equation[(k+1)%numClassifiers][columns-1];
					
					if(ans2 > 0) {
						//System.out.println(i + " A Classified as : " + trainData[(k+1)%numClassifiers][0][columns-1] + " \nActual: " + testData[i][columns-1] + "\n");
						
						if(trainData[(k+1)%numClassifiers][0][columns-1].equals(testData[i][columns-1])) { 
							correct++;}
						else wrong++;
						
					}else if(ans2 < 0) {
						//System.out.println(i + " B Classified as : " + trainData[k-1][0][columns-1] + " \nActual: " + testData[i][columns-1] + "\n");
						
						if(trainData[k-1][0][columns-1].equals(testData[i][columns-1])) { 
							correct++;}
						else wrong++;
						
					}else System.out.println(i + " one else");
				}else if(ans < 0) {
					double ans2 =0;
					for(int j=0;j<columns-1;j++) {
						ans2 += equation[k][j]*Double.parseDouble(testData[i][j]);
					}
					ans2 += equation[k][columns-1];
					
					if(ans2 > 0) {
						//System.out.println(i + " C Classified as : " + trainData[(k+1)%numClassifiers][0][columns-1] + " \nActual: " + testData[i][columns-1] + "\n");
						
						if(trainData[(k+1)%numClassifiers][0][columns-1].equals(testData[i][columns-1])) { 
							correct++;}
						else wrong++;
						
					}else if(ans2 < 0) {
						//System.out.println(i + " D Classified as : " + trainData[k][0][columns-1] + " \nActual: " + testData[i][columns-1] + "\n");
						
						if(trainData[k][0][columns-1].equals(testData[i][columns-1])) { 
							correct++;}
						else wrong++;
						
					}else throw new Exception("Data cannot be classified");
				}else throw new Exception("Data cannot be classified");
			}
		}
		int[] ans = {correct, wrong};
		return ans;
	}
	
	//to format long doubles to two decimal points
    static String formatNum(double doubleNum) {
        return String.format("%.2f", doubleNum);
    }
	
	//display results
	public void display(int[][] ans) {
		double avgCorrect = 0;
		double avgWrong = 0;
		for(int i=0;i<10;i++) {
			avgCorrect += ans[i][0];
			avgWrong += ans[i][1];
		}
		avgCorrect = avgCorrect/10;
		avgWrong = avgWrong/10;
		
        Charts s = new Charts(ans, testData.length, String.format("%.2f",avgCorrect/testData.length*100), String.format("%.2f",avgWrong/testData.length*100));
        
        
		
	}
	
	
	public static void main(String[] args) {
		
		LinearSvm4D5 lSvm = new LinearSvm4D5();
		
	}

	//Run the program
	@Override
	public void run() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(dim.width/2,dim.height/2);
		setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		//add icon to application
		BufferedImage[] img = new BufferedImage[7];
		try {
			img[0] = ImageIO.read(new File("src\\Icon\\one.jpg"));
			img[1] = ImageIO.read(new File("src\\Icon\\1.5.jpg"));
			img[2] = ImageIO.read(new File("src\\Icon\\1.7.jpg"));
			img[3] = ImageIO.read(new File("src\\Icon\\two.jpg"));
			img[4] = ImageIO.read(new File("src\\Icon\\2.3.jpg"));
			img[5] = ImageIO.read(new File("src\\Icon\\2.7.jpg"));
			img[6] = ImageIO.read(new File("src\\Icon\\three.jpg"));
			setIconImage(img[0]);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		//add instructions and options to home page
		JLabel instructions = new JLabel("<html><center>Linear SVM Classifier<br/><br/>This allows you to submit either one file that contains "
				+ "a single data set of combined training and test data, or two seperate files, one containing training data"
				+ "and the other containing testing data.<br/><br/>Please select your option below.<br/><br/>" 
				+ "If you select two seperate files, you will first be asked to choose the file containing the train data, "
				+ "you will then be asked to choose the file containing the test data."
				+ "<br/><br/>All classes must contain the same number of attributes and an equal number of training cases</html></center>");
		instructions.setBorder(new EmptyBorder(5,5,5,5));
		instructions.setFont(new Font("Courier", Font.BOLD, 15));
		oneFile.setFont(new Font("Helvetica", Font.BOLD, 15));
		twoFile.setFont(new Font("Helvetica", Font.BOLD, 15));
		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout());
		
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBackground(Color.WHITE);
		buttonPanel.add(oneFile, BorderLayout.WEST);
		buttonPanel.add(twoFile, BorderLayout.EAST);
		buttonPanel.setBorder(new EmptyBorder(5,5,5,5));
		
		container.add(buttonPanel, BorderLayout.SOUTH);
		container.add(instructions, BorderLayout.NORTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ButtonHandler bh = new ButtonHandler();
		oneFile.addActionListener(bh);
		twoFile.addActionListener(bh);
		setVisible(true);
		int imgCount = 1;
		//wait while the user chooses files
		while(numFiles == 0) {
			try {
				setIconImage(img[imgCount]);
				imgCount = (imgCount+1)%7;
				container.revalidate();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		setVisible(false);
		
		try {
			this.chooseFile(numFiles);
		} catch (Exception e1) {
			container.removeAll();
			JLabel error = new JLabel(e1.getMessage());
			error.setBorder(new EmptyBorder(5,5,5,5));
			error.setFont(new Font("Courier", Font.BOLD, 15));
			container.add(error);
            setVisible(true);
            while(true)
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		int[] linesColumns = this.countLinesColumns(this.getFile());
		
		if(numFiles == 2) {
			int[]testLinesColumns = this.countLinesColumns(this.getTestFile());
			this.testSetSize = testLinesColumns[0];
		}
		
		int ans[][] = new int[10][2];
		//Run code ten times to get average
		for(int runNum=0;runNum<10;runNum++){
			int dataSetSize = linesColumns[0];
			int columns = linesColumns[1];
			
			int count = 0;
			if(numFiles == 2) {
				String[][] rows = this.readData(this.getFile());
				count = this.countClasses(rows);
				this.trainData = this.splitData(dataSetSize, count, columns, rows);
				this.testData = this.readData(this.getTestFile());
			}else {
				//2D array for reading in all lines from csv (i.e. 150 rows and 5 columns)
				String[][] rows = this.readData(this.getFile());
				
				//find how many classes are present in the data
				count = this.countClasses(rows);
				
				String[][][] data = data = this.splitData(dataSetSize, count, columns, rows);
			
				//3D array for holding all trainData in one variable
				this.trainData = this.getTrainData(data, count, columns);
				//3d array for holding all testData in one variable
				this.testData = this.getTestData(data, count, columns);
			}
			//shuffle test data
			Collections.shuffle(Arrays.asList(testData));
			
			//get the number of binaryClassifiers required
			int numClassifiers =0;
			for(int i=0;i<count;i++) {
				numClassifiers  += i;
			}
			
			//System.out.println(numClassifiers);
			
			//modelA stores all the points for plotting two sets of the training data on the same graph	
			double[][][] models = this.getModels(numClassifiers, columns, trainData, count);
			
			//variables that will store the average points for each of the two data sets (a and b)
			double[][][] avg = this.getAvgPoints(numClassifiers, columns, trainData, count);
			
			double[][][] closest = this.getClosestPoints(models, numClassifiers, columns, avg);
	
			//System.out.println(Arrays.deepToString(closest));
			//System.out.println(Arrays.deepToString(distance));
			
			double[][] equation = this.getEquation(numClassifiers, columns, closest);
			
			/*for(int i=0; i<equation.length;i++) {
				System.out.println(i + " " + Arrays.toString(equation[i]));
			}*/
			
			try {
				ans[runNum] = this.testModel(testData, equation, columns, numClassifiers, trainData);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//this.display(ans[runNum][0],ans[runNum][1]);
		}
		
		this.display(ans);
	}
		
}


package Classifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
//Create visuals and display results
public class Charts extends JFrame{
	private static final Dimension WindowSize = new Dimension (1030,700);
	private int width;
	private int height;
	static int[][] ans;
	static int testSize;
	static String avgCorrect;
	static String avgWrong;
	
	public Charts() {
		this.setTitle("Resulting Charts");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setBounds(50, 50, WindowSize.width, WindowSize.height);
		setVisible(true);
	}
	
	public Charts(int[][] ans, int testSize, String avgCorrect, String avgWrong) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.width = dim.width/2;
		this.height = dim.height/2;
		this.ans = ans;
		this.testSize = testSize;
		this.avgCorrect = avgCorrect;
		this.avgWrong = avgWrong;
		Charts c = new Charts();
	}
	
	public void paint (Graphics g) {
		int red, blue, green;
		int i, count =0;
		double j; 
		g.setFont(new Font("Courier", Font.BOLD, 20));
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, WindowSize.width, WindowSize.height);
		g.setColor(new Color(137,141,147));
		g.fillRect(0, WindowSize.height-515, 15, 500);
		g.fillRect(0, WindowSize.height-15, WindowSize.width, 15);
		
		
	     
	    g.setColor(Color.BLACK);
		g.drawString("Accuracy results accross ten iterations", 50, 50);
		g.drawString("Each bar represents one run, ranging from 1-10", 50, 80);
		g.drawString("Average Accuracy: " + avgCorrect  + "%", WindowSize.width-300, 50);
		
		Color[] colors = {new Color(66,133,244),new Color(219,68,55),new Color(244,160,0),new Color(15,157,88)};
		
		for(i=0; i<10; i++){
			j = (double)ans[count][0]/testSize*100;
			g.setColor(colors[(0+i)%4]);
			
			g.fillRect(i*100+20, (WindowSize.height-(int)j*5)-15, 90, (int)j*5);
			count++;
			
		}
		
	}

}

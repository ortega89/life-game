package com.ortega.lifegame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Window.Type;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import static com.ortega.lifegame.Constants.*;

public class LifeGame implements Runnable {
	
	private JFrame frame;
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new LifeGame());
	}
	
	public void run()
	{
		BufferedImage emptyCell = new BufferedImage(CELL_WIDTH, CELL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		BufferedImage fullCell = new BufferedImage(CELL_WIDTH, CELL_HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		drawCell(emptyCell, EMPTY_CELL_COLOR, EMPTY_CELL_COLOR.brighter(), EMPTY_CELL_COLOR.darker());
		drawCell(fullCell, FULL_CELL_COLOR, FULL_CELL_COLOR.brighter(), FULL_CELL_COLOR.darker());
				
		frame = new JFrame(makeTitleWithDelay(200));
		frame.setType(Type.UTILITY);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//The LifePanel is my canvas where I draw
		LifePanel panel = new LifePanel(frame, emptyCell, fullCell, CELL_WIDTH, CELL_HEIGHT, SIZE_X, SIZE_Y, new Timer(200, null));
		panel.setBackground(GAP_COLOR);
		
		//My panel is also a universal listener for mouse.
		panel.addMouseListener(panel);
		panel.addMouseMotionListener(panel);
		panel.addMouseWheelListener(panel);
		
		frame.add(panel);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		frame.setVisible(true);
	}
	
	public static String makeTitleWithDelay(int delay) {
		return String.format("Life. LMB draws, RMB runs, wheel sets delay (%d ms)", delay);
	}
	
	/**
	 * Draws a cell on a given image.<br />
	 * This is how we would paint an 8x5 cell:
	 * <pre>
	 * LLLLLLLD
	 * LCCCCCCD
	 * LCCCCCCD
	 * LCCCCCCD
	 * DDDDDDDD
	 * </pre>
	 * @param img An image to draw the cell on.
	 * @param cellColor is the color of C
	 * @param lightBorder is the color of L
	 * @param darkBorder is the color of D
	 */
	private void drawCell(BufferedImage img, Color cellColor, Color lightBorder, Color darkBorder) {
		int cellWidth = img.getWidth();
		int cellHeight = img.getHeight();
		
		Graphics g = img.getGraphics();
		g.setColor(cellColor);
		g.fillRect(1, 1, cellWidth-2, cellHeight-2);
		g.setColor(lightBorder);
		g.drawLine(0, 0, cellWidth-1, 0);
		g.drawLine(0, 0, 0, cellHeight-1);
		g.setColor(darkBorder);
		g.drawLine(cellWidth-1, cellHeight-1, cellWidth-1, 0);
		g.drawLine(cellWidth-1, cellHeight-1, 0, cellHeight-1);
	}
}

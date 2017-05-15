package com.ortega.lifegame;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import static com.ortega.lifegame.Constants.*;

@SuppressWarnings("serial")
public class LifePanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener {

	private JFrame parent;
	
	private BufferedImage empty, full;
	
	private final int cellStepX;
	private final int cellStepY;
	private final int sizeX;
	private final int sizeY;
	
	private Timer timer;
	
	/** It is <code>true</code> when you are dragging mouse with left button pressed. */
	private boolean painting;
	
	/** While painting, it shows what to do: draw (<code>true</code>) or erase (<code>false</code>). */
	private boolean fill;
	
	private boolean[][] field;
	
	/** Neighbor shifts array. When we sum up the point of our cell with one of the shifts, we get the neighbor's point. */
	final Point[] shift = new Point[] {
			new Point(-1,-1), new Point( 0,-1), new Point( 1,-1),
			new Point(-1, 0),  /* Our cell */   new Point( 1, 0),
			new Point(-1, 1), new Point( 0, 1), new Point( 1, 1)
	};
	
	/**
	 * Creates a new panel for Life game. It uses a matrix of <code>boolean</code>s.
	 * @param empty The image for an empty cell.
	 * @param full The image for a full cell.
	 * @param cellWidth The cell width. No need to make it equal to image width.
	 * @param cellHeight The cell height. No need to make it equal to image height.
	 * @param sizeX The field width, in cells.
	 * @param sizeY The field height, in cells.
	 */
	public LifePanel(JFrame parent, BufferedImage empty, BufferedImage full, int cellWidth, int cellHeight, int sizeX, int sizeY, Timer timer) {
		this.parent = parent;
		this.empty = empty;
		this.full = full;
		this.cellStepX = cellWidth+GAP;
		this.cellStepY = cellHeight+GAP;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		
		setPreferredSize(new Dimension(sizeX*this.cellStepX-GAP, sizeY*this.cellStepY-GAP));
		
		field = newField();
		
		this.timer = timer;
		timer.setInitialDelay(0);
		timer.stop();
		timer.addActionListener(this);
	}
	
	/**
	 * Initializes a new empty field of <code>false</code> values (empty cells).
	 * @return A sizeY array of sizeX boolean arrays.
	 */
	private boolean[][] newField() {
		boolean[][] field = new boolean[sizeY][];
		
		for (int i = 0; i < field.length; i++)
			field[i] = new boolean[sizeX];
		
		return field;
	}
	
	/**
	 * Checks if the given coordinates point at the cell within the play field.
	 * @param x The abscissa.
	 * @param y The ordinate.
	 * @return <code>true</code> if the point belongs to the cell. Otherwise, returns <code>false</code>.
	 */
	private boolean isValidPoint(int x, int y) {
		return x >= 0 && y >= 0 && x < sizeX && y < sizeY;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < field.length; i++)
			for (int j = 0; j < field[i].length; j++)
				g.drawImage(field[i][j] ? full : empty, j*cellStepX, i*cellStepY, null);
	}

	private Point getCellByXY(Point pt) {
		return new Point(pt.x / cellStepX, pt.y / cellStepY);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		//When we paint, we should fill each cell we drag the mouse over.
		if (painting) {
			Point cell = getCellByXY(e.getPoint());
			if (!isValidPoint(cell.x, cell.y))
				return;
			
			field[cell.y][cell.x] = fill;
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) { }

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			startPaintingAt(e.getPoint());
			break;
		case MouseEvent.BUTTON3:
			toggleTimer();
			break;
		case MouseEvent.BUTTON2:
			clearField();
			break;
		}
	}
	
	private void startPaintingAt(Point point) {
		Point cell = getCellByXY(point);
		if (!isValidPoint(cell.x, cell.y))
			return;

		fill = !field[cell.y][cell.x];
		field[cell.y][cell.x] = fill;
		painting = true;
		
		repaint();
	}
	
	private void toggleTimer() {
		if (timer != null)
			if (timer.isRunning())
				timer.stop();
			else
				timer.start();
	}
	
	private void clearField() {
		for (boolean[] a : field)
			Arrays.fill(a, false);
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) { 
		painting = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void actionPerformed(ActionEvent e) {
		//A timer action. We need to make a new field based on the previous one.
		boolean[][] oldField = field;
		field = newField();
		
		//Rules:
		//1. The action takes place on a limited field of square cells.
		//2  Each cell can be filled (i. e. have a dweller) or empty.
		//3. Any cell can have up to 8 neighbors, i. e. dwellers placed nearby.
		//4. If a dweller has less than 2 neighbors, it dies due to loneliness.
		//5. If a dweller has more than 3 neighbors, it dies due to the crush.
		//6. If an empty cell has exactly 3 neighbors, a new dweller appears there.
		
		for (int i = 0; i < sizeY; i++)
			for (int j = 0; j < sizeX; j++) {
				int neighbors = getNeighborsCount(oldField, j, i);
				field[i][j] = oldField[i][j];
				if (oldField[i][j] && (neighbors < 2 || neighbors > 3))
					field[i][j] = false;
				else if (!oldField[i][j] && neighbors == 3)
					field[i][j] = true;
			}
		
		repaint();
	}
			
	/**
	 * Calculates the number of neighbors for the given cell.
	 * @param field The play field.
	 * @param x Abscissa of the desired cell.
	 * @param y Its ordinate.
	 * @return The number of neighbors.
	 */
	private int getNeighborsCount(boolean[][] field, int x, int y) {
		int neigh = 0;
		for (int i = 0; i < shift.length; i++)
			if (isValidPoint(y+shift[i].y, x+shift[i].x) && field[y+shift[i].y][x+shift[i].x])
				neigh++;
		return neigh;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (timer != null) {
			//wheel rotation is negative when the wheel goes up
			int units = -e.getWheelRotation();
			int delay = timer.getDelay();
			int step = units > 0 ? 1 : -1;
		
			do {
				delay = limitValue(delay + step * DELAY_STEP, MIN_DELAY, MAX_DELAY);
				units -= step;
			} while (units != 0);
			
			timer.setDelay(delay);
			parent.setTitle(LifeGame.makeTitleWithDelay(delay));
		}
	}
	
	private int limitValue(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
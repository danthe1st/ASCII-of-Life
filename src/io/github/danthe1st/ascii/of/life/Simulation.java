package io.github.danthe1st.ascii.of.life;

import java.util.Random;

public class Simulation {
	private boolean[][] currentCells;
	private boolean[][] tmpCells;
	
	public Simulation(int width,int height) {
		currentCells=new boolean[height][width];
		tmpCells=new boolean[height][width];
	}
	
	public void nextRound() {
		for (int y = 0; y < currentCells.length; y++) {
			for (int x = 0; x < currentCells[y].length; x++) {
				int aliveCount=0;
				for (int y1 = y-1; y1 <= y+1; y1++) {
					for (int x1 = x-1; x1 <= x+1; x1++) {
						if (y==y1&&x==x1) {
							continue;
						}
						int otherCellX=y1%currentCells.length;
						int otherCellY=x1%currentCells[0].length;
						if (y1<0) {
							otherCellX+=currentCells.length;
						}
						if (x1<0) {
							otherCellY+=currentCells[0].length;
						}
						if (currentCells[otherCellX][otherCellY]) {
							aliveCount++;
						}
						
					}
				}
				
				if (aliveCount==2) {
					tmpCells[y][x]=currentCells[y][x];
				}
				else if (aliveCount==3) {
					tmpCells[y][x]=true;
				}
				else {
					tmpCells[y][x]=false;
				}
			}
		}
		swapArrays();
	}
	private void swapArrays() {
		boolean[][] tmp=currentCells;
		currentCells=tmpCells;
		tmpCells=tmp;
	}
	public boolean isAlive(int x,int y) {
		return currentCells[y][x];
	}
	public void changeCell(int x,int y) {
		currentCells[y][x]=!currentCells[y][x];
	}
	public int getWidth() {
		return currentCells[0].length;
	}
	public int getHeight() {
		return currentCells.length;
	}
	public void randomize(Random rand) {
		for (int i = 0; i < currentCells.length; i++) {
			for (int j = 0; j < currentCells[i].length; j++) {
				currentCells[i][j]=rand.nextBoolean();
			}
		}
	}

	public void clear() {
		currentCells=new boolean[currentCells.length][currentCells[0].length];
	}

	public void setCell(int x, int y, boolean alive) {
		currentCells[y][x]=alive;
	}
}

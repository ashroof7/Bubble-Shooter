package com.bubbleshooter;

import java.util.LinkedList;
import java.util.Queue;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoop extends Thread {
	boolean isRunning;
	SurfaceHolder sHolder;
	MainGame gamePanel;
	Queue<Integer> q;
	
	int speedX = 1;
	int speedY = 1;
	
	static final int[] dc = {0,0,1,-1,1,1,-1,-1}; 
	static final int[] dr = {1,-1,0,0,1,-1,1,-1}; 
	static int[] neighborROWS;
	static int[] neighborCOLS;
	static boolean[][] visited;
	static int neighborsCount;
	
	
	public GameLoop(MainGame game)
	{
		super();
		sHolder = game.getHolder();
		gamePanel = game;
		q = new LinkedList<Integer>();
//		neighborCOLS = new int[gamePanel.map.length* gamePanel.map[0].length];
//		neighborROWS = new int[gamePanel.map.length* gamePanel.map[0].length];
//		visited=  new boolean[gamePanel.map.length][gamePanel.map[0].length];
	}

	@SuppressLint("WrongCall")
	@Override
	public void run()
	{
		Canvas canvas;
		Log.v("MainThread", "starting the main thread");
		while (isRunning)
		{
			canvas = null;

			try
			{
				canvas = sHolder.lockCanvas();
				synchronized (sHolder)
				{
					// update game state
//					updateGame();
					// draws the canvas on the panel
					gamePanel.onDraw(canvas);
				}
			} finally
			{
				if (canvas != null)
				{
					sHolder.unlockCanvasAndPost(canvas);
				}
			}

		}
	}
/*
	void fireBullet()
	{
		// TODO not sure about this dimensions
		gamePanel.bullet.x = gamePanel.displayDims.x/2;
		gamePanel.bullet.y = MainGame.footerHeight;
	}

	void updateGame()
	{
		if (gamePanel.bullet.x != -1)
		{
			// update bullet position
			int newX = gamePanel.bullet.x + speedX;
			int newY = gamePanel.bullet.y + speedY;
			
			// get bullet index in the map grid 
			int row = ( gamePanel.bullet.x + MainGame.DIAM/2 ) / MainGame.DIAM;
			int col = ( gamePanel.bullet.y + MainGame.DIAM/2 ) / MainGame.DIAM;
			int newRow = (newX + MainGame.DIAM/2) / MainGame.DIAM;
			int newCol = (newY + MainGame.DIAM/2) / MainGame.DIAM;
			
			// check for collision with board borders
			if (newX > gamePanel.displayDims.x || newX < 0 || newY < 0)
			{
				if(newY < 0) // reached the border of the board so will stop the bullet
				{
					// stop bullet
					gamePanel.map[row][col] = gamePanel.bulletColor;
					gamePanel.bullet.x = -1;
					gamePanel.bullet.y = -1;
				}
				else
					speedX = -speedX; // collision with horizontal border
			} else
			{
				if (gamePanel.map[newRow][newCol] != -1)
				{
					// bullet should be stopped
					gamePanel.map[row][col] = gamePanel.bulletColor;
					
					// flood fill
					// TODO change this queue and create your one to avoid allocation
					for(int i = 0 ; i < visited.length;i++)
						for(int j = 0 ; j < visited[0].length;j++)
							visited[i][j] = false;
					q.add(row);
					q.add(col);
					int color = gamePanel.bulletColor;
					neighborROWS[0] = row;
					neighborCOLS[0] = col;
					visited[row][col] = true;
					neighborsCount = 1;
					while(!q.isEmpty())
					{
						int frontR = q.poll();
						int frontC = q.poll();
						for(int i = 0; i < dc.length;i++)
						{
							int neighborC = frontC + dc[i]; 
							int neighborR = frontR + dr[i];
							if(neighborC > 0 && neighborC < MainGame.COLS && neighborR > 0 && neighborR < MainGame.baseRow && gamePanel.map[neighborR][neighborC] == color && !visited[neighborR][neighborC])
							{
								neighborCOLS[neighborsCount] = neighborC;
								neighborROWS[neighborsCount++] = neighborR;
								visited[neighborR][neighborC] = true;
								q.add(neighborR);
								q.add(neighborC);
							}
						}
					}
					if(neighborsCount > 3)
						for(int i = 0 ;i  < neighborsCount;i++)
							gamePanel.map[neighborROWS[i]][neighborCOLS[i]] = -1;
					// check for disconnected bullet
					for(int i = 0 ; i < visited.length;i++)
						for(int j = 0 ; j < visited[0].length;j++)
							visited[i][j] = false;
					for(int r = MainGame.baseRow ; r < visited.length;r++)
						for(int c = 0 ; c < visited[0].length;c++)
							if(gamePanel.map[r][c] != -1 &&  !visited[r][c])
							{
								q.add(r);
								q.add(c);
								if(r > 0) // disconnected component
									gamePanel.map[r][c] = -1;
								else
									visited[r][c] = true; // first connected component that is near the ceiling
								while(!q.isEmpty())
								{
									int frontR = q.poll();
									int frontC = q.poll();
									for(int i = 0; i < dc.length;i++)
									{
										int neighborC = frontC + dc[i]; 
										int neighborR = frontR + dr[i];
										if(neighborC > 0 && neighborC < MainGame.COLS && neighborR > 0 && neighborR < MainGame.baseRow && gamePanel.map[neighborR][neighborC] != -1 && !visited[neighborR][neighborC])
										{
											if(r > 0)
												gamePanel.map[neighborR][neighborC] = -1;
											else
												visited[neighborR][neighborC] = true;
											q.add(neighborR);
											q.add(neighborC);
										}
									}
								}
							}
				}else{
					gamePanel.bullet.x = newX;
					gamePanel.bullet.y = newY;
				}
			}
		}
	}
*/
}

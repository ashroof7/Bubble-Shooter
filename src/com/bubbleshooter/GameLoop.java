package com.bubbleshooter;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.SurfaceHolder;


public class GameLoop extends Thread {
	boolean isRunning;
	SurfaceHolder sHolder;
	MainGame gamePanel;
	MyQueue q;

	int speedX = 1;
	int speedY = 1;

	static final int[] dc = { 0, 0, 1, -1, 1, 1, -1, -1 };
	static final int[] dr = { 1, -1, 0, 0, 1, -1, 1, -1 };
	static int[] neighborROWS;
	static int[] neighborCOLS;
	static boolean[][] visited;
	static int neighborsCount;

	SoundPool soundPool;
	static int hitSoundID;
	static int scoreSoundID;
	static final int SHIFTDOWN = 900;
	static int Downperiod = SHIFTDOWN;

	public GameLoop(MainGame game) {
		super();
		sHolder = game.getHolder();
		gamePanel = game;
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		hitSoundID = soundPool.load(gamePanel.getContext(), R.raw.ballhit, 1);
		scoreSoundID = soundPool.load(gamePanel.getContext(), R.raw.score, 1);
	}

	void initGame() {
		q = new MyQueue(gamePanel.map.length * gamePanel.map[0].length);
		neighborCOLS = new int[gamePanel.map.length * gamePanel.map[0].length];
		neighborROWS = new int[gamePanel.map.length * gamePanel.map[0].length];
		visited = new boolean[gamePanel.map.length][gamePanel.map[0].length];
	}

	@SuppressLint("WrongCall")
	@Override
	public void run() {
		Canvas canvas;
		Log.v("MainThread", "starting the main thread");
		boolean win = false;
		
		while (isRunning)
		{
			canvas = null;

			try {
				canvas = sHolder.lockCanvas();
				synchronized (sHolder) {
					// update game state
					win = updateGame();
					isRunning = !win;
					// draws the canvas on the panel
					gamePanel.onDraw(canvas);
//					sHolder.unlockCanvasAndPost(canvas);
					
					if (isRunning && --Downperiod < 0)
					{
						// check base row if there's any ball he loses
						for (int i = 0; i < gamePanel.map[0].length; i++)
							if (gamePanel.map[MainGame.baseRow][i] != -1)
							{
								win = isRunning = false;
								break;
							}
						if (isRunning)
						{
							MainGame.baseRow--;
							MainGame.shiftMargin += MainGame.DIAM;
							Downperiod = SHIFTDOWN;
						}
					}
				}
			} finally {
				if (canvas != null) {
					sHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
		System.out.println("Player = Win/Lose  = "+ win);
	}

	boolean updateGame()
	{
		if (gamePanel.isfired)
		{
			// update bullet position
			int newX = gamePanel.bulletLoc.x + speedX;
			int newY = gamePanel.bulletLoc.y + speedY;

			// get bullet index in the map grid
			int row = (gamePanel.bulletLoc.y + MainGame.DIAM / 2 - MainGame.shiftMargin)
					/ MainGame.DIAM;
			int col = (gamePanel.bulletLoc.x + MainGame.DIAM / 2)
					/ MainGame.DIAM;

			// check for collision with board borders
			if (newX + MainGame.DIAM > gamePanel.displayDims.x || newX < 0
					|| newY < MainGame.shiftMargin)
			{
				if (newY < MainGame.shiftMargin) // reached the border of the
													// board so will stop
				// the bullet
				{
					// stop bullet
					gamePanel.map[row][col] = gamePanel.bulletColor;
					gamePanel.bulletLoc.x = gamePanel.bulletInitLoc.x;
					gamePanel.bulletLoc.y = gamePanel.bulletInitLoc.y;
					gamePanel.isfired = false;
					gamePanel.bulletColor = (int) (Math.random() * MainGame.supportedColors);
					soundPool.play(hitSoundID, 1, 1, 1, 0, 1f);
				} else
					speedX = -speedX; // collision with horizontal border
			} else {
				int centerY = (newY + MainGame.DIAM / 2);
				int centerX = (newX + MainGame.DIAM / 2);
				int centerR = (centerY - MainGame.shiftMargin) / MainGame.DIAM;// ball
																				// center
																				// row/column
				int centerC = centerX / MainGame.DIAM;
				boolean collision = false;
				for (int i = 0; i < dr.length; i++) {
					int nextCellR = centerR + dr[i];
					int nextCellC = centerC + dc[i];
					if (nextCellR >= 0 && nextCellR < gamePanel.map.length
							&& nextCellC >= 0
							&& nextCellC < gamePanel.map[0].length
							&& gamePanel.map[nextCellR][nextCellC] != -1) {
						// check two balls intersection
						int nextCellCenterX = nextCellC * (MainGame.DIAM)
								+ MainGame.DIAM / 2;
						int nextCellCenterY = nextCellR * (MainGame.DIAM)
								+ MainGame.DIAM / 2 + MainGame.shiftMargin;
						int dist2 = (nextCellCenterX - centerX)
								* (nextCellCenterX - centerX)
								+ (nextCellCenterY - centerY)
								* (nextCellCenterY - centerY);
						if (dist2 <= MainGame.DIAM * MainGame.DIAM) {
							collision = true;
							break;
						}
					}
				}
				if (collision) {
					// bullet should be stopped
					int color = gamePanel.map[row][col] = gamePanel.bulletColor;
					gamePanel.getNextBubble();
					gamePanel.isfired = false;
					gamePanel.bulletColor = (int) (Math.random() * MainGame.supportedColors);
					soundPool.play(hitSoundID, 1, 1, 1, 0, 1f);
					// flood fill
					// change this queue and create your own to avoid
					// allocation
					for (int i = 0; i < visited.length; i++)
						for (int j = 0; j < visited[0].length; j++)
							visited[i][j] = false;
					q.add(row);
					q.add(col);
					neighborROWS[0] = row;
					neighborCOLS[0] = col;
					visited[row][col] = true;
					neighborsCount = 1;
					while (!q.isEmpty()) {
						int frontR = q.poll();
						int frontC = q.poll();
						for (int i = 0; i < dc.length; i++) {
							int neighborC = frontC + dc[i];
							int neighborR = frontR + dr[i];
							if (neighborC >= 0
									&& neighborC < MainGame.COLS
									&& neighborR >= 0
									&& neighborR < gamePanel.map.length
									&& gamePanel.map[neighborR][neighborC] == color
									&& !visited[neighborR][neighborC]) {
								neighborCOLS[neighborsCount] = neighborC;
								neighborROWS[neighborsCount++] = neighborR;
								visited[neighborR][neighborC] = true;
								q.add(neighborR);
								q.add(neighborC);
							}
						}
					}
					if (neighborsCount >= 3) {
						MainGame.score+= neighborsCount-2;
						soundPool.play(scoreSoundID, 1, 1, 1, 0, 1f);
						int lastFalling = 0;
						for (int i = 0; i < neighborsCount; i++)
						{
							gamePanel.map[neighborROWS[i]][neighborCOLS[i]] = -1;
							while(gamePanel.fallingBallsX[lastFalling] >= 0)
								lastFalling++;
							gamePanel.fallingBallsX[lastFalling] = neighborCOLS[i]*MainGame.DIAM;
							gamePanel.fallingBallsY[lastFalling] = neighborROWS[i]*MainGame.DIAM + MainGame.shiftMargin;
						}
						// check for disconnected bullet
						for (int i = 0; i < visited.length; i++)
							for (int j = 0; j < visited[0].length; j++)
								visited[i][j] = false;
						for (int r = 0; r <= MainGame.baseRow; r++)
							for (int c = 0; c < visited[0].length; c++)
								if (gamePanel.map[r][c] != -1 && !visited[r][c]) {
									q.add(r);
									q.add(c);
									if (r > 0) // disconnected component
										gamePanel.map[r][c] = -1;
									else
										visited[r][c] = true; // first connected
																// component
																// that is near
																// the ceiling
									while (!q.isEmpty()) {
										int frontR = q.poll();
										int frontC = q.poll();
										for (int i = 0; i < dc.length; i++) {
											int neighborC = frontC + dc[i];
											int neighborR = frontR + dr[i];
											if (neighborC >= 0
													&& neighborC < MainGame.COLS
													&& neighborR >= 0
													&& neighborR < gamePanel.map.length
													&& gamePanel.map[neighborR][neighborC] != -1
													&& !visited[neighborR][neighborC]) {
												if (r > 0)
													gamePanel.map[neighborR][neighborC] = -1;
												else
													visited[neighborR][neighborC] = true;
												q.add(neighborR);
												q.add(neighborC);
											}
										}
									}
								}
						boolean won = true;
						for (int r = 0; r <= MainGame.baseRow && won; r++)
							for (int c = 0; c < gamePanel.map[0].length && won; c++)
								if (gamePanel.map[r][c] != -1)
									won = false;
						return won;
					}
				} else {
					gamePanel.bulletLoc.x = newX;
					gamePanel.bulletLoc.y = newY;
				}
			}
		}
		return false;
	}
}
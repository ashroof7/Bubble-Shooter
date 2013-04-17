package com.bubbleshooter;

import java.util.Arrays;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainGame extends SurfaceView implements SurfaceHolder.Callback {

	GameLoop mainLoopThread;
	Point displayDims;
	static int ROWS = 15;
	static int COLS = 10;
	static int baseRow = 0; // the matching row with the footer
	static int maxShownRows;
	static int startEmptyRows ;
	static int footerRatio = 15 ;//15% or screen height
	static int startEmptyRatio = 40; //40% of the screen height
	
	static int footerHeight ;
	static int DIAM = 65; //bubble diameter  
	Bitmap redBitmap;
	Bitmap bubblesResized; 
	
	int map[][];
	
	
	static final int ORANGE = 0;
	static final int RED = 1;
	static final int PINK = 2;
	static final int PURPLE = 3;
	static final int GREEN = 4;
	
	static final int supportedColors = 5;
	
	void initGame(int rows){
		DIAM = displayDims.x/COLS;
		footerHeight = (int) (displayDims.y*footerRatio/100.0);
		maxShownRows = displayDims.y - footerHeight ;
		startEmptyRows  = (int) (displayDims.y*startEmptyRatio/100.0);
		
		Random random = new Random();
		
		map = new int [rows][COLS];
		for (int i = 0; i < startEmptyRows ; i++) 
			Arrays.fill(map[i], -1);
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				map[i][j] = random.nextInt(supportedColors); 
			}
		}
	
	}
	
	public MainGame(Context context) {
		super(context);
		mainLoopThread = new GameLoop(this);
		getHolder().addCallback(this);
		setFocusable(true);
		
		redBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.red);
		//FIXME
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		displayDims = new Point(metrics.widthPixels, metrics.heightPixels);
		
		bubblesResized = Bitmap.createScaledBitmap(redBitmap, DIAM, DIAM, false);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mainLoopThread.isRunning = true;
		mainLoopThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		// mainLoopThread.isRunning = false;
		while (retry) {
			try {
				mainLoopThread.join();
				retry = false;
			} catch (InterruptedException e) {

			}

		}

	}

	
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < ROWS; i++) 
			for (int j = 0; j < COLS -(i&1); j++){
				if (map[i][j] == -1)
					continue ;
				canvas.drawBitmap(bubblesResized,j*DIAM+((i&1)==1?DIAM/2:0), i*(DIAM-5),null);
				
			} 
			
		
	}

}

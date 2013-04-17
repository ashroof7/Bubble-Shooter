package com.bubbleshooter;

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
	static int ROWS = 7;
	static int COLS = 10;
	static int DIAM = 65; //bubble diameter  
	Bitmap redBitmap;
	Bitmap bubblesResized; 
	
	public MainGame(Context context) {
		super(context);
		mainLoopThread = new GameLoop(this);
		getHolder().addCallback(this);
		setFocusable(true);
		
		redBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.red);
		//FIXME
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		displayDims = new Point(metrics.widthPixels, metrics.heightPixels);
		DIAM = displayDims.x/COLS;
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

	static final int RED = 1;
	static final int PINK = 2;
	static final int PURPLE = 3;
	static final int GREEN = 4;
	static final int ORANGE = 5;
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		for (int i = 0; i < ROWS; i++) 
			for (int j = 0; j < COLS -(i&1); j++) 
				canvas.drawBitmap(bubblesResized,j*DIAM+((i&1)==1?DIAM/2:0), i*(DIAM-5),null);
			
		
	}

}

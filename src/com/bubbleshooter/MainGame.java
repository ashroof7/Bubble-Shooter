package com.bubbleshooter;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class MainGame extends SurfaceView implements SurfaceHolder.Callback {

	GameLoop mainLoopThread;
	Point displayDims;
	static int ROWS = 15;
	static int COLS = 10;
	static int baseRow = 0; // the matching row with the footer
	static int startEmptyRows ;
	static int footerRatio = 20 ;//15% or screen height
	static int startEmptyRatio = 40; //40% of the screen height
	
	static int footerHeight ;
	static int gunWidth = 200;
	static int drawOffset ; // upper end of footer
	static int DIAM = 65; //bubble diameter
	
	Point bulletLoc;
	Point bulletInitLoc;
	int bulletColor;
	Bitmap redBitmap;
	Bitmap bubblesResized; 
	
	int map[][];
	
	static final int supportedColors = 5;
	
	static final int ORANGE = 0;
	static final int RED = 1;
	static final int PINK = 2;
	static final int PURPLE = 3;
	static final int GREEN = 4;
	Bitmap[] bubbles = new Bitmap[supportedColors]; // scaled bubbles
	Bitmap[] rawBubbles = new Bitmap[supportedColors]; // colored bubbles just read from files 
	
	
	
	// 0 is the next to be fired 
	int nextBubbleColor[] = new int[3];
	Point nextBubbleLoc[] = new Point[3];
	
	//FIXME profile the performance for the background image
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	void initGame(int rows){
		
		// adjusting vars
		ROWS = rows;
		DIAM = displayDims.x/COLS;
		footerHeight = (int) (displayDims.y*footerRatio/100.0);
		drawOffset = displayDims.y - footerHeight ;
		startEmptyRows  = (int) (displayDims.y*startEmptyRatio/100.0/DIAM);
		
		int sdk = android.os.Build.VERSION.SDK_INT;
		if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			this.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_1));
		} else {
		    this.setBackground(getResources().getDrawable(R.drawable.background_1));
		}
		
		// filling the map 
		map = new int [rows][COLS];
		baseRow = rows-1; 
		for (int i = rows -startEmptyRows; i < ROWS ; i++){ 
			Arrays.fill(map[i], -1);
		}
		for (int i = 0; i < rows-startEmptyRows; i++) 
			for (int j = 0; j < map[0].length; j++) 
				map[i][j] =(int) (Math.random()*supportedColors); 
		
		// adjust location and color of the 3 next bubbles to shoot
		int gap = displayDims.x/40;
		int y = drawOffset + (footerHeight-DIAM)/2;
		int x = (displayDims.x-gunWidth)/2 - 3*gap;
		for (int i = 0; i < nextBubbleColor.length; i++) {
			nextBubbleColor[i] = (int) (Math.random()*supportedColors);
			nextBubbleLoc[i] = new Point(x - i*(DIAM+gap), y);
		}
		
		// initial bullet bubble location
		bulletInitLoc 	= new Point((displayDims.x - DIAM)/2, y);
		bulletLoc 		= new Point((displayDims.x - DIAM)/2, y);
		bulletColor =  (int) (Math.random()*supportedColors);
		mainLoopThread.initGame();
	}
	
	public MainGame(Context context) {
		super(context);
		mainLoopThread = new GameLoop(this);
		getHolder().addCallback(this);
		setFocusable(true);
		
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		displayDims = new Point(metrics.widthPixels, metrics.heightPixels);
		initGame(15);
		
		//reading raw images
		rawBubbles[ORANGE] 	= BitmapFactory.decodeResource(getResources(),R.drawable.orange);
		rawBubbles[RED] 	= BitmapFactory.decodeResource(getResources(),R.drawable.red);
		rawBubbles[PINK] 	= BitmapFactory.decodeResource(getResources(),R.drawable.pink);
		rawBubbles[PURPLE] 	= BitmapFactory.decodeResource(getResources(),R.drawable.purple);
		rawBubbles[GREEN]	= BitmapFactory.decodeResource(getResources(),R.drawable.green);
		
		// scaled images 
		for (int i = 0; i < supportedColors; i++) 
			bubbles[i] = Bitmap.createScaledBitmap(rawBubbles[i], DIAM, DIAM, false);
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
		mainLoopThread.isRunning = false ;
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
	
	
	boolean isfired = false ;
	int v = 25; //firing velocity pixel/frame
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			//touch down
			if (isfired || event.getY() > bulletInitLoc.y)
				return false;
			
			isfired = true;
			int xChange = (int) (event.getX() - bulletInitLoc.x);
			int yChange = (int) (event.getY() - bulletInitLoc.y);
			// time is the measured in FPS (number of game loop executions)
			int time = (int) (Math.sqrt(xChange*xChange + yChange*yChange)/v);
 
			mainLoopThread.speedX = xChange/time;
			mainLoopThread.speedY = yChange/time;
			bulletLoc.x =  bulletInitLoc.x + mainLoopThread.speedX;
			bulletLoc.y =  bulletInitLoc.y + mainLoopThread.speedY;
			return true ;
			
		}
		else if (event.getAction() == MotionEvent.ACTION_UP){
			//touch released;
		}
		
		
		return false;
	}	
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		
		//Draw the grid
		for (int i = baseRow; i >= 0 &&  drawOffset- (baseRow-i)*DIAM > -DIAM; i--) 
			for (int j = 0; j < COLS -(i&1); j++)
				if (map[i][j] == -1)
					continue ;
				else 
					canvas.drawBitmap(bubbles[map[i][j]],j*DIAM+((i&1)==1?DIAM/2:0), drawOffset -(baseRow-i-1)*(DIAM-5),  null);
		
		// Draw the next to shoot bubbles
		for (int i = 0; i < nextBubbleColor.length; i++) 
			canvas.drawBitmap(bubbles[nextBubbleColor[i]], nextBubbleLoc[i].x, nextBubbleLoc[i].y, null);
		
		// Draw the bullet bubble
		canvas.drawBitmap(bubbles[bulletColor], bulletLoc.x,bulletLoc.y, null);
	}

}

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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class MainGame extends SurfaceView implements SurfaceHolder.Callback {

	GameLoop mainLoopThread;
	Point displayDims;
	static int score;
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
	static int shiftMargin;
	
	
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
	
	
	boolean isfired = false ;
	int v = 25; //firing velocity pixel/frame
	boolean firstDraw = true;
	
	// 0 is the next to be fired 
	int nextBubbleColor[] = new int[3];
	Point nextBubbleLoc[] = new Point[3];
	int nextBubble;
	
	
	//FIXME profile the performance for the background image
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	void initGame(int rows){
		// adjusting vars
		mainLoopThread = new GameLoop(this);
		setFocusable(true);
		
		score = 0;
		ROWS = rows;
		DIAM = displayDims.x/COLS;
		footerHeight = (int) (displayDims.y*footerRatio/100.0);
		drawOffset = displayDims.y - footerHeight ;
		startEmptyRows  = (int) (displayDims.y*startEmptyRatio/100.0/DIAM);
		shiftMargin = 0;
		
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
				map[i][j] =(int) (i%supportedColors); 
//				map[i][j] =(int) (Math.random()*supportedColors); 
		
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
		nextBubble = 0;
		mainLoopThread.initGame();
	}
	
	public MainGame(Context context) {
		super(context);
        getHolder().addCallback(this);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		displayDims = new Point(metrics.widthPixels, metrics.heightPixels);
		initGame(15);
		resizeBitmaps();
	}
	
    private void resizeBitmaps(){
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
    
	void getNextBubble()
	{
		bulletLoc.x = bulletInitLoc.x; 
		bulletLoc.y = bulletInitLoc.y; 
		bulletColor = nextBubbleColor[nextBubble];
		nextBubbleColor[nextBubble] = (int) (Math.random()*supportedColors);
		nextBubble  = (nextBubble+1)%nextBubbleColor.length;
//		String s  = "";
//		for(int  i = 0 ; i < map.length;i++ , s+= "\n")
//			for(int  j = 0 ; j < map[0].length;j++)
//				s+= map[i][j]+" ";
//		System.out.println(s);
//		s = "";
//		for(int i = 0 ; i < nextBubbleColor.length;i++)
//			s+=nextBubbleColor[(i+nextBubble)%nextBubbleColor.length]+" ";
//		System.out.println("Next "+s);
	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

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
		if(firstDraw)
		{
			firstDraw = false;
			return ;
		}
		
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		
		//Draw the grid
		for (int i = baseRow; i >= 0 ; i--) 
			for (int j = 0; j < COLS ; j++)
				if (map[i][j] == -1)
					continue ;
				else 
					canvas.drawBitmap(bubbles[map[i][j]],j*DIAM, i*DIAM+shiftMargin,  null);
		
		// Draw the next to shoot bubbles
		for (int i = 0; i < nextBubbleColor.length; i++) 
			canvas.drawBitmap(bubbles[nextBubbleColor[(i+nextBubble)%nextBubbleColor.length]], nextBubbleLoc[i].x, nextBubbleLoc[i].y, null);
		
		// Draw the bullet bubble
		canvas.drawBitmap(bubbles[bulletColor], bulletLoc.x,bulletLoc.y, null);
	}

}

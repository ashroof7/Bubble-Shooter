package com.bubbleshooter;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GameLoop extends Thread {
	boolean isRunning;
	SurfaceHolder sHolder;
	MainGame gamePanel;

	public GameLoop(MainGame game) {
		super();
		sHolder = game.getHolder();
		gamePanel = game;
	}

	@SuppressLint("WrongCall")
	@Override
	public void run() {
		Canvas canvas;
		Log.v("MainThread", "starting the main thread");
		while (isRunning) {
			canvas = null;

			try {
				canvas = sHolder.lockCanvas();
				synchronized (sHolder) {
					// update game state
					// draws the canvas on the panel
					gamePanel.onDraw(canvas);
				}
			} finally {
				if (canvas != null) {
					sHolder.unlockCanvasAndPost(canvas);
				}
			}

		}
	}
}

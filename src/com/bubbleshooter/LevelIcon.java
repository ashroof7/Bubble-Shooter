package com.bubbleshooter;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.StackView;
import android.widget.TextView;

public class LevelIcon extends FrameLayout {
	
	public LevelIcon(Context context, AttributeSet attrs, int levelNo) {
		super(context, attrs);

//		setGravity(Gravity.CENTER_VERTICAL);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.level_icon_options, this, true);

		TextView level = (TextView) getChildAt(1);
		level.setText(levelNo + "");
	}
}

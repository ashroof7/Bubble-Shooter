package com.bubbleshooter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class LevelChooserActivity extends Activity {
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// to make it Fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		
		setContentView(R.layout.activity_level_chooser);
		
		GridView levels = (GridView) findViewById(R.id.grid_levels);
		levels.setAdapter(new LevelAdapter(this));
		
		final Intent intent = new Intent(LevelChooserActivity.this , GameActivity.class);
		levels.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	//TODO start a level with number 
		    	startActivity(intent);
	        }
	    });
		
		
		
	}
}

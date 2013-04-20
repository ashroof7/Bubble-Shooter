package com.bubbleshooter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class LevelAdapter extends BaseAdapter {

	Context context ;
	LevelIcon levels[] = new LevelIcon[MainGame.LEVELS];
	
	public LevelAdapter(Context c) {
        context = c;
	}	
	
	@Override
	public int getCount() {
		return levels.length;
	}

	@Override
	public Object getItem(int pos) {
		return levels[pos];
	}

	@Override
	public long getItemId(int pos) {
		return 0;//deafult
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            levels[position]= new LevelIcon(context, null,position+1); 
//            levels[position].setLayoutParams(new GridView.LayoutParams(85, 85));
            levels[position].setPadding(8, 8, 8, 8);
        } else {
            levels[position] = (LevelIcon) convertView;
        }
        return levels[position];
    }

	
}

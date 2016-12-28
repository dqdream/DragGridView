package com.dq.puzzle;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
/**
 * @author dq
 */
public class DragAdapter extends BaseAdapter implements DragGridBaseAdapter{
	private List<BitBean> list;
	private LayoutInflater mInflater;
	private int mHidePosition = -1;
	
	public DragAdapter(Context context, List<BitBean> list){
		this.list = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 由于复用convertView导致某些item消失了，所以这里不复用item，
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.grid_item, null);
		ImageView mImageView = (ImageView) convertView.findViewById(R.id.item_image);
		mImageView.setImageBitmap(list.get(position).getBitmap());
		if(position == mHidePosition){
			convertView.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}
	
    
	@Override
	public void reorderItems(int oldPosition, int newPosition) {
		Collections.swap(list, oldPosition, newPosition);
	}

	@Override
	public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition; 
		notifyDataSetChanged();
	}


}

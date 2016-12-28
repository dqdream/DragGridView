package com.dq.puzzle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author dq
 */
public class MainActivity extends Activity {
	private List<BitBean> dataSourceList = new ArrayList<BitBean>();
	DragGridView mDragGridView;
	DragAdapter mDragAdapter;
	Button button1, button2;
	private TextView text_timer;
	private int Level = 3;
	private int time;
	private Timer timer;
	private boolean isStart=false;
	private ImageView image;
	int width ;
	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			handler.sendEmptyMessage(1);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDragGridView = (DragGridView) findViewById(R.id.dragGridView);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		image = (ImageView) findViewById(R.id.image);
		text_timer = (TextView) findViewById(R.id.text_timer);
		mDragGridView.setTouch(false);
		Level=getSharedPreferences("level", Activity.MODE_PRIVATE).getInt("level", 3);
		WindowManager wm = this.getWindowManager();
	    width = wm.getDefaultDisplay().getWidth();
		initBitmaps();
		
		mDragAdapter = new DragAdapter(this, dataSourceList);
		mDragGridView.setAdapter(mDragAdapter);
		
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (timer == null&&!isStart) {
					isStart=true;
					initBitmaps();
					mDragAdapter.notifyDataSetChanged();
					Toast.makeText(MainActivity.this,"3秒准备" , 1000).show();
					handler.sendEmptyMessageDelayed(0, 3000);
				}
			}

		});
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				stop();
			}
		});
	}
	private void initBitmaps() {
		dataSourceList.clear();
		mDragGridView.setNumColumns(Level);
		Bitmap bitmap = null;
		switch (Level) {
		case 3:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.a);
			break;
		case 4:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.b);
			break;
		case 5:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.c);
			break;
		case 6:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.d);
			break;
		case 7:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.e);
			break;
		}
		if (bitmap!=null) {
			image.setImageBitmap(bitmap);
			dataSourceList.addAll(getbitmap(bitmap,
					Level, Level, width - 44, width - 44));
		}
	}
	
	private void stop() {
		isStart=false;
		mDragGridView.setTouch(false);
		mDragGridView.onStopDrag();
		if (timer!=null) {
			timer.cancel();
			timerTask.cancel();
			timer = null; 
			timerTask = new TimerTask() {

				@Override
				public void run() {
					handler.sendEmptyMessage(1);
				}
			};
		}
		dataSourceList.clear();
		mDragGridView.setNumColumns(Level);
		Bitmap bitmap = null;
		switch (Level) {
		case 3:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.a);
			break;
		case 4:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.b);
			break;
		case 5:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.c);
			break;
		case 6:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.d);
			break;
		case 7:
			bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.e);
			break;
		}
		if (bitmap!=null) {
			image.setImageBitmap(bitmap);
			dataSourceList.addAll(getbitmap(bitmap,
					Level, Level, width - 44, width - 44));
		}
		mDragAdapter.notifyDataSetChanged();
	}
	Handler handler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (isStart) {
					Toast.makeText(MainActivity.this,"go" , 1000).show();
					Collections.shuffle(dataSourceList);
//					Collections.swap(dataSourceList, 0, 1);
					mDragAdapter.notifyDataSetChanged();
					mDragGridView.setTouch(true);
					switch (Level) {
					case 3:
						time = 150;
						break;
					case 4:
						time = 180;
						break;
					case 5:
						time = 210;
						break;
					case 6:
						time = 240;
						break;
					case 7:
						time = 270;
						break;
					}
					timer = new Timer();
					timer.schedule(timerTask, 1000, 1000);
				}
				break;
			case 1:
				if (isStop()) {// 完成
					if (Level==7) {
						Toast.makeText(MainActivity.this, "真牛逼，这都通关了", 1000).show();
					}else
					    Toast.makeText(MainActivity.this, "完成", 1000).show();
					if (Level<7) {
						Level++;
						getSharedPreferences("level", Activity.MODE_PRIVATE).edit().putInt("level", Level).commit();
					}
					stop();
					return;
				}
				if (time == 0) {// 时间到
					Toast.makeText(MainActivity.this, "未完成", 1000).show();
					stop();
					return;
				}
				time--;
				String t = "";
				if (time % 60 < 10) {
					t = time / 60 + ":0" + time % 60;
				} else {
					t = time / 60 + ":" + time % 60;
				}
				text_timer.setText(t);
				break;
			}
		};
	};

	public List<BitBean> getbitmap(Bitmap mb, int xLevel, int yLevel, int w,
			int h) {
		Bitmap mbitmap = Bitmap.createScaledBitmap(mb, w, h, false);
		List<BitBean> beans = new ArrayList<BitBean>();
		int postion = -1;
		for (int i = 0; i < yLevel; i++) {
			for (int j = 0; j < xLevel; j++) {
				BitBean bean = new BitBean();
				postion++;
				Bitmap bitmap = Bitmap.createBitmap(mbitmap,
						j * (mbitmap.getWidth() / xLevel),
						i * (mbitmap.getHeight() / yLevel), mbitmap.getWidth()
								/ xLevel, mbitmap.getHeight() / yLevel);
				bean.setPostion(postion);
				bean.setBitmap(bitmap);
				beans.add(bean);
			}
		}
		if (!mbitmap.isRecycled()) {
			mbitmap.recycle();
		}
		return beans;
	}

	private boolean isStop() {
		boolean isStop = true;
		for (int i = 0; i < dataSourceList.size(); i++) {
			if (dataSourceList.get(i).getPostion() != i) {
				isStop = false;
			}
		}
		return isStop;
	}
}

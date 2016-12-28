package com.dq.puzzle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * @author dq
 */
public class DragGridView extends GridView {
	/**
	 * DragGridView��item������Ӧ��ʱ�䣬 Ĭ����1000���룬Ҳ������������
	 */
	private long dragResponseMS = 300;
	private boolean isTouch = false;

	public boolean isTouch() {
		return isTouch;
	}

	public void setTouch(boolean isTouch) {
		this.isTouch = isTouch;
	}

	/**
	 * �Ƿ������ק��Ĭ�ϲ�����
	 */
	private boolean isDrag = false;

	private int mDownX;
	private int mDownY;
	private int moveX;
	private int moveY;
	/**
	 * ������ק��position
	 */
	private int mDragPosition;

	/**
	 * �տ�ʼ��ק��item��Ӧ��View
	 */
	private View mStartDragItemView = null;

	/**
	 * ������ק�ľ�������ֱ����һ��ImageView
	 */
	private ImageView mDragImageView;

	/**
	 * ����
	 */
	private Vibrator mVibrator;

	private WindowManager mWindowManager;
	/**
	 * item����Ĳ��ֲ���
	 */
	private WindowManager.LayoutParams mWindowLayoutParams;

	/**
	 * ������ק��item��Ӧ��Bitmap
	 */
	private Bitmap mDragBitmap;

	/**
	 * ���µĵ㵽����item���ϱ�Ե�ľ���
	 */
	private int mPoint2ItemTop;

	/**
	 * ���µĵ㵽����item�����Ե�ľ���
	 */
	private int mPoint2ItemLeft;

	/**
	 * DragGridView������Ļ������ƫ����
	 */
	private int mOffset2Top;

	/**
	 * DragGridView������Ļ��ߵ�ƫ����
	 */
	private int mOffset2Left;

	/**
	 * ״̬���ĸ߶�
	 */
	private int mStatusHeight;

	/**
	 * DragGridView�Զ����¹����ı߽�ֵ
	 */
	private int mDownScrollBorder;

	/**
	 * DragGridView�Զ����Ϲ����ı߽�ֵ
	 */
	private int mUpScrollBorder;

	/**
	 * DragGridView�Զ��������ٶ�
	 */
	private static final int speed = 20;

	private boolean mAnimationEnd = true;

	private DragGridBaseAdapter mDragAdapter;
	private int mNumColumns;

	public DragGridView(Context context) {
		this(context, null);
	}

	public DragGridView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		mStatusHeight = getStatusHeight(context); // ��ȡ״̬���ĸ߶�
	}

	private Handler mHandler = new Handler();

	// ���������Ƿ�Ϊ������Runnable
	private Runnable mLongClickRunnable = new Runnable() {

		@Override
		public void run() {
			isDrag = true; // ���ÿ�����ק
			mVibrator.vibrate(50); // ��һ��
			mStartDragItemView.setVisibility(View.INVISIBLE);// ���ظ�item

			// �������ǰ��µĵ���ʾitem����
			createDragImage(mDragBitmap, mDownX, mDownY);
		}
	};

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);

		if (adapter instanceof DragGridBaseAdapter) {
			mDragAdapter = (DragGridBaseAdapter) adapter;
		} else {
			throw new IllegalStateException(
					"the adapter must be implements DragGridAdapter");
		}
	}

	@Override
	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
		this.mNumColumns = numColumns;
	}

	/**
	 * ������Ӧ��ק�ĺ�������Ĭ����1000����
	 * 
	 * @param dragResponseMS
	 */
	public void setDragResponseMS(long dragResponseMS) {
		this.dragResponseMS = dragResponseMS;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isTouch) {
			return false;
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) ev.getX();
			mDownY = (int) ev.getY();

			// ���ݰ��µ�X,Y�����ȡ�����item��position
			mDragPosition = pointToPosition(mDownX, mDownY);
			tPosition = mDragPosition;
			if (mDragPosition == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}

			// ʹ��Handler�ӳ�dragResponseMSִ��mLongClickRunnable
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);

			// ����position��ȡ��item����Ӧ��View
			mStartDragItemView = getChildAt(mDragPosition
					- getFirstVisiblePosition());

			mPoint2ItemTop = mDownY - mStartDragItemView.getTop();
			mPoint2ItemLeft = mDownX - mStartDragItemView.getLeft();

			mOffset2Top = (int) (ev.getRawY() - mDownY);
			mOffset2Left = (int) (ev.getRawX() - mDownX);

			// ��ȡDragGridView�Զ����Ϲ�����ƫ������С�����ֵ��DragGridView���¹���
			mDownScrollBorder = getHeight() / 5;
			// ��ȡDragGridView�Զ����¹�����ƫ�������������ֵ��DragGridView���Ϲ���
			mUpScrollBorder = getHeight() * 4 / 5;

			// ����mDragItemView��ͼ����
			mStartDragItemView.setDrawingCacheEnabled(true);
			// ��ȡmDragItemView�ڻ����е�Bitmap����
			mDragBitmap = Bitmap.createBitmap(mStartDragItemView
					.getDrawingCache());
			// ��һ���ܹؼ����ͷŻ�ͼ���棬��������ظ��ľ���
			mStartDragItemView.destroyDrawingCache();

			break;
		case MotionEvent.ACTION_MOVE:
			int moveX = (int) ev.getX();
			int moveY = (int) ev.getY();

			// ��������ڰ��µ�item�����ƶ���ֻҪ������item�ı߽����ǾͲ��Ƴ�mRunnable
			if (!isTouchInItem(mStartDragItemView, moveX, moveY)) {
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			break;
		case MotionEvent.ACTION_UP:
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * �Ƿ�����GridView��item����
	 * 
	 * @param itemView
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isTouchInItem(View dragView, int x, int y) {
		if (dragView == null) {
			return false;
		}
		int leftOffset = dragView.getLeft();
		int topOffset = dragView.getTop();
		if (x < leftOffset || x > leftOffset + dragView.getWidth()) {
			return false;
		}

		if (y < topOffset || y > topOffset + dragView.getHeight()) {
			return false;
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isDrag && mDragImageView != null) {
			switch (ev.getAction()) {
			case MotionEvent.ACTION_MOVE:
				moveX = (int) ev.getX();
				moveY = (int) ev.getY();

				// �϶�item
				onDragItem(moveX, moveY);
				break;
			case MotionEvent.ACTION_UP:
				onStopDrag();
				isDrag = false;
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * �����϶��ľ���
	 * 
	 * @param bitmap
	 * @param downX
	 *            ���µĵ���Ը��ؼ���X����
	 * @param downY
	 *            ���µĵ���Ը��ؼ���X����
	 */
	private void createDragImage(Bitmap bitmap, int downX, int downY) {
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; // ͼƬ֮��������ط�͸��
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight;
		mWindowLayoutParams.alpha = 0.55f; // ͸����
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

		mDragImageView = new ImageView(getContext());
		mDragImageView.setImageBitmap(bitmap);
		mWindowManager.addView(mDragImageView, mWindowLayoutParams);
	}

	/**
	 * �ӽ��������ƶ��϶�����
	 */
	private void removeDragImage() {
		if (mDragImageView != null) {
			mWindowManager.removeView(mDragImageView);
			mDragImageView = null;
		}
	}

	/**
	 * �϶�item��������ʵ����item�����λ�ø��£�item���໥�����Լ�GridView�����й���
	 * 
	 * @param x
	 * @param y
	 */
	private void onDragItem(int moveX, int moveY) {
		mWindowLayoutParams.x = moveX - mPoint2ItemLeft + mOffset2Left;
		mWindowLayoutParams.y = moveY - mPoint2ItemTop + mOffset2Top
				- mStatusHeight;
		mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); // ���¾����λ��
		onSwapItem(moveX, moveY);

		// GridView�Զ�����
		mHandler.post(mScrollRunnable);
	}

	/**
	 * ��moveY��ֵ�������Ϲ����ı߽�ֵ������GridView�Զ����Ϲ��� ��moveY��ֵС�����¹����ı߽�ֵ������GridView�Զ����¹���
	 * ���򲻽��й���
	 */
	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			int scrollY;
			if (getFirstVisiblePosition() == 0
					|| getLastVisiblePosition() == getCount() - 1) {
				mHandler.removeCallbacks(mScrollRunnable);
			}

			if (moveY > mUpScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else if (moveY < mDownScrollBorder) {
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 25);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
			}

			smoothScrollBy(scrollY, 10);
		}
	};
	/**
	 * ���Ƶ�ǰλ�ý�������
	 */
	int tPosition = -10;
	long time = 0;

	/**
	 * ����item,���ҿ���item֮�����ʾ������Ч��
	 * 
	 * @param moveX
	 * @param moveY
	 */
	private void onSwapItem(int moveX, int moveY) {
		// ��ȡ������ָ�ƶ������Ǹ�item��position
		final int tempPosition = pointToPosition(moveX, moveY);
		// ����tempPosition �ı��˲���tempPosition������-1,����н���
		if (tempPosition != mDragPosition
				&& tempPosition != AdapterView.INVALID_POSITION
				&& mAnimationEnd) {
			if (tPosition != tempPosition) {
				tPosition = tempPosition;
				time = System.currentTimeMillis();
			}
			// if (!isSwap) {
			// isSwap=true;
			// }
			if (System.currentTimeMillis() - time <= 200) {
				return;
			}
			// isSwap=false;
			mDragAdapter.reorderItems(mDragPosition, tempPosition);
			mDragAdapter.setHideItem(tempPosition);
			onStopDrag();
//			final ViewTreeObserver observer = getViewTreeObserver();
//			observer.addOnPreDrawListener(new OnPreDrawListener() {
//
//				@Override
//				public boolean onPreDraw() {
//					observer.removeOnPreDrawListener(this);
//					// animateReorder(mDragPosition, tempPosition);
//					mDragPosition = tempPosition;
//					return true;
//				}
//			});

		}
	}

	/**
	 * �����ƶ�����
	 * 
	 * @param view
	 * @param startX
	 * @param endX
	 * @param startY
	 * @param endY
	 * @return
	 */
	private AnimatorSet createTranslationAnimations(View view, float startX,
			float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX",
				startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY",
				startY, endY);
		ObjectAnimator animA = ObjectAnimator.ofFloat(view, "alpha", 1.0f,
				0.3f, 1.0F);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY, animA);
		return animSetXY;
	}

	/**
	 * item�Ľ�������Ч��
	 * 
	 * @param oldPosition
	 * @param newPosition
	 */
	private void animateReorder(final int oldPosition, final int newPosition) {
		View view = getChildAt(newPosition);
		int x = 0, y = 0;
		if ((newPosition) % mNumColumns >= (oldPosition) % mNumColumns) {
			x = view.getWidth()
					* ((newPosition) % mNumColumns - (oldPosition)
							% mNumColumns);
		}
		if ((newPosition) / mNumColumns >= (oldPosition) / mNumColumns) {
			y = view.getHeight()
					* ((newPosition) / mNumColumns - (oldPosition)
							/ mNumColumns);
		}
		AnimatorSet resultSet = createTranslationAnimations(view, 0, x, 0, y);
		resultSet.setDuration(3000);
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAnimationEnd = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimationEnd = true;
			}
		});
		resultSet.start();
	}

	/**
	 * ֹͣ��ק���ǽ�֮ǰ���ص�item��ʾ���������������Ƴ�
	 */
	public void onStopDrag() {
		View view = getChildAt(mDragPosition - getFirstVisiblePosition());
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		mDragAdapter.setHideItem(-1);
		removeDragImage();
	}

	/**
	 * ��ȡ״̬���ĸ߶�
	 * 
	 * @param context
	 * @return
	 */
	private static int getStatusHeight(Context context) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		((Activity) context).getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass
						.getField("status_bar_height").get(localObject)
						.toString());
				statusHeight = context.getResources().getDimensionPixelSize(i5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

}

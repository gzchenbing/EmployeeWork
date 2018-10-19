package com.kmnfsw.work.repair.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.GridView;
/**
 * 高度自适应GridView
 * @author YanFaBu
 *
 */
public class AdaptationHightGridView extends GridView {

	public AdaptationHightGridView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AdaptationHightGridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public AdaptationHightGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public AdaptationHightGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightSpec; // 这几行代码比较重要
		if (getLayoutParams().height == AbsListView.LayoutParams.WRAP_CONTENT) {
			heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		} else {
			heightSpec = heightMeasureSpec;

		}
		super.onMeasure(widthMeasureSpec, heightSpec);

	}

}

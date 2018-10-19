package com.kmnfsw.work.repair.view;

import com.kmnfsw.work.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
/**
 * 导航类型选择弹出框
 * @author YanFaBu
 *
 */
public class NavigationDialog extends Dialog{

	private static final String Tag = ".repair.view.NavigationDialog";
	private Context context;
	private RelativeLayout driver;
	private RelativeLayout ride;
	private RelativeLayout gd_minimap;
	
	public NavigationDialog(Context context) {
		super(context);
		this.context = context;
		initView();
	}
	public NavigationDialog(Context context, int theme) {
		super(context,theme);
		this.context = context;
		
		initView();
	}
	
	private void initView(){
		setContentView(R.layout.navigation_dialog);
		driver = (RelativeLayout)findViewById(R.id.driver);
		ride = (RelativeLayout)findViewById(R.id.ride);
		gd_minimap = (RelativeLayout)findViewById(R.id.gd_minimap);
	}
	
	public void setDriverNavigationListener(View.OnClickListener listener){
		driver.setOnClickListener(listener);
	}
	public void setRideNavigationListener(View.OnClickListener listener){
		ride.setOnClickListener(listener);
	}
	public void setMinimapNavigationListener(View.OnClickListener listener){
		gd_minimap.setOnClickListener(listener);
	}
	

}

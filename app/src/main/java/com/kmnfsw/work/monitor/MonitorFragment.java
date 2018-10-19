package com.kmnfsw.work.monitor;

import com.kmnfsw.work.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MonitorFragment extends Fragment{
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.monitor_fragment, null);
		return view;

	}
}

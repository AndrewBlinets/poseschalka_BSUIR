package com.urban.basicsample.adapter;

import java.util.ArrayList;

import com.urban.basicsample.R;
import com.urban.basicsample.model.Attendance;
import com.urban.basicsample.model.Lesson;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DetailsAdapter extends BaseAdapter {

	Context ctx;
	LayoutInflater lInflater;
	ArrayList<Attendance> objects;
	
	public DetailsAdapter(Context context, ArrayList<Attendance> attendance) {
		ctx = context;
		objects = attendance;
		lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return objects.size();
	}

	@Override
	public Object getItem(int position) {
		return objects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = lInflater.inflate(R.layout.list_item2, parent, false);
		}
		
		Attendance at = getAttendance(position);
		
		TextView tv1 = (TextView) view.findViewById(R.id.listName);
		TextView tv2 = (TextView) view.findViewById(R.id.listGroup1);
		TextView tv3 = (TextView) view.findViewById(R.id.listAtt);
		tv1.setText(at.getFirstName() + " " + at.getLastName());
		tv2.setText(at.getGroup());
		if (at.getAt1() == 1 && at.getAt2() == 1) {
			tv3.setText("Обе части");
			view.setBackgroundColor(Color.GREEN);
		} else if (at.getAt1() == 1 && at.getAt2() == 0) {
			tv3.setText("Первая часть");
			view.setBackgroundColor(Color.YELLOW);
		} else if (at.getAt1() == 0 && at.getAt2() == 1) {
			tv3.setText("Вторая часть");
			view.setBackgroundColor(Color.YELLOW);
		} else {
			tv3.setText("Ошибка");
		}
		
		return view;
	}
	
	Attendance getAttendance(int pos) {
		return (Attendance) getItem(pos);
	}

}

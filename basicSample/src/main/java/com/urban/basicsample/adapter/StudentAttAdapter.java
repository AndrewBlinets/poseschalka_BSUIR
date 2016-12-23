package com.urban.basicsample.adapter;

import java.util.ArrayList;

import com.urban.basicsample.MyFileClass;
import com.urban.basicsample.R;
import com.urban.basicsample.model.Attendance;
import com.urban.basicsample.model.Lesson;
import com.urban.basicsample.model.Student;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StudentAttAdapter extends BaseAdapter {

	Context ctx;
	LayoutInflater lInflater;
	ArrayList<Attendance> objects;

	private static final String Tag = "MyLog";
	public MyFileClass fileClass =  new MyFileClass();


	public StudentAttAdapter(Context context, ArrayList<Attendance> attendances) {
		fileClass.writeFile( this.getClass().getName() + "   StudentAttAdapter  ");
		ctx = context;
		objects = attendances;
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
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		fileClass.writeFile( this.getClass().getName() + "   getView");
		View view = convertView;
		if (view == null) {
			view = lInflater.inflate(R.layout.list_item3, parent, false);
		}
		
		Attendance l = getAttendance(position);
		
		TextView tv1 = (TextView) view.findViewById(R.id.listSubjectName);
		TextView tv2 = (TextView) view.findViewById(R.id.listDate3);
		TextView tv3 = (TextView) view.findViewById(R.id.listAtt3);
		tv1.setText(l.getSubjectName());
		tv2.setText(l.getDate());
		if (l.getAt1() == 1 && l.getAt2() == 1) {
			tv3.setText("Обе части");
			view.setBackgroundColor(Color.GREEN);
		} else if (l.getAt1() == 1 && l.getAt2() == 0) {
			tv3.setText("Первая часть");
			view.setBackgroundColor(Color.YELLOW);
		} else if (l.getAt1() == 0 && l.getAt2() == 1) {
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

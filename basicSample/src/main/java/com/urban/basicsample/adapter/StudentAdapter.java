package com.urban.basicsample.adapter;

import java.util.ArrayList;

import com.urban.basicsample.R;
import com.urban.basicsample.model.Lesson;
import com.urban.basicsample.model.Student;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StudentAdapter extends BaseAdapter {

	Context ctx;
	LayoutInflater lInflater;
	ArrayList<Student> objects;

	public StudentAdapter(Context context, ArrayList<Student> student) {
		ctx = context;
		objects = student;
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
		return objects.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = lInflater.inflate(R.layout.list_item_student, parent, false);
		}
		
		Student l = getStudent(position);
		
		TextView tv1 = (TextView) view.findViewById(R.id.listFirstName);
		TextView tv2 = (TextView) view.findViewById(R.id.listLastName);
		TextView tv3 = (TextView) view.findViewById(R.id.listStudentGroup);
		tv1.setText(l.getFirstName());
		tv2.setText(l.getLastName());
		tv3.setText(l.getGroup());
		
		return view;
	}
	
	Student getStudent(int pos) {
		return (Student) getItem(pos);
	}

}

package com.urban.basicsample.adapter;

import java.util.ArrayList;

import com.urban.basicsample.R;
import com.urban.basicsample.model.Lesson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LessonAdapter extends BaseAdapter {

	Context ctx;
	LayoutInflater lInflater;
	ArrayList<Lesson> objects;

	public LessonAdapter(Context context, ArrayList<Lesson> lessons) {
		ctx = context;
		objects = lessons;
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
			view = lInflater.inflate(R.layout.list_item1, parent, false);
		}
		
		Lesson l = getLesson(position);
		
		TextView tv1 = (TextView) view.findViewById(R.id.listSubject);
		TextView tv2 = (TextView) view.findViewById(R.id.listGroup);
		TextView tv3 = (TextView) view.findViewById(R.id.listDate);
		tv1.setText(l.getSubject());
		tv2.setText(l.getGroup());
		tv3.setText(l.getDate());
		
		return view;
	}
	
	Lesson getLesson(int pos) {
		return (Lesson) getItem(pos);
	}

}

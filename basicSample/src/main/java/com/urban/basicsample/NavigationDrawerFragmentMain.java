package com.urban.basicsample;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NavigationDrawerFragmentMain extends NavigationDrawerFragment {

	private boolean access = true;

	public NavigationDrawerFragmentMain() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		String[] sections;
		if (access) {
			sections = new String[] { 
					getString(R.string.title_section0), 
					getString(R.string.title_list),
					getString(R.string.title_section1), 
					getString(R.string.title_section2),
					getString(R.string.title_section_im), 
					getString(R.string.title_load_sh),
					getString(R.string.title_section3)
					};
		} else {
			sections = new String[] { getString(R.string.title_list), getString(R.string.title_section3), };
		}

		mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer1, container, false);

		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectItem(position);
			}
		});
		mDrawerListView.setAdapter(new ArrayAdapter<String>(getActionBar().getThemedContext(),
				android.R.layout.simple_list_item_activated_1, android.R.id.text1, sections
		));
		mDrawerListView.setItemChecked(mCurrentSelectedPosition, false);
		return mDrawerListView;
	}
}

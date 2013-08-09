package com.android.securecontainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Spinner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class SecureContainerMainActivity extends Activity
										 implements OnItemSelectedListener {
	private static final String TAG = "SecureContainer";

	private ArrayList<String> mApplicationList;
	private ArrayAdapter mApplicationListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		mApplicationList = new ArrayList<String>();

		final ListView applicationListView = (ListView) findViewById(R.id.application_list);
		mApplicationListAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mApplicationList);

		applicationListView.setAdapter(mApplicationListAdapter);
		applicationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String packageName = (String) parent.getItemAtPosition(position);
				Intent appLaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
				if (appLaunchIntent == null) {
					Toast.makeText(getBaseContext(), R.string.app_launch_failed, Toast.LENGTH_SHORT).show();
					return;
				}

				startActivity(appLaunchIntent);
			}

		});

		Spinner domainListSpinner = (Spinner) findViewById(R.id.domain_filter);
		ArrayAdapter<CharSequence> domainListAdapter = ArrayAdapter.createFromResource(this, R.array.domain_types,
										android.R.layout.simple_spinner_item);
		domainListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		domainListSpinner.setAdapter(domainListAdapter);
		domainListSpinner.setOnItemSelectedListener(this);
	}

	private void fillApplicationListForCategory(String category) {
		final PackageManager pm = getPackageManager();

		mApplicationList.clear();

		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			Log.d(TAG, "Installed package :" + packageInfo.packageName);
			Log.d(TAG, "Launch Activity :"
				+ pm.getLaunchIntentForPackage(packageInfo.packageName));
			Log.d(TAG, "seinfo :" + packageInfo.seinfo);

			for (String mmacType : packageInfo.mmacTypes) {
				Log.d(TAG, "mmacType :" + mmacType);
			}

			if (packageInfo.seinfo.equals(category) &&
				// NOTE using this for determination wether the package contains
				// a launchable component is rather ugly and slow but works for our
				// purpose
				pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {

				Log.d(TAG, "Found platform application");
				mApplicationList.add(packageInfo.packageName);
			}
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		String category = (String) parent.getItemAtPosition(pos);
		fillApplicationListForCategory(category);
		mApplicationListAdapter.notifyDataSetChanged();
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}
}

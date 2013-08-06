package com.android.securecontainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class SecureContainerMainActivity extends Activity {
	private static final String TAG = "SecureContainer";
	private static final String SEINFO_PLATFORM = "platform";
	private static final String SEINFO_SYSTEM = "system";
	private static final String SEINFO_TRUSTED = "trusted";
	private static final String SEINFO_UNTRUSTED = "untrusted";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		final PackageManager pm = getPackageManager();

		final ArrayList<String> containerApplications = new ArrayList<String>();

		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		for (ApplicationInfo packageInfo : packages) {
			Log.d(TAG, "Installed package :" + packageInfo.packageName);
			Log.d(TAG,
					"Launch Activity :"
							+ pm.getLaunchIntentForPackage(packageInfo.packageName));
			Log.d(TAG, "seinfo :" + packageInfo.seinfo);

			if (packageInfo.seinfo.equals(SEINFO_TRUSTED) &&
				// NOTE using this for determination wether the package contains
				// a launchable component is rather ugly and slow but works for our
				// purpose
				pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {

				Log.d(TAG, "Found platform application");
				containerApplications.add(packageInfo.packageName);
			}
		}

		final ListView applicationListView = (ListView) findViewById(R.id.application_list);
		final StableArrayAdapter adapter = new StableArrayAdapter(this,
				android.R.layout.simple_list_item_1, containerApplications);

		applicationListView.setAdapter(adapter);
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
	}

	private class StableArrayAdapter extends ArrayAdapter<String> {
		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			for (int i = 0; i < objects.size(); ++i) {
				mIdMap.put(objects.get(i), i);
			}
		}

		@Override
		public long getItemId(int position) {
			String item = getItem(position);
			return mIdMap.get(item);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}

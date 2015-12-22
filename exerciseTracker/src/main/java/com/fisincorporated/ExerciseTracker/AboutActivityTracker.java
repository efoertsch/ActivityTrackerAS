package com.fisincorporated.ExerciseTracker;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AboutActivityTracker extends AppCompatActivity {
	TextView tvAbout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity_tracker);
		tvAbout = (TextView) findViewById(R.id.about_activity_tracker_tvAbout);
		tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
		  tvAbout.setText(Html.fromHtml(loadAboutText()));
		//tvAbout.setText(loadAboutText());


	}

	// Keep just in case
	private String loadAboutText() {
		StringBuffer sb = new StringBuffer();
		AssetManager assetManager = getResources().getAssets();
		InputStream inputStream = null;

		try {
			inputStream = assetManager.open("about.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(), "Oops About file is missing "
					+ e.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(),
					"Oops. Error reading About file" + e.toString(),
					Toast.LENGTH_LONG).show();;
		}
		finally{
			return sb.toString();
		}
		

	}

	 

}

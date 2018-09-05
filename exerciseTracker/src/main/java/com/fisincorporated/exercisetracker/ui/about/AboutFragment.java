package com.fisincorporated.exercisetracker.ui.about;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.BuildConfig;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.master.ExerciseDaggerFragment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class AboutFragment extends ExerciseDaggerFragment {

    public AboutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_activity_tracker, container, false);
        TextView tvAbout = (TextView) view.findViewById(R.id.about_activity_tracker_tvAbout);

        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout.setText(Html.fromHtml(loadAboutText()));

        TextView tvAppVersionCode = (TextView) view.findViewById(R.id.about_app_version_code);
        tvAppVersionCode.setText(getString(R.string.app_version_code, getAppVersionCode()));

        TextView tvAppVersionName = (TextView) view.findViewById(R.id.about_app_version_name);
        tvAppVersionName.setText(getString(R.string.app_version_name, getAppVersionName()));

        TextView tvDbVersion = (TextView) view.findViewById(R.id.about_database_version);
        tvDbVersion.setText(getString(R.string.db_version, getDatabaseVersion()));
        return view;
    }


    // Keep just in case
    private String loadAboutText() {
        StringBuilder sb = new StringBuilder();
        AssetManager assetManager = getResources().getAssets();
        InputStream inputStream;
        try {
            inputStream = assetManager.open("about.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(getActivity().getApplicationContext(), "Oops About file is missing "
                    + e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Oops. Error reading About file" + e.toString(),
                    Toast.LENGTH_LONG).show();
        }
        return sb.toString();
    }

    private int getDatabaseVersion(){
        int version = database.getVersion();
        return version;
    }

    private int getAppVersionCode(){
        return  BuildConfig.VERSION_CODE;

    }

    private String getAppVersionName(){
        return  BuildConfig.VERSION_NAME;
    }
}

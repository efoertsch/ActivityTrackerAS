package com.fisincorporated.exercisetracker.ui.about;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class AboutFragment extends Fragment {
    private TextView tvAbout;

    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.about_activity_tracker, container, false);
        tvAbout = (TextView) view.findViewById(R.id.about_activity_tracker_tvAbout);
        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout.setText(Html.fromHtml(loadAboutText()));
        return view;
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
            Toast.makeText(getActivity().getApplicationContext(), "Oops About file is missing "
                    + e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Oops. Error reading About file" + e.toString(),
                    Toast.LENGTH_LONG).show();
            ;
        } finally {
            return sb.toString();
        }


    }
}

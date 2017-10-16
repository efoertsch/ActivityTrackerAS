package com.fisincorporated.exercisetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ProgramOptionsFragment extends ExerciseMasterFragment implements
        OnItemSelectedListener {
    private Spinner spnrUnits;
    private ArrayAdapter<CharSequence> spnrAdapter;
    private String selectedUnits = null;

//    private Button btnCancel;
//    private Button btnSave;
    private Button btnBackupRestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.program_options, container, false);
        getReferencedViews(view);
        getSavedInstanceState(savedInstanceState);
        return view;
    }

    private void getReferencedViews(View view) {
        spnrUnits = (Spinner) view.findViewById(R.id.program_options_spnrUnits);
        spnrUnits.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner
        // layout
        spnrAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.displayUnits,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        spnrAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spnrUnits.setAdapter(spnrAdapter);


        TextView tvBackupDatabase = (TextView) view.findViewById(R.id.tvBackupDatabase);
        tvBackupDatabase.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BackupRestore.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        String key = getResources().getString(R.string.display_units);
        String imperialMetric = getResources().getString(R.string.imperial);
        try {
            if (selectedUnits == null) {
                // read current display units

                selectedUnits = databaseHelper.getProgramOption(database, key,
                        imperialMetric);
            }
            int spinnerPosition = spnrAdapter.getPosition(selectedUnits);
            // set the default according to value
            spnrUnits.setSelection(spinnerPosition);

        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    "Error in obtaining display units: " + e.toString(),
                    Toast.LENGTH_LONG).show();

        }
    }

    // save the selected Units so if you change orientation you have the current
    // value. This is called before activity destroy (perhaps due to orientation changes
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(
                getResources().getString(R.string.display_units), selectedUnits);
    }


    private void getSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedUnits = savedInstanceState.getString(getResources().getString(
                    R.string.display_units));
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String newSelectedUnits = (String) parent.getItemAtPosition(pos);
        if (!selectedUnits.equalsIgnoreCase(newSelectedUnits)){
            selectedUnits = newSelectedUnits;
            updateDisplayUnits();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        ;

    }

    private void updateDisplayUnits() {
        String key = getResources().getString(R.string.display_units);
        try {
            if (selectedUnits != null) {
                if (true == databaseHelper.setProgramOption(database, key, selectedUnits)) {
                    Toast.makeText(getActivity(),
                            "Display Units updated. ", Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    "Error in updating display units: " + e.toString(),
                    Toast.LENGTH_LONG).show();

        }
    }


}

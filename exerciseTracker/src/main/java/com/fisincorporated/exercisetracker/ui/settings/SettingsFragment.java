package com.fisincorporated.exercisetracker.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.backupandrestore.BackupScheduler;
import com.fisincorporated.exercisetracker.ui.drive.DriveSignOnActivity;
import com.fisincorporated.exercisetracker.ui.utils.DisplayUnits;
import com.fisincorporated.exercisetracker.utility.PhotoUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private boolean handleThisChange = true;

    @Inject
    PhotoUtils photoUtils;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    DisplayUnits displayUnits;

    public static SettingsFragment newInstance(Bundle bundle) {
        SettingsFragment settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(bundle);
        return settingsFragment;
    }

    @Override
    public void onAttach(Context context) {
        // Note: AndroidSupportInjection not AndroidInjection
        AndroidSupportInjection.inject(this);
        super.onAttach(context);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        if (getArguments() == null) {
            addPreferencesFromResource(R.xml.preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences_backup);
        }

        setupPreferences();
    }

    // cribbed some code from http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
    private void setupPreferences() {
        Preference photoImagePreference = findPreference(getString(R.string.startup_image));
        if (photoImagePreference != null) {
            photoImagePreference.setOnPreferenceClickListener(preference -> {
                String currentPhotoPath = photoUtils.getStartupPhotoPath();
                if (currentPhotoPath == null) {
                    Intent intent = new Intent();
                    // Show only images, no videos or anything else
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GlobalValues.PICK_PHOTO);
                } else {
                    Intent intent = new Intent(SettingsFragment.this.getContext(), ChangeStartupPhotoActivity.class);
                    startActivity(intent);
                }
                return true;
            });
        }

        Preference backupPreference = findPreference(getString(R.string.backup_button_key));
        if (backupPreference != null) {
            backupPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(getString(R.string.backup_button_key), 0);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.drive_backup)) && handleThisChange) {
            boolean driveBackup = sharedPreferences.getBoolean(getString(R.string.drive_backup), false);
            if (driveBackup) {
                checkDriveSignOn();
            } else {
                // TODO revoke Drive approval
            }
            handleThisChange = true;
            return;
        }
        if (key.equals(getString(R.string.local_backup)) && handleThisChange) {
            boolean localBackup = sharedPreferences.getBoolean(getString(R.string.local_backup), false);
            if (localBackup) {
                displayBackupNowDialog(GlobalValues.BACKUP_TO_LOCAL);
            }
            return;
        }
        if (key.equals(getString(R.string.display_units_preference_key))){
            displayUnits.refreshUnits();
        }

    }

    private void checkDriveSignOn() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (signInAccount == null) {
            Intent intent = new Intent(getActivity(), DriveSignOnActivity.class);
            startActivityForResult(intent, GlobalValues.BACKUP_TO_DRIVE);
        } else {
            updateDrivePreference(true);
        }
    }

    private void checkSelectPhoto() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Show an exlpanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            GlobalValues.PICK_PHOTO);

                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        GlobalValues.PICK_PHOTO);
            }
        } else {
            selectPhotoFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case GlobalValues.PICK_PHOTO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectPhotoFromGallery();
                } else {
                    //TODO  - do something
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void selectPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GlobalValues.PICK_PHOTO);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case (GlobalValues.PICK_PHOTO): {
                if (resultCode == RESULT_OK
                        && intent != null
                        && intent.getData() != null) {
                    photoUtils.saveToInternalStorage(getActivity(), intent.getData());
                }
            }
            case (GlobalValues.BACKUP_TO_DRIVE): {
                if (resultCode == RESULT_OK) {
                    boolean driveSigninSuccess = intent.getBooleanExtra(getString(R.string.drive_backup), false);
                    updateDrivePreference(driveSigninSuccess);
                }
            }
        }
    }

    private void updateDrivePreference(boolean driveSigninSuccess) {
        boolean doDriveBackup = sharedPreferences.getBoolean(getContext().getString(R.string.drive_backup), false);
        if (driveSigninSuccess && doDriveBackup) {
            displayBackupNowDialog(GlobalValues.BACKUP_TO_DRIVE);
        } else if (!driveSigninSuccess) {
            setDriveBackupPreference(sharedPreferences, false);
        }
    }

    private void setDriveBackupPreference(SharedPreferences sharedPreferences, boolean doDriveBackup) {
        handleThisChange = false;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getContext().getString(R.string.drive_backup), doDriveBackup);
        editor.commit();
    }

    /**
     * @param backupType - backup to local Download or to Drive
     */
    private void displayBackupNowDialog(int backupType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.backup_database)
                .setMessage(R.string.would_you_like_to_schedule_backup_now);
        builder.setPositiveButton(R.string.backup_now, (dialog, which) -> {
            BackupScheduler.scheduleBackupJob(getContext(), backupType);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.backup_later, (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

}

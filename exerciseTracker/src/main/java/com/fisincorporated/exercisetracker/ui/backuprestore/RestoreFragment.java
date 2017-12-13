package com.fisincorporated.exercisetracker.ui.backuprestore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.backupandrestore.GoogleDriveUtil;
import com.fisincorporated.exercisetracker.backupandrestore.LocalBackupUtils;
import com.fisincorporated.exercisetracker.ui.drive.DriveSignOnActivity;
import com.fisincorporated.exercisetracker.ui.history.ActivityHistory;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;


// code from http://stackoverflow.com/questions/6540906/android-simple-export-and-import-of-sqlite-com.fisincorporated.database
// with slight modifications
public class RestoreFragment extends ExerciseMasterFragment {

    private static final String TAG = RestoreFragment.class.getSimpleName();

    private Button restoreFromLocalBtn;
    private Button restoreFromDriveBtn;
    private TextView restoreLocalLocation;
    private static final int RESTORE_FROM_LOCAL = 0;
    private static final int RESTORE_FROM_DRIVE = 1;
    private static final int GOOGLE_SIGNON = 2;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private GoogleSignInAccount signInAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.restore_fragment,
                container, false);
        restoreLocalLocation = (TextView) view.findViewById(R.id.restore_db_from_local_text);
        restoreLocalLocation.setText(getContext().getString(R.string.restore_will_be_from_local, Environment.DIRECTORY_DOWNLOADS));
        restoreFromLocalBtn = (Button) view.findViewById(R.id.restore_db_from_local_btn);
        restoreFromLocalBtn.setOnClickListener(v -> showRestoreDialog(RESTORE_FROM_LOCAL));
        restoreFromDriveBtn = (Button) view.findViewById(R.id.restore_db_from_drive_btn);
        restoreFromDriveBtn.setOnClickListener(v -> showRestoreDialog(RESTORE_FROM_DRIVE));
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showRestoreDialog(int restoreLocation) {
        ActivityDialogFragment dialog = ActivityDialogFragment.newInstance(-1,
                R.string.press_restore_to_restore,
                R.string.restore, -1,
                R.string.cancel);
        dialog.setTargetFragment(RestoreFragment.this, restoreLocation);
        dialog.show(getActivity().getSupportFragmentManager(), "confirmDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == RESTORE_FROM_LOCAL || requestCode == RESTORE_FROM_DRIVE) {
            int buttonPressed = intent.getIntExtra(
                    ActivityDialogFragment.DIALOG_RESPONSE, -1);
            if (buttonPressed == DialogInterface.BUTTON_POSITIVE) {
                runRestore(requestCode);
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.restore_cancelled), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == GOOGLE_SIGNON) {
            boolean signinSuccess = intent.getBooleanExtra(getString(R.string.drive_backup), false);
            if (signinSuccess) {
                runRestore(RESTORE_FROM_DRIVE);
            }
        }
    }

    private void runRestore(final int requestCode) {
        Completable completable;
        if (requestCode == RESTORE_FROM_LOCAL) {
            completable = LocalBackupUtils.getRestoreLocalCompletable(getActivity().getApplicationContext(),getActivity().getApplicationContext().getPackageName(), GlobalValues.DATABASE_NAME);
            subscribeToCompletable(completable);
        } else {
            if (haveDriveAccess()) {
                completable = GoogleDriveUtil.getRestoreFromDriveCompletable(getActivity(), signInAccount);
                subscribeToCompletable(completable);
            }
        }
    }

    private boolean haveDriveAccess() {
        signInAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (signInAccount == null) {
            Intent intent = new Intent(getActivity(), DriveSignOnActivity.class);
            startActivityForResult(intent, GOOGLE_SIGNON);
            return false;
        }
        return true;
    }

    private void subscribeToCompletable(Completable completable) {
        completable.subscribe(() -> {
                    displaySuccessfulRestoreDialog((getResources().getText(R.string.restore_successful)).toString());
                },
                throwable -> {
                    displayUnsuccessfulRestoreDialog((getResources().getText(R.string.restore_error, throwable.toString())).toString());
                });
    }

    private void displaySuccessfulRestoreDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(R.string.restore_database);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            goToHistoryActivity();
            dialog.dismiss();
            getActivity().finish();

        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void displayUnsuccessfulRestoreDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(R.string.restore_database);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void goToHistoryActivity() {
        Intent intent = new Intent(getActivity(), ActivityHistory.class);
        startActivity(intent);
    }

    private void log(Object object) {
        if (object != null) {
            Log.d(TAG, "log: " + object);
            Toast.makeText(getActivity(), object.toString(), Toast.LENGTH_SHORT).show();
            if (object instanceof Throwable) {
                ((Throwable) object).printStackTrace();
            }
        }
    }

}

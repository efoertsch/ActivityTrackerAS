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

import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.rxdrive.RxDrive;
import com.fisincorporated.exercisetracker.ui.history.ActivityHistory;
import com.fisincorporated.exercisetracker.ui.master.ExerciseMasterFragment;
import com.fisincorporated.exercisetracker.ui.utils.ActivityDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


// code from http://stackoverflow.com/questions/6540906/android-simple-export-and-import-of-sqlite-com.fisincorporated.database
// with slight modifications
public class RestoreFragment extends ExerciseMasterFragment {

    private static final String TAG = RestoreFragment.class.getSimpleName();

    private Button restoreFromLocalBtn;
    private Button restoreFromDriveBtn;
    private TextView restoreLocalLocation;
    private static final int RESTORE_FROM_LOCAL = 0;
    private static final int RESTORE_FROM_DRIVE = 1;

    private RxDrive rxDrive;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


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
        if (rxDrive != null) {
            rxDrive.disconnect();
            compositeDisposable.clear();
        }
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
    }


    private void runRestore(final int requestCode) {
        Completable completable;
        if (requestCode == RESTORE_FROM_LOCAL) {
            completable = BackupUtils.getRestoreLocalCompletable(getActivity().getApplicationContext());
            subscribeToCompletable(completable);
        } else {
            signInToDrive();
        }

    }

    private void signInToDrive() {
        rxDrive = new RxDrive(new GoogleApiClient.Builder(getActivity())
                .addScope(Drive.SCOPE_FILE));
        setupGoogleApiClientObservable();
        rxDrive.connect();
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

    /**
     * Start sign in activity.
     */
    private void setupGoogleApiClientObservable() {
        Disposable disposable = rxDrive.connectionObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionState -> {
                    switch (connectionState.getState()) {
                        case CONNECTED:
                            log("Connected");
                            subscribeToCompletable(BackupUtils.getRestoreDriveCompletable(getActivity().getApplicationContext(),rxDrive));
                            break;
                        case SUSPENDED:
                            log("SUSPENDED");
                            break;
                        case FAILED:
                            log(connectionState.getConnectionResult().getErrorMessage());
                            rxDrive.resolveConnection(getActivity(), connectionState.getConnectionResult());
                            break;
                        case UNABLE_TO_RESOLVE:
                            log("Unable to resolve. " + connectionState.getConnectionResult());
                            getActivity().finish();
                            break;
                    }
                }, this::log);
        compositeDisposable.add(disposable);

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

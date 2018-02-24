package com.fisincorporated.exercisetracker.ui.drive;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;


public class DriveSignOnActivity extends AppCompatActivity {

    private static final String TAG = DriveSignOnActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private GoogleSignInClient mGoogleSignInClient;

    protected ActionBar actionBar;
    protected Toolbar toolbar;

    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        // implement this in superclass?
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        signIn();
    }

    /**
     * Start sign in activity.
     */
    private void signIn() {
        Log.i(TAG, "Start sign in");
        mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    returnToSender(GlobalValues.DRIVE_SIGNIN_SUCCESSFUL);
                } else {
                    returnToSender(GlobalValues.DRIVE_SIGNIN_UNSUCCESSFUL);
                }
                break;
        }
    }

    private void returnToSender(boolean signinSuccess) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(getString(R.string.drive_backup),signinSuccess);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}

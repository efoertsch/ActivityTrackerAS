package com.fisincorporated.exercisetracker.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.TrackerDatabase.LocationExercise;

import java.util.Arrays;
import java.util.List;

//	https://developers.facebook.com/docs/android/open-graph
//http://stackoverflow.com/questions/21634138/working-example-of-latest-android-facebook-sdk-login

public class FacebookPostStatsFragment extends Fragment {
	private static final String TAG = "FacebookPostStatsFragment";
	private LoginButton loginButton;
	private Button updateStatusBtn;
	private TextView tvUserName;
	private TextView tvActivityStats;
	//use a UiLifecycleHelper to set a callback to handle the result of opening the Share Dialog
	private static final String PERMISSION = "publish_actions";
	private static final List<String> PERMISSIONS = Arrays
			.asList("publish_actions");
	private boolean canPresentShareDialog;
	//private GraphUser user;
	private ProfilePictureView profilePictureView;

	private String title = "";
	private String description = "";
	private String activityStats = "";
	private static final String lineSeparator = System
			.getProperty("line.separator");
	private CallbackManager callbackManager;

	private enum PendingAction {
		NONE, POST_STATUS_UPDATE
	}

	private PendingAction pendingAction = PendingAction.NONE;
	private final String PENDING_ACTION_BUNDLE_KEY = "com.fisincorporated.exercisetracker.facebook:PendingAction";

	public static FacebookPostStatsFragment newInstance(Bundle bundle) {
		FacebookPostStatsFragment fragment = new FacebookPostStatsFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (savedInstanceState != null) {
			String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}
		
		FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
	   
		 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		lookForArguments(savedInstanceState);
		View view = inflater.inflate(R.layout.facebook_post_stats, container,
				false);
		profilePictureView = (ProfilePictureView) view
				.findViewById(R.id.profilePicture);
		tvUserName = (TextView) view.findViewById(R.id.tvUserName);

		// LoginButton is Facebooks login/logout button
		loginButton = (LoginButton) view.findViewById(R.id.login_button);
	    loginButton.setReadPermissions("user_friends");
	    // If using in a fragment
	    loginButton.setFragment(this);    
	    // Other app specific specialization
	    
	    // Callback registration
	    callbackManager = CallbackManager.Factory.create();
	    loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
	        @Override
	        public void onSuccess(LoginResult loginResult) {
	            // App code
	        }

	        @Override
	        public void onCancel() {
	            // App code
	        }

	        @Override
	        public void onError(FacebookException exception) {
	            // App code
	        }


	    });    
		updateStatusBtn = (Button) view.findViewById(R.id.btnPostToFacebook);
		updateStatusBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Publish results?");
			}
		});

		// updateStatusBtn.setBackgroundResource(R.drawable.com_facebook_button_blue);
		buttonsEnabled(false);


		tvActivityStats = (TextView) view.findViewById(R.id.tvActivityStats);
		tvActivityStats.setText( getResources().getText(R.string.i_am_racking_up_the_miles) 
		+ lineSeparator + title + lineSeparator + description
				+ lineSeparator + activityStats);

		return view;
	}
	
	
	
	 

	// This needs some checking but in general
	// 1. See if arguments passed to Fragment via newInstance
	// setArguments(bundle) and if so get via getArguments
	// 2. See if any info in savedInstnaceState bundle
	// Need to be careful as logic below may need modification per the
	// circumstances
	private void lookForArguments(Bundle savedInstanceState) {
		Bundle bundle = null;
		if (getArguments() != null) {
			bundle = getArguments();
		}
		// If fragment destroyed but then later recreated,
		// the savedInstanceState will hold info (assuming saved via
		// onSaveInstanceState(... )
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
		}
		if (bundle != null) {
			// get values by key name

			title = bundle.getString(GlobalValues.TITLE);
			description = bundle.getString(LocationExercise.DESCRIPTION);
			activityStats = bundle.getString(GlobalValues.ACTIVITY_STATS);
		}

	}


	public void buttonsEnabled(boolean isEnabled) {
		// updateStatusBtn.setEnabled(isEnabled);
		// actually change visibility
		if (isEnabled) {
			updateStatusBtn.setVisibility(View.VISIBLE);
		} else {
			updateStatusBtn.setVisibility(View.INVISIBLE);
		}
	}

	
	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    callbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		savedState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}
}
package com.fisincorporated.exercisetracker.ui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.fisincorporated.exercisetracker.GlobalValues;

public class ActivityDialogFragment extends DialogFragment {
	
 public final static String DIALOG_RESPONSE = "com.fisincorporated.ExerciseTracker.dialog.response"; 
		
/**
 * Pass in resource id's or -1 if to ignore
 * @param title
 * @param message
 * @param positiveButtonMsg
 * @param negativeButtonMsg
 * @param neutralButtonMsg
 * @return
 */
	public static ActivityDialogFragment newInstance( int title, int message,int  positiveButtonMsg, int negativeButtonMsg, int neutralButtonMsg ) {
		ActivityDialogFragment frag = new ActivityDialogFragment();
		Bundle args = new Bundle();
		args.putInt(GlobalValues.TITLE, title);
		args.putInt(GlobalValues.MESSAGE, message);
		args.putInt(GlobalValues.POSITIVE_BUTTON_MSG, positiveButtonMsg);
		args.putInt(GlobalValues.NEGATIVE_BUTTON_MSG, negativeButtonMsg);
		args.putInt(GlobalValues.NEUTRAL_BUTTON_MSG, neutralButtonMsg);
		frag.setArguments(args);
		return frag;
	}
	
	public static ActivityDialogFragment newInstance( int title, String message,int  positiveButtonMsg, int negativeButtonMsg, int neutralButtonMsg ) {
		ActivityDialogFragment frag = new ActivityDialogFragment();
		Bundle args = new Bundle();
		args.putInt(GlobalValues.TITLE, title);
		args.putString(GlobalValues.MESSAGE_STRING, message);
		args.putInt(GlobalValues.POSITIVE_BUTTON_MSG, positiveButtonMsg);
		args.putInt(GlobalValues.NEGATIVE_BUTTON_MSG, negativeButtonMsg);
		args.putInt(GlobalValues.NEUTRAL_BUTTON_MSG, neutralButtonMsg);
		frag.setArguments(args);
		return frag;
	}
	
	

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		int title = getArguments().getInt(GlobalValues.TITLE, -1);
		int message = getArguments().getInt(GlobalValues.MESSAGE, -1);
		String messageString = getArguments().getString(GlobalValues.MESSAGE_STRING);
		int positiveMsg = getArguments().getInt(GlobalValues.POSITIVE_BUTTON_MSG, -1);
		int negativeMsg = getArguments().getInt(GlobalValues.NEGATIVE_BUTTON_MSG, -1);
		int neutralMsg = getArguments().getInt(GlobalValues.NEUTRAL_BUTTON_MSG , -1);
		if (title != -1){
			builder.setTitle(title);
		}
		if (message != -1){
			builder.setMessage(message);
		}
		else if (messageString != null){
			builder.setMessage(messageString);
		}
	 
		if (positiveMsg != -1){
		// The OK button returns a (possibly) updated filter list)
		builder.setPositiveButton(positiveMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//Log.i(GlobalValues.LOG_TAG, "Positive Button Clicked");
						sendResult(Activity.RESULT_OK, id);
					}
				});
		}
		if (negativeMsg != -1){
		// Negative button is Cancel so return the original filter list
		builder.setNegativeButton(negativeMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//Log.i(GlobalValues.LOG_TAG, "NegativeButton Clicked");
						sendResult(Activity.RESULT_OK, id);
					}
				});
		}
		if (neutralMsg != -1){
		// set neutral is to clear filter (show all)
		builder.setNeutralButton(neutralMsg,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//Log.i(GlobalValues.LOG_TAG, "Neutral Clicked");
						sendResult(Activity.RESULT_OK, id);
					}
				});
		}
		return builder.create();
	}

 
	private void sendResult(int resultCode, int button_pressed) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(ActivityDialogFragment.DIALOG_RESPONSE , button_pressed );
		getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode,
				intent);
	}

}

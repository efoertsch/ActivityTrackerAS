package com.fisincorporated.broadcastreceiver;

import com.fisincorporated.ExerciseTracker.GPSLocationManager;
import com.fisincorporated.database.LocationExerciseRecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class UpdateLerReceiver extends BroadcastReceiver {
	private static final String TAG = "UpdateLerReceiver";
	@Override
	public void onReceive(Context c, Intent intent) {
		LocationExerciseRecord ler = (LocationExerciseRecord)intent.getParcelableExtra(GPSLocationManager.LER_UPDATE);
		if (ler != null)
			onLerUpdate(ler);
    return;
	}
	// Override this method in the class that uses this broadcastreceiver
	protected void onLerUpdate(LocationExerciseRecord ler) {
		Log.d(TAG, this + " Got ler update id: " + ler.get_id());
	}
}

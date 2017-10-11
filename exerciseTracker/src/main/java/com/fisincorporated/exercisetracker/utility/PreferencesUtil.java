package com.fisincorporated.exercisetracker.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;

import java.io.IOException;


public class PreferencesUtil {

    private static final String TAG = PreferencesUtil.class.getSimpleName();

    public static void storePhotoPath(Context context, String filePath) {
        SharedPreferences sharedPref = context.getSharedPreferences(GlobalValues.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.startup_image), filePath);
        editor.commit();
    }

    public static Bitmap getStartupBackgroundPhoto(Context context) {
        Bitmap photoBitmap = null;
        SharedPreferences sharedPref = context.getSharedPreferences(GlobalValues.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String photoPath = sharedPref.getString(context.getString(R.string.startup_image), null);
        if (photoPath != null) {
            Uri uri = Uri.parse(photoPath);
            try {
                photoBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }
        return photoBitmap;
    }
}

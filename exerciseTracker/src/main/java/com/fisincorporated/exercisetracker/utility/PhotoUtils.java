package com.fisincorporated.exercisetracker.utility;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.maps.PhotoDetail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class PhotoUtils {

    private static final String TAG = PhotoUtils.class.getSimpleName();

    public static void storePhotoPath(Context context, String filePath) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.startup_image), filePath);
        editor.commit();
    }

    public static String getStartupPhotoPath(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.startup_image), null);
    }

    public static boolean loadPhotoToImageView(ImageView imageView, String photoPath) {
        if (photoPath != null) {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                imageView.setImageURI(Uri.fromFile(imgFile));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return true;
            }
        }
        return false;
    }

    public static void saveToInternalStorage(Activity activity, Uri imageUri) {
        InputStream imageStream = null;
        try {
            imageStream = activity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmapImage = BitmapFactory.decodeStream(imageStream);
            String filename = PhotoUtils.getFileNameFromURI(activity, imageUri);
            writeResizedBitmap(activity, bitmapImage, filename);
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());

        } finally {
            if (imageStream != null) {
                try {
                    imageStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void writeResizedBitmap(Activity activity, Bitmap bitmapImage, String filename) {
        FileOutputStream fos = null;
        try {
            ContextWrapper cw = new ContextWrapper(activity);
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir(GlobalValues.IMAGE_DIR, Context.MODE_PRIVATE);
            // Create imageDir
            File photoPathFile = new File(directory, filename);
            deleteAllFiles(directory);
            storePhotoPath(activity, photoPathFile.toString());
            fos = new FileOutputStream(photoPathFile);
            // Use the compress method on the BitMap object to write image to the OutputStream
            Log.d(TAG, resizeForPortraitView(bitmapImage).compress(Bitmap.CompressFormat.JPEG, 50, fos) ? " Successful save of image" : " Unsuccessful save of image");
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }


    public static Bitmap getResizedImage(Bitmap bitmap) {
        int[] imageSize = new int[]{bitmap.getWidth(), bitmap.getHeight()};
        int[] newSize;
        int[] screenSize = getScreenSize();
        if (imageSize[0] > imageSize[1]) {
            // landscape photo - w > h
            if (screenSize[0] > screenSize[1]) {
                // landscape device orientation
                return PhotoUtils.resize(bitmap, screenSize[1], screenSize[0]);
            } else {
                // portrait device orientation
                return PhotoUtils.resize(bitmap, screenSize[1], screenSize[0]);
            }
        } else {
            // portrait photo  w < h
            if (screenSize[0] > screenSize[1]) {
                // landscape device orientation
                return PhotoUtils.resize(bitmap, screenSize[1], screenSize[0]);
            } else {
                // portrait device orientation
                return PhotoUtils.resize(bitmap, screenSize[1], screenSize[0]);
            }
        }
    }


    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                // landscape photo
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                // protrait photo
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    /**
     * Resize photo so for display in portrait mode
     * Current logic is based on 4:3 (w x h) image aspect ratio
     * and device screen is something like 5:3 (w x h)
     * So this may need work for other screen/photo sizes
     *
     * @param image
     * @return
     */
    public static Bitmap resizeForPortraitView(Bitmap image) {
        if (image == null) {
            return null;
        }

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if (imageWidth == 0 || imageHeight == 0) {
            return image;
        }

        int deviceWidth = getScreenWidth();
        int deviceHeight = getScreenHeight();

        // currently landscape orientation so switch numbers
        if (deviceWidth > deviceWidth) {
            // currently landscape orientation
            deviceWidth = deviceHeight;
            deviceHeight = getScreenWidth();
        }

        float scaleRatio;
        int finalWidth;
        int finalHeight;

        if (imageWidth > imageHeight) {
            // image is landscape orientation
            scaleRatio = (float) deviceHeight / (float) imageHeight;
            finalHeight = deviceHeight;
            finalWidth = (int) (scaleRatio * (float) imageWidth);
        } else {
            // image is portrait oriented
            scaleRatio = (float) deviceWidth / (float) imageWidth;
            finalWidth = deviceHeight;
            finalHeight = (int) (scaleRatio * (float) imageHeight);
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
        return image;
    }


    public static String getFileNameFromURI(Context context, Uri uri) {
        String fileName = "";
        ContentResolver cr = context.getContentResolver();
        String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor metaCursor = cr.query(uri, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    fileName = metaCursor.getString(0);
                }
            } finally {
                metaCursor.close();
            }
        }
        return fileName;
    }

    public static void deleteAllFiles(File dir) {
        for (File file : dir.listFiles())
            if (!file.isDirectory() && file.toString().endsWith(".jpeg"))
                file.delete();
    }

    /**
     * get Screen size in pixels -  w x h
     *
     * @return
     */
    public static int[] getScreenSize() {
        int[] size = {getScreenWidth(), getScreenHeight()};
        return size;
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    public static ArrayList<PhotoDetail> getPhotosTaken(Context context, Long startTime, Long endTime) {
        ArrayList<PhotoDetail> photosTaken = new ArrayList<>();
        if (startTime != null && endTime != null){
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor;
            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN};
            String selection = MediaStore.Images.Media.DATE_TAKEN + " >= ? and " + MediaStore.Images.Media.DATE_TAKEN + " <=  ?";
            String[] selectionArgs = {startTime.toString(), endTime.toString()};
//            String selection = null;
//            String[] selectionArgs = null;


            String orderBy = MediaStore.Images.Media.DATE_TAKEN;

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy + " ASC");

            int pathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            int latitudeIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
            int longitudeIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

            while (cursor.moveToNext()) {
                PhotoDetail photoDetail = new PhotoDetail().setPhotoPath(cursor.getString(pathIndex))
                        .setDateTaken(Long.parseLong(cursor.getString(dateTakenIndex)))
                        .setLatitude(cursor.getString(latitudeIndex))
                        .setLongitude(cursor.getString(longitudeIndex));
                photosTaken.add(photoDetail);
            }
            cursor.close();
        }
        return photosTaken;
    }

}

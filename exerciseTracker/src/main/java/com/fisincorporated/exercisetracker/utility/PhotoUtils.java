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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.ui.media.MediaDetail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.Single;


public class PhotoUtils {

    private static final String TAG = PhotoUtils.class.getSimpleName();

    private Context context;

    public PhotoUtils(Context context) {
        this.context = context;
    }

    public void storePhotoPath(String filePath) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.startup_image), filePath);
        editor.commit();
    }

    public String getStartupPhotoPath() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(context.getString(R.string.startup_image), null);
    }

    public boolean loadPhotoToImageView(ImageView imageView, String photoPath, ProgressBar progressBar, TextView errorTextView) {
        if (photoPath != null) {
            File imgFile = new File(photoPath);
            if (imgFile.exists()) {
                progressBar.setVisibility(View.VISIBLE);
                Glide.with(imageView.getContext())
                        .load(imgFile)
                        .listener(new RequestListener<File, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                                errorTextView.setText(R.string.error_occurred_loading_photo);
                                progressBar.setVisibility(View.GONE);
                                if (errorTextView != null) {
                                    errorTextView.setVisibility(View.VISIBLE);
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                if (errorTextView != null) {
                                    errorTextView.setVisibility(View.GONE);
                                }
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                return false;
                            }
                        })
                        .error(R.id.startup_photo_no_photo_text)
                        .into(imageView);

                return true;
            }
        }
        return false;
    }

    public void saveToInternalStorage(Activity activity, Uri imageUri) {
        InputStream imageStream = null;
        try {
            imageStream = activity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmapImage = BitmapFactory.decodeStream(imageStream);
            String filename = getFileNameFromURI(imageUri);
            writeResizedBitmap(bitmapImage, filename);
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

    public void writeResizedBitmap(Bitmap bitmapImage, String filename) {
        FileOutputStream fos = null;
        try {
            File directory = getPhotoFileDirectory();
            // Create imageDir
            File photoPathFile = new File(directory, filename);
            deleteAllFiles(directory);
            storePhotoPath(photoPathFile.toString());
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

    public File getPhotoFileDirectory() {
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        return cw.getDir(GlobalValues.IMAGE_DIR, Context.MODE_PRIVATE);
    }

    public void removeStorePhotoPreference() {
        File directory = getPhotoFileDirectory();
        deleteAllFiles(directory);
        storePhotoPath(null);
    }


    public Bitmap getResizedImage(Bitmap bitmap) {
        int[] imageSize = new int[]{bitmap.getWidth(), bitmap.getHeight()};
        int[] newSize;
        int[] screenSize = getScreenSize();
        if (imageSize[0] > imageSize[1]) {
            // landscape photo - w > h
            if (screenSize[0] > screenSize[1]) {
                // landscape device orientation
                return resize(bitmap, screenSize[1], screenSize[0]);
            } else {
                // portrait device orientation
                return resize(bitmap, screenSize[1], screenSize[0]);
            }
        } else {
            // portrait photo  w < h
            if (screenSize[0] > screenSize[1]) {
                // landscape device orientation
                return resize(bitmap, screenSize[1], screenSize[0]);
            } else {
                // portrait device orientation
                return resize(bitmap, screenSize[1], screenSize[0]);
            }
        }
    }


    public Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
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
    public Bitmap resizeForPortraitView(Bitmap image) {
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


    public String getFileNameFromURI(Uri uri) {
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

    public void deleteAllFiles(File dir) {
        for (File file : dir.listFiles())
            if (!file.isDirectory() && file.toString().endsWith(".jpeg")) {
                file.delete();
            }
    }

    /**
     * get Screen size in pixels -  w x h
     *
     * @return
     */
    public int[] getScreenSize() {
        int[] size = {getScreenWidth(), getScreenHeight()};
        return size;
    }

    public int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public Single<ArrayList<MediaDetail>> getPhotoDetailListObservable(Context context, Long startTime, Long endTime) {
        return Single.create(emitter -> {
            emitter.onSuccess(getPhotosTaken(context, startTime, endTime));
        });
    }

    public Single<ArrayList<MediaDetail>> getVideoListObservable(Context context, Long startTime, Long endTime) {
        return Single.create(emitter -> {
            emitter.onSuccess(getVideoTaken(context, startTime, endTime));

        });
    }

    // Note to self - make sure you import io.reactivex.functions.BiConsumer, rather than java version
    public Single<ArrayList<MediaDetail>> getMediaListObservable(final Context context, final Long startTime, final Long endTime) {
        return Single.concat(getPhotoDetailListObservable(context, startTime, endTime)
                , getVideoListObservable(context, startTime, endTime))
                .collect(ArrayList::new
                        , (mediaDetails, mediaDetails2) -> {
                            mediaDetails.addAll(mediaDetails2);
                            Collections.sort(mediaDetails);

                        });
    }

    //MimeType null on MotoX 2nd gen
    public ArrayList<MediaDetail> getPhotosTaken(Context context, Long startTime, Long
            endTime) {
        ArrayList<MediaDetail> photosTaken = new ArrayList<>();
        if (startTime != null && endTime != null) {
            Cursor cursor;
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] columns = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN};
            String selection = MediaStore.Images.Media.DATE_TAKEN + " >= ? and " + MediaStore.Images.Media.DATE_TAKEN + " <=  ?";
            String[] selectionArgs = {startTime.toString(), endTime.toString()};
            String orderBy = MediaStore.Images.Media.DATE_TAKEN + " ASC";

            cursor = context.getContentResolver().query(uri, columns, selection, selectionArgs, orderBy);

            int pathIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            int latitudeIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
            int longitudeIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

            while (cursor.moveToNext()) {
                MediaDetail mediaDetail = new MediaDetail().setMediaPath(cursor.getString(pathIndex))
                        .setDateTaken(Long.parseLong(cursor.getString(dateTakenIndex)))
                        .setLatitude(cursor.getString(latitudeIndex))
                        .setLongitude(cursor.getString(longitudeIndex))
                        .setAsImage();
                photosTaken.add(mediaDetail);
//                Log.d(TAG, " Photo:" + mediaDetail.getMediaPath()
//                        + " Time:" + new Timestamp(mediaDetail.getDateTaken())
//                        + " At:" + mediaDetail.getLatitude() + ":" + mediaDetail.getLongitude());

            }
            cursor.close();
        }
        return photosTaken;
    }

    public ArrayList<MediaDetail> getVideoTaken(Context context, Long startTime, Long
            endTime) {
        ArrayList<MediaDetail> photosTaken = new ArrayList<>();
        if (startTime != null && endTime != null) {
            Cursor cursor;
            String[] columns = {MediaStore.Video.VideoColumns.DATA,
                    MediaStore.Video.VideoColumns.LATITUDE,
                    MediaStore.Video.VideoColumns.LONGITUDE,
                    MediaStore.Video.VideoColumns.DATE_TAKEN,
                    MediaStore.Video.VideoColumns.MINI_THUMB_MAGIC};

            String selection = MediaStore.Video.VideoColumns.DATE_TAKEN + " >= ? and "
                    + MediaStore.Video.VideoColumns.DATE_TAKEN + " <=  ? ";
            final String orderBy = MediaStore.Video.VideoColumns.DATE_TAKEN + " ASC ";
            String[] selectionArgs = {startTime.toString(), endTime.toString()};

            Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            cursor = context.getContentResolver().query(queryUri, columns, selection, selectionArgs, orderBy);
            int pathIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
            int latitudeIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.LATITUDE);
            int longitudeIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.LONGITUDE);
            int dateTakenIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN);

            String path;
            String dateTaken;
            String dateAdded;

            while (cursor.moveToNext()) {
                path = cursor.getString(pathIndex);
                dateTaken = cursor.getString(dateTakenIndex);

                if (path != null && dateTaken != null) {
                    MediaDetail mediaDetail = new MediaDetail().setMediaPath(path)
                            .setDateTaken(Long.parseLong(dateTaken))
                            .setLatitude(cursor.getString(latitudeIndex))
                            .setLongitude(cursor.getString(longitudeIndex))
                            .setAsVideo();
                    photosTaken.add(mediaDetail);
//                    Log.d(TAG, "Video:" + mediaDetail.getMediaPath()
//                            + " Time Taken long " + mediaDetail.getDateTaken()
//                            + " Time Taken:" + new Timestamp(mediaDetail.getDateTaken())
//                            + " Time Added long " + (dateAdded == null ? "missing" : Long.parseLong(dateAdded))
//                            + " Time Added:" + (dateAdded == null ? "missing" : new Timestamp(Long.parseLong(dateAdded)))
//                            + " MimeType:" + mediaDetail.getMimeType());
                }
            }
            cursor.close();
        }
        return photosTaken;
    }

}

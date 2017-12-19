package com.fisincorporated.exercisetracker.ui.maps;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class PhotosUtils {

    private PhotosUtils() {}

    public static ArrayList<PhotoDetail> getPhotosTaken(Context context, Long startTime, Long endTime) {
        ArrayList<PhotoDetail> photosTaken = new ArrayList<>();
        if (startTime != null && endTime != null){
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor;
            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN};
            String selection = MediaStore.Images.Media.DATE_TAKEN + " >= ? " + MediaStore.Images.Media.DATE_TAKEN + " <=  ?";
            String[] selectionArgs = {startTime.toString(), endTime.toString()};

            String orderBy = MediaStore.Images.Media.DATE_TAKEN;

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, orderBy + " DESC");

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


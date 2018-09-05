package com.fisincorporated.exercisetracker.ui.maps;




import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.fisincorporated.exercisetracker.GlobalValues;
import com.fisincorporated.exercisetracker.R;
import com.fisincorporated.exercisetracker.database.LocationExerciseRecord;
import com.fisincorporated.exercisetracker.utility.StatsUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Create a kml (keyhole markup language) file
 */

public class KmlWriter {

    private static String TAG = KmlWriter.class.getSimpleName();
    private static String newline = System.getProperty("line.separator");

    private Context context;
    private LocationExerciseRecord ler;
    private String kmlFileName = null;

    private File kmlPath = null;
    private File kmlFile = null;

    private String activityTitle = "";
    private String title = "";
    private String description = "";
    private Cursor cursor;

    StatsUtil statsUtil;

    public KmlWriter(StatsUtil statsUtil) {
        this.statsUtil = statsUtil;
    }

    public KmlWriter setContext(Context context) {
        this.context = context;
        return this;
    }

    public KmlWriter setLocationExerciseRecord(LocationExerciseRecord ler) {
        this.ler = ler;
        return this;
    }

    public KmlWriter setTitle(String title) {
        this.title = title;
        return this;
    }

    public KmlWriter setDescription(String description) {
        this.description = description;
        return this;
    }

    public KmlWriter setCursor(Cursor cursor) {
        this.cursor = cursor;
        return this;
    }

    void createKmlFileForEmailing() {
        if (createKMLFile()) {
            activityTitle = title + "  " + description;
            writeToKMLFile(cursor);
            emailKMLFile();
        }
    }

    private boolean createKMLFile() {
        String appName = context.getString(R.string.app_name);
        kmlFileCleanup();
        // Any changes to file format requires change to kmlFileCleanup
        kmlFileName = statsUtil.makeFileNameReady(appName
                + "."
                + title
                + "_"
                +  new SimpleDateFormat("yyyy-MM-dd HH:mm").format(ler
                .getStartTimestamp()) + ".kml");
        kmlPath = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        kmlFile = new File(kmlPath, kmlFileName);
        kmlPath.mkdirs();
        if (!kmlPath.isDirectory()) {
            Toast.makeText(
                    context,
                    "Sorry. The GPS attachment file could not be created at "
                            + kmlPath.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // Note that coordinates for kml file are longitude/latitude
    private void writeToKMLFile(Cursor csr) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(kmlFile.getAbsoluteFile())));
            writer.write(GlobalValues.KML_ROUTE_HEADER + newline);
            writer.write(" <Placemark><name>" + statsUtil.removeLtGt(activityTitle)
                    + "</name>" + newline);
            writer.write("<description>"
                    + statsUtil.removeLtGt(ler.getDescription()) + "</description>"
                    + newline);
            writer.write("<LineString><coordinates>" + newline);
            if (csr.getCount() > 0) {
                csr.moveToFirst();
                while (!csr.isAfterLast()) {
                    writer.write(csr.getDouble(1) + "," + csr.getDouble(0) + ","
                            + csr.getInt(2) + newline);
                    csr.moveToNext();
                }
            }
            writer.write(GlobalValues.KML_ROUTE_TRAILER + newline);
        } catch (Exception e) {
            Log.e(GlobalValues.LOG_TAG,
                    "ActivityMapFragment.writeToKMLFile error " + e.toString());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    ;
                }
            }
            // Note depending on supplying logic to close cursor.
        }
    }

    /**
     * Note this depends on having successfully written KML file and the
     * path/filename can be had from kmlPath and kmlFileName
     */
    private void emailKMLFile() {
        String summaryStats;
        if (!kmlFile.exists() || !kmlFile.canRead()) {
            Toast.makeText(context,
                    "Can't find or read the created logfile: " + kmlFile.getName(),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "KML attachment at " + kmlFile.getName(),
                Toast.LENGTH_SHORT).show();
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/xml");

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My "
                + activityTitle);

        summaryStats = getStatsForEmail();

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, summaryStats + context.getResources()
                .getString(R.string.kml_email_explain) + newline + newline);

        Uri uri = Uri.parse("file://" + kmlFile.getAbsolutePath());
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }


    private String getStatsForEmail() {
        ArrayList<String[]> stats = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.my_activity_statistics, title)).append(newline).append(newline);

        statsUtil.formatActivityStats(stats, ler, false);
        for (int i = 0; i < stats.size(); ++i) {
            sb.append(stats.get(i)[0]).append(":\t").append(stats.get(i)[1]).append(newline);
        }
        sb.append(newline);
        return sb.toString();
    }

    private void kmlFileCleanup() {
        final String appName = context.getString(R.string.app_name);
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] files = folder.listFiles((dir, name) -> name.matches(appName + ".*.@.*_*.kml"));
        for (final File file : files) {
            if (!file.delete()) {
                Log.e(GlobalValues.LOG_TAG,
                        "Can't remove " + file.getAbsolutePath());
            }
        }
    }

}

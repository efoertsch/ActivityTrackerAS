package com.fisincorporated.exercisetracker.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;


// From https://www.developerfeed.com/copy-bytes-inputstream-outputstream-android/
public class IoUtils {

    private static final int BUFFER_SIZE = 1024;

    private IoUtils() {
        // StatsUtil class.
    }

    public static int copy(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            closeStream(in);
            closeStream(out);
        }
        return count;
    }

    public static int copyWithNoClose(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;

        while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
            out.write(buffer, 0, n);
            count += n;
        }
        out.flush();

        return count;
    }

    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {

            }
        }
    }
}

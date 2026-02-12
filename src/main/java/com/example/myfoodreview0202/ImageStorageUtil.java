package com.example.myfoodreview0202;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageStorageUtil {
    public static String copyImageToAppDir(Context context, Uri uri) {
        try {
            String mime = context.getContentResolver().getType(uri);
            String ext = "jpg";
            if ("image/png".equals(mime)) ext = "png";
            else if ("image/webp".equals(mime)) ext = "webp";

            InputStream inputStream =
                    context.getContentResolver().openInputStream(uri);

            String fileName = "photo_" + System.currentTimeMillis() + "." + ext;
            File outFile = new File(context.getFilesDir(), fileName);

            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();
            outputStream.close();
            return outFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteImage(String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}

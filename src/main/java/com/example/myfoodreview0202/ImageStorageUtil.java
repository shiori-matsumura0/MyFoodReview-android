package com.example.myfoodreview0202;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


// ===== 外部（カメラorギャラリー）の画像をアプリ専用の内部ストレージにコピーする =====

public class ImageStorageUtil {
    public static String copyImageToAppDir(Context context, Uri uri) {
        try {
            // 画像の形式を確認して拡張子を決定する（jpg/png/webp）
            String mime = context.getContentResolver().getType(uri);
            String ext = "jpg";
            if ("image/png".equals(mime)) ext = "png";
            else if ("image/webp".equals(mime)) ext = "webp";

            InputStream inputStream =
                    context.getContentResolver().openInputStream(uri);

            
            // 保存先のファイル名と場所を決めて、現在の時刻（ミリ秒）をファイル名に使用
            String fileName = "photo_" + System.currentTimeMillis() + "." + ext;
            // アプリ専用の内部ストレージ（data/data/パッケージ名/files/）を取得
            File outFile = new File(context.getFilesDir(), fileName);

            // データのコピー実行
            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];    // 1KBずつ
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();
            outputStream.close();

            // 保存したファイルのフルパスを返す（これをDB等に保存する）
            return outFile.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== 指定されたパスにある画像ファイルを削除 =====
    public static void deleteImage(String path) {
        if (path == null) return;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
}


package com.example.myfoodreview0202;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ImageManager {

    public static final int REQUEST_CODE_CAMERA = 200;
    public static final int REQUEST_CODE_PICK_IMAGE = 100;

    private Activity activity;
    private ImageView mainImageView;
    private ImageView[] subImageViews;

    private ArrayList<String> photoPathList = new ArrayList<>();
    private String cameraPhotoPath;


    // ===== コンストラクタ =====
    public ImageManager(Activity activity, ImageView mainImageView, ImageView[] subImageViews) {
        this.activity = activity;
        this.mainImageView = mainImageView;
        this.subImageViews = subImageViews;
    }

    // ===== 画像パスリストの取得 =====
    public ArrayList<String> getPhotoPathList() {
        return photoPathList;
    }

    // ===== カメラの起動 =====
    public void startCamera() {
        if (photoPathList.size() >= 5) {
            Toast.makeText(activity, "写真は5枚までです", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN);
        String fileName = "IMG_" + sdf.format(new Date()) + ".jpg";

        File photoFile = new File(activity.getFilesDir(), fileName);
        cameraPhotoPath = photoFile.getAbsolutePath();

        Uri photoUri = FileProvider.getUriForFile(
                activity,
                activity.getPackageName() + ".fileprovider",
                photoFile
        );

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    // ===== ギャラリーの起動 =====
    public void startGallery() {

        if (photoPathList.size() >= 5) {
            Toast.makeText(activity, "写真は5枚までです", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        activity.startActivityForResult(
                Intent.createChooser(intent, "画像を選択"),
                REQUEST_CODE_PICK_IMAGE
        );
    }

    // ===== カメラorギャラリーからの結果の受け取り =====
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_CODE_CAMERA) {
            if (cameraPhotoPath != null) {
                photoPathList.add(cameraPhotoPath);
                cameraPhotoPath = null;
                updatePhotoViews();
            }
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();

                for (int i = 0; i < count; i++) {
                    if (photoPathList.size() >= 5) break;

                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    String path =
                            ImageStorageUtil.copyImageToAppDir(activity, uri);

                    if (path != null) photoPathList.add(path);
                }
            } else if (data.getData() != null) {
                if (photoPathList.size() < 5) {
                    String path =
                            ImageStorageUtil.copyImageToAppDir(
                                    activity, data.getData());
                    if (path != null) photoPathList.add(path);
                }
            }

            updatePhotoViews();
        }
    }

    // ===== 表示の更新 =====
    private void updatePhotoViews() {
        // メイン画像の表示
        if (!photoPathList.isEmpty()) {
            Glide.with(activity)
                    .load(new File(photoPathList.get(0)))
                    .centerCrop() // 中央で切り抜き（ImageViewのサイズに合わせる）
                    .into(mainImageView);
        } else {
            // 画像がない場合はnoimageを表示
            mainImageView.setImageResource(R.drawable.noimage);
        }

        // サブ画像の表示
        for (int i = 0; i < subImageViews.length; i++) {
            if (photoPathList.size() > i + 1) {
                Glide.with(activity)
                        .load(new File(photoPathList.get(i + 1)))
                        .centerCrop()
                        .placeholder(R.drawable.noimage) // 読み込み中の画像
                        .into(subImageViews[i]);
            } else {
                Glide.with(activity)
                        .load(R.drawable.noimage)
                        .into(subImageViews[i]);
            }
        }
    }
}



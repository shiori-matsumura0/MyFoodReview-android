package com.example.myfoodreview0202;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import java.io.File;
import java.util.ArrayList;

public class PhotoController {

    private ImageManager imageManager;
    private ImageView mainImageView;
    private ImageView[] subImageViews;
    private ArrayList<String> photoPathList;
    private Activity activity;

    public PhotoController(Activity activity, ImageView mainImageView, ImageView[] subImageViews) {
        this.activity = activity;
        this.mainImageView = mainImageView;
        this.subImageViews = subImageViews;

        imageManager = new ImageManager(activity, mainImageView, subImageViews);
        photoPathList = imageManager.getPhotoPathList();

        setListeners();
    }

    private void setListeners() {
        // サブ画像クリック
        for (int i = 0; i < subImageViews.length; i++) {
            final int index = i + 1;

            subImageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photoPathList.size() > index) {
                        String main = photoPathList.get(0);
                        photoPathList.set(0, photoPathList.get(index));
                        photoPathList.set(index, main);
                        updatePhotoViews();
                    }
                }
            });

            subImageViews[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    removePhoto(index);
                    return true;
                }
            });
        }

        // メイン長押し
        mainImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                removePhoto(0);
                return true;
            }
        });
    }

    public void updatePhotoViews() {
        if (!photoPathList.isEmpty()) {
            mainImageView.setImageURI(Uri.fromFile(new File(photoPathList.get(0))));
        } else {
            if (mainImageView != null) {
                mainImageView.setImageResource(R.drawable.noimage);
            }
        }

        for (int i = 0; i < subImageViews.length; i++) {
            if (photoPathList.size() > i + 1) {
                subImageViews[i].setImageURI(Uri.fromFile(new File(photoPathList.get(i + 1))));
            } else {
                subImageViews[i].setImageResource(R.drawable.noimage);
            }
        }
    }

    public void disableListeners() {
        if (mainImageView != null) {
            mainImageView.setOnLongClickListener(null);
        }
        for (ImageView iv : subImageViews) {
            iv.setOnClickListener(null);
            iv.setOnLongClickListener(null);
        }
    }

    public void removePhoto(int index) {
        if (photoPathList.size() > index) {
            photoPathList.remove(index);
            updatePhotoViews();
        }
    }

    public void attachImageButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChoicePopup(v);
            }
        });
    }

    private void showImageChoicePopup(View anchor) {
        PopupMenu popup = new PopupMenu(activity, anchor);
        popup.getMenu().add("写真を撮る");
        popup.getMenu().add("ギャラリーから選択");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                if (title.equals("写真を撮る")) {
                    imageManager.startCamera();
                } else if (title.equals("ギャラリーから選択")) {
                    imageManager.startGallery();
                }
                return true;
            }
        });
        popup.show();
    }

    public ArrayList<String> getPhotoPathList() {
        return photoPathList;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageManager.onActivityResult(requestCode, resultCode, data);
        updatePhotoViews();
    }

    public void loadPhotoFromPath(String path) {
        photoPathList.clear();
        if (path == null || path.isEmpty()) {
            if (mainImageView != null) mainImageView.setImageResource(R.drawable.noimage);
            for (ImageView iv : subImageViews) {
                iv.setImageResource(R.drawable.noimage);
            }
            return;
        }

        photoPathList.add(path);
        updatePhotoViews();
    }

    public void swapPhoto(int index1, int index2) {
        if (photoPathList.size() > index1 && photoPathList.size() > index2) {
            String temp = photoPathList.get(index1);
            photoPathList.set(index1, photoPathList.get(index2));
            photoPathList.set(index2, temp);
            updatePhotoViews();
        }
    }
}
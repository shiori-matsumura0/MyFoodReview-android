package com.example.myfoodreview0202;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SearchDialogFragment extends DialogFragment {

    private EditText etSearchKeyword;
    private Spinner spSearchPrefecture;
    private EditText etSearchArea;
    private Spinner spSearchGenre;
    private CheckBox cbSearchVisited;
    private CheckBox cbSearchWant;
    private ImageView ivSearchFavorite;
    private boolean isFavorite = false; // お気に入りON/OFF
    private ImageView[] stars = new ImageView[5];
    private int rating = 0;  // 現在の評価（0～5）

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_search, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.TransparentDialog);
        builder.setView(dialogView);

        // ===== 画面部品取得 =====
        etSearchKeyword = dialogView.findViewById(R.id.etSearchKeyword);
        spSearchPrefecture = dialogView.findViewById(R.id.spSearchPrefecture);
        spSearchPrefecture.setSelection(0);
        etSearchArea = dialogView.findViewById(R.id.etSearchArea);
        etSearchArea.setText("");
        spSearchGenre = dialogView.findViewById(R.id.spSearchGenre);
        spSearchGenre.setSelection(0);

        stars[0] = dialogView.findViewById(R.id.ivSearchStar1);
        stars[1] = dialogView.findViewById(R.id.ivSearchStar2);
        stars[2] = dialogView.findViewById(R.id.ivSearchStar3);
        stars[3] = dialogView.findViewById(R.id.ivSearchStar4);
        stars[4] = dialogView.findViewById(R.id.ivSearchStar5);
        cbSearchVisited = dialogView.findViewById(R.id.cbSearchVisited);
        cbSearchWant = dialogView.findViewById(R.id.cbSearchWant);
        ivSearchFavorite = dialogView.findViewById(R.id.ivSearchFavorite);

        // ===== お気に入りのクリックリスナー =====
        ivSearchFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite = !isFavorite; // トグル

                if (isFavorite) {
                    ivSearchFavorite.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else {
                    ivSearchFavorite.setImageResource(R.drawable.selector_heart_checkbox);
                }
            }
        });

        // ===== ボタンの機能設定 =====
        Button btSearchCancel = dialogView.findViewById(R.id.btSearchCancel);
        btSearchCancel.setOnClickListener(new CancelClickListener());

        Button btSearchSearch = dialogView.findViewById(R.id.btSearchSearch);
        btSearchSearch.setOnClickListener(new SearchClickListener());

        for (int i = 0; i < stars.length; i++) {
            stars[i].setOnClickListener(new StarClickListener(i));
        }

        AlertDialog dialog = builder.create();
        return dialog;

    }

    // ===== ダイアログ表示時のレイアウト調整 =====
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }


    // ===== 検索ボタンのクリックリスナー =====
    private class SearchClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            String keyword = etSearchKeyword.getText().toString();
            String prefecture = spSearchPrefecture.getSelectedItem().toString();
            String area = etSearchArea.getText().toString();
            String genre = spSearchGenre.getSelectedItem().toString();
            int ratingInt = rating; // ★クリックで設定された数字をそのまま送る

            Boolean visited = cbSearchVisited.isChecked();
            Boolean want = cbSearchWant.isChecked();
            Boolean favorite = isFavorite;

            Intent intent = new Intent(getActivity(), ResultActivity.class);

            Bundle bundle = new Bundle();
            bundle.putString("keyword", keyword);
            bundle.putString("prefecture", prefecture);
            bundle.putString("area", area);
            bundle.putString("genre", genre);
            bundle.putInt("rating", ratingInt);
            bundle.putBoolean("visited", visited);
            bundle.putBoolean("want", want);
            bundle.putBoolean("favorite", favorite);

            intent.putExtras(bundle);
            startActivity(intent);

            dismiss();  //ダイアログを閉じる

        }
    }

    // ===== キャンセルのクリックリスナー =====
    private class CancelClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            dismiss();
        }
    }


    // ===== ☆の塗りつぶしの設定 =====
    private void updateStars() {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_baseline_star_24); // 塗りつぶし
            } else {
                stars[i].setImageResource(R.drawable.ic_baseline_star_border_24); // 空
            }
        }
    }

    // ===== ☆のクリックリスナーの設定 =====
    private class StarClickListener implements View.OnClickListener {

        private int index; // 何番目の星か（0～4）

        public StarClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            if (rating == index + 1) {
                // 同じ星をもう一度押したら解除
                rating = 0;
            } else {
                rating = index + 1;
            }
            updateStars();
        }
    }
}


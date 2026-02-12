package com.example.myfoodreview0202;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // データベースヘルパー
    private DatabaseHelper _helper;
    // メインの垂直リスト
    private RecyclerView verticalRecyclerView;
    // 表示モード切替ボタン
    private Button btnSwitchMode;
    // 写真追加ボタン
    private FloatingActionButton fabAdd;
    // 現在の表示モード (true: ジャンル, false: 地域)
    private boolean isGenreMode = true;
    // 複数選択モードフラグ
    private boolean isSelectionMode = false;
    // 選択されたアイテムのIDリスト
    private List<Integer> selectedIds = new ArrayList<>();
    // 地域表示の階層 (0:都道府県, 1:エリア)
    private int locationLevel = 0;
    // 選択中の都道府県
    private String selectedPrefecture = "";
    // アクションバー中央に表示するロゴ
    private ImageView logoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- アクションバーのカスタマイズ ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowCustomEnabled(true);

            logoView = new ImageView(this);
            logoView.setImageResource(R.drawable.splash_logo);

            int height = (int) (40 * getResources().getDisplayMetrics().density);
            androidx.appcompat.app.ActionBar.LayoutParams params = new androidx.appcompat.app.ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    height,
                    android.view.Gravity.CENTER
            );
            getSupportActionBar().setCustomView(logoView, params);

            // 初回計算
            adjustLogoToCenter();
        }

        // --- ビューの紐付け ---
        _helper = new DatabaseHelper(MainActivity.this);
        verticalRecyclerView = findViewById(R.id.main_verticalRecyclerView);
        btnSwitchMode = findViewById(R.id.main_btnSwitchMode);
        fabAdd = findViewById(R.id.main_fabAdd);

        // レイアウト設定
        verticalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // リスト初期表示
        refreshList();

        // --- リスナー設定 ---

        // モード切替ボタン
        btnSwitchMode.setOnClickListener(v -> {
            if (!isGenreMode && locationLevel == 1) {
                locationLevel = 0;
                selectedPrefecture = "";
            } else {
                isGenreMode = !isGenreMode;
                locationLevel = 0;
                selectedPrefecture = "";
            }
            refreshList();
        });

        // 複数選択キャンセルボタン
        findViewById(R.id.main_btnSelectionCancel).setOnClickListener(v -> {
            setSelectionMode(false);
        });

        // 一括削除ボタン
        findViewById(R.id.main_btnSelectionDelete).setOnClickListener(v -> {
            int count = selectedIds.size();
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("一括削除")
                    .setMessage(count + "件のデータを削除しますか？")
                    .setPositiveButton("削除", (dialog, which) -> {
                        SQLiteDatabase dbWrite = _helper.getWritableDatabase();
                        dbWrite.beginTransaction();
                        try {
                            for (Integer id : selectedIds) {
                                dbWrite.delete("myfoodreviews", "id = ?", new String[]{String.valueOf(id)});
                            }
                            dbWrite.setTransactionSuccessful();
                        } finally {
                            dbWrite.endTransaction();
                            dbWrite.close();
                        }
                        Toast.makeText(MainActivity.this, count + "件削除しました", Toast.LENGTH_SHORT).show();
                        setSelectionMode(false);
                        refreshList();
                    })
                    .setNegativeButton("キャンセル", null)
                    .show();
        });

        // お気に入り一括登録
        findViewById(R.id.main_btnSelectionFavorite).setOnClickListener(v -> {
            int count = selectedIds.size();
            SQLiteDatabase db = _helper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put("is_favorite", 1);
                for (Integer id : selectedIds) {
                    db.update("myfoodreviews", values, "id = ?", new String[]{String.valueOf(id)});
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }
            Toast.makeText(MainActivity.this, count + "件をお気に入りに登録しました", Toast.LENGTH_SHORT).show();
            setSelectionMode(false);
            refreshList();
        });

        fabAdd.setOnClickListener(new FabAddClickListener());
    } // onCreate ここまで

    /**
     * ロゴを画面の物理的な中央に強制配置するメソッド
     */
    private void adjustLogoToCenter() {
        if (logoView == null) return;
        logoView.post(() -> {
            androidx.appcompat.widget.Toolbar parent = (androidx.appcompat.widget.Toolbar) logoView.getParent();
            if (parent != null) {
                parent.setContentInsetsAbsolute(0, 0);
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int logoWidth = logoView.getWidth();
                int targetLeft = (screenWidth / 2) - (logoWidth / 2);

                int[] location = new int[2];
                logoView.getLocationOnScreen(location);
                int currentLeft = location[0];

                float translationX = targetLeft - currentLeft;
                // 現在のズレを考慮してセットし直す
                logoView.setTranslationX(logoView.getTranslationX() + translationX);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_options_main, menu);
        // アイコン表示の直後に位置を再計算
        adjustLogoToCenter();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.menu_main_search) {
            showSearchDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<GenreModel> dataList;
        if (isGenreMode) {
            btnSwitchMode.setText("地 域");
            btnSwitchMode.setVisibility(View.VISIBLE);
            dataList = getGenreData();
        } else {
            btnSwitchMode.setVisibility(View.VISIBLE);
            btnSwitchMode.setText(locationLevel == 1 ? "戻る" : "ジャンル");
            dataList = getLocationData();
        }
        verticalRecyclerView.setAdapter(new VerticalAdapter(dataList));
    }

    private List<GenreModel> getGenreData() {
        List<GenreModel> list = new ArrayList<>();
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql = "SELECT id, genre, is_favorite FROM myfoodreviews WHERE shop_name IS NOT NULL AND shop_name != '' ORDER BY id DESC";
        Cursor cursor = db.rawQuery(sql, null);
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                genre = (genre != null) ? genre.trim() : "";
                int isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));
//                String path = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));

                //20260210松村変更
                // --- ここで photos テーブルからメイン写真(sort_order=0)を取得 ---
                String path = null;
                Cursor pCursor = db.rawQuery("SELECT photo_path FROM photos WHERE review_id = ? AND sort_order = 0", new String[]{String.valueOf(id)});
                if (pCursor.moveToFirst()) {
                    path = pCursor.getString(0);
                }
                pCursor.close();
                // ----------------------------------------------------------

                PhotoModel photo = new PhotoModel(id, path, isFav);
                boolean found = false;
                for (GenreModel model : list) {
                    if (model.getGenreName().equals(genre)) {
                        model.getPhotoList().add(photo);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    List<PhotoModel> photoList = new ArrayList<>();
                    photoList.add(photo);
                    list.add(new GenreModel(genre, photoList));
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    private List<GenreModel> getLocationData() {
        List<GenreModel> list = new ArrayList<>();
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql;
        String[] args = null;
        if (locationLevel == 0) {
            sql = "SELECT id, prefecture, area, is_favorite FROM myfoodreviews WHERE shop_name IS NOT NULL AND shop_name != '' ORDER BY id DESC";
        } else {
            sql = "SELECT id, prefecture, area, is_favorite FROM myfoodreviews WHERE prefecture = ? AND shop_name IS NOT NULL AND shop_name != '' ORDER BY id DESC";
            args = new String[]{selectedPrefecture};
        }
        Cursor cursor = db.rawQuery(sql, args);
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String rawPref = cursor.getString(cursor.getColumnIndexOrThrow("prefecture"));
                String rawArea = cursor.getString(cursor.getColumnIndexOrThrow("area"));
//                String path = cursor.getString(cursor.getColumnIndexOrThrow("photo_path"));
                int isFav = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));

                //20260210松村変更
                // --- ここで photos テーブルからメイン写真を取得 ---
                String path = null;
                Cursor pCursor = db.rawQuery("SELECT photo_path FROM photos WHERE review_id = ? AND sort_order = 0", new String[]{String.valueOf(id)});
                if (pCursor.moveToFirst()) {
                    path = pCursor.getString(0);
                }
                pCursor.close();
                // ----------------------------------------------

                String label = (locationLevel == 0) ?
                        (rawPref != null && !rawPref.trim().isEmpty() ? rawPref.trim() : "未設定") :
                        (rawArea != null && !rawArea.trim().isEmpty() ? rawArea.trim() : "その他エリア");

                PhotoModel photo = new PhotoModel(id, path, isFav);
                boolean found = false;
                for (GenreModel model : list) {
                    if (model.getGenreName().equals(label)) {
                        model.getPhotoList().add(photo);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    List<PhotoModel> photoList = new ArrayList<>();
                    photoList.add(photo);
                    list.add(new GenreModel(label, photoList));
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public void setSelectionMode(boolean enabled) {
        this.isSelectionMode = enabled;
        View llSelectionBar = findViewById(R.id.main_llSelectionBar);
        if (enabled) {
            if (llSelectionBar != null) llSelectionBar.setVisibility(View.VISIBLE);
            fabAdd.hide();
            btnSwitchMode.setVisibility(View.GONE);
        } else {
            if (llSelectionBar != null) llSelectionBar.setVisibility(View.GONE);
            fabAdd.show();
            btnSwitchMode.setVisibility(View.VISIBLE);
            selectedIds.clear();
        }
        if (verticalRecyclerView.getAdapter() != null) {
            verticalRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public void toggleSelection(int shopId) {
        if (selectedIds.contains(shopId)) {
            selectedIds.remove(Integer.valueOf(shopId));
        } else {
            selectedIds.add(shopId);
        }
        if (selectedIds.isEmpty()) {
            setSelectionMode(false);
        } else {
            verticalRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    // --- 内部クラス (Model/Adapter) ---

    public static class GenreModel {
        private String genreName;
        private List<PhotoModel> photoList;
        public GenreModel(String genreName, List<PhotoModel> photoList) {
            this.genreName = genreName;
            this.photoList = photoList;
        }
        public String getGenreName() { return genreName; }
        public List<PhotoModel> getPhotoList() { return photoList; }
    }

    public static class PhotoModel {
        private int shopId;
        private String photoPath;
        private int isFavorite;
        public PhotoModel(int shopId, String photoPath, int isFavorite) {
            this.shopId = shopId;
            this.photoPath = photoPath;
            this.isFavorite = isFavorite;
        }
        public int getShopId() { return shopId; }
        public String getPhotoPath() { return photoPath; }
        public int getIsFavorite() { return isFavorite; }
    }

    private class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.VerticalViewHolder> {
        private List<GenreModel> genreList;
        public VerticalAdapter(List<GenreModel> genreList) { this.genreList = genreList; }
        @NonNull
        @Override
        public VerticalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_genre, parent, false);
            return new VerticalViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull VerticalViewHolder holder, int position) {
            GenreModel genre = genreList.get(position);
            holder.textGenreName.setText(genre.getGenreName());
            holder.btnMore.setOnClickListener(v -> {
                if (isGenreMode || locationLevel == 1) {
                    Intent intent = new Intent(v.getContext(), ResultActivity.class);
                    if (isGenreMode) {
                        intent.putExtra("genre", genre.getGenreName().equals("未設定") ? "ジャンル未設定" : genre.getGenreName());
                    } else {
                        intent.putExtra("prefecture", selectedPrefecture);
                        intent.putExtra("area", genre.getGenreName());
                    }
                    intent.putExtra("FROM_MAIN_LIST", true);
                    v.getContext().startActivity(intent);
                } else {
                    String pref = genre.getGenreName();
                    SQLiteDatabase db = _helper.getReadableDatabase();
                    Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM myfoodreviews WHERE prefecture = ? AND area IS NOT NULL AND area != ''", new String[]{pref});
                    cursor.moveToFirst();
                    int count = cursor.getInt(0);
                    cursor.close();
                    if (count > 0) {
                        selectedPrefecture = pref;
                        locationLevel = 1;
                        refreshList();
                    } else {
                        Intent intent = new Intent(v.getContext(), ResultActivity.class);
                        intent.putExtra("prefecture", pref);
                        intent.putExtra("FROM_MAIN_LIST", true);
                        v.getContext().startActivity(intent);
                    }
                }
            });
            holder.horizontalRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            holder.horizontalRecyclerView.setAdapter(new HorizontalAdapter(genre.getPhotoList()));
        }
        @Override
        public int getItemCount() { return genreList.size(); }
        class VerticalViewHolder extends RecyclerView.ViewHolder {
            TextView textGenreName;
            RecyclerView horizontalRecyclerView;
            Button btnMore;
            public VerticalViewHolder(View iv) {
                super(iv);
                textGenreName = iv.findViewById(R.id.textGenreName);
                horizontalRecyclerView = iv.findViewById(R.id.horizontalRecyclerView);
                btnMore = iv.findViewById(R.id.btnMore);
            }
        }
    }

    private class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.PhotoViewHolder> {
        private List<PhotoModel> photoList;
        public HorizontalAdapter(List<PhotoModel> photoList) { this.photoList = photoList; }
        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            PhotoModel photo = photoList.get(position);
            // 画像パスが null か空なら、リソースID (noimage) を、あればパスを load に渡す
            Object loadTarget = (photo.getPhotoPath() == null || photo.getPhotoPath().isEmpty())
                    ? R.drawable.noimage
                    : photo.getPhotoPath();

            holder.ivFavorite.setVisibility(photo.getIsFavorite() == 1 ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> {
                if (isSelectionMode) toggleSelection(photo.getShopId());
                else {
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    intent.putExtra("SHOP_ID", photo.getShopId());
                    v.getContext().startActivity(intent);
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                if (!isSelectionMode) { setSelectionMode(true); toggleSelection(photo.getShopId()); }
                return true;
            });
            if (isSelectionMode && selectedIds.contains(photo.getShopId())) {
                holder.imagePhoto.setAlpha(0.4f);
                holder.itemView.setBackgroundColor(android.graphics.Color.LTGRAY);
            } else {
                holder.imagePhoto.setAlpha(1.0f);
                holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
            Glide.with(holder.imagePhoto.getContext())
                    .load(loadTarget)                    // 画像パス
                    .placeholder(R.drawable.noimage)             // プレースホルダー
                    .transform(new CenterCrop(), new RoundedCorners(16)) // 角丸変換
                    .into(holder.imagePhoto);                     // 最後に into()
        }
        @Override
        public int getItemCount() { return photoList.size(); }
        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imagePhoto, ivFavorite;
            public PhotoViewHolder(View iv) {
                super(iv);
                imagePhoto = iv.findViewById(R.id.imagePhoto);
                ivFavorite = iv.findViewById(R.id.ivMainFavorite);
            }
        }
    }

    private class FabAddClickListener implements View.OnClickListener {
        @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, AddActivity.class)); }
    }

    private void showSearchDialog() {
        new SearchDialogFragment().show(getSupportFragmentManager(), "SearchDialogFragment");
    }
}
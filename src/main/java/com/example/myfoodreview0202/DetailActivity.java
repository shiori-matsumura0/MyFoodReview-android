package com.example.myfoodreview0202;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private DatabaseHelper _helper;
    private TextView tvDetailShopName;
    private TextView tvDetailGenre;
    private TextView tvDetailVisitedDate;
    private TextView tvDetailPrefecture;
    private TextView tvDetailArea;
    private TextView tvDetailUrl;
    private TextView tvDetailMemo;
    private boolean isFavorite;
    private MenuItem menuFavorite;
    private int DetailId;
    private String shopId;
    private ImageView[] stars;
    private Chip chipVisitStatus;
    private ViewPager2 vpDetailPhotos;
    private TabLayout tlDetailIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("レビュー詳細");

        // 戻る「←」ボタンの表示
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // MainActivityからidの受け取り
        Intent intent = getIntent();
        DetailId = intent.getIntExtra("SHOP_ID", -1);
        if (DetailId == -1) {
            finish();
            return;
        }
        shopId = String.valueOf(DetailId);

        _helper = new DatabaseHelper(DetailActivity.this);

        // ===== 画面部品取得 =====
        tvDetailShopName = findViewById(R.id.tvDetailShopName);
        tvDetailGenre = findViewById(R.id.tvDetailGenre);
        tvDetailVisitedDate = findViewById(R.id.tvDetailVisitedDate);
        vpDetailPhotos = findViewById(R.id.vpDetailPhotos);
        tvDetailPrefecture = findViewById(R.id.tvDetailPrefecture);
        tvDetailArea = findViewById(R.id.tvDetailArea);
        tvDetailUrl = findViewById(R.id.tvDetailUrl);
        tvDetailMemo = findViewById(R.id.tvDetailMemo);
        chipVisitStatus = findViewById(R.id.chipVisitStatus);
        tlDetailIndicator = findViewById(R.id.tlDetailIndicator);

        loadDetailFromDB();
        loadFavoriteState();
        tvDetailUrl.setOnClickListener(new UrlClickListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetailFromDB(); // UpdateActivity から戻ったときも再読み込み
        loadFavoriteState();   // ★お気に入り再読み込み
        updateFavoriteIcon();  // ★アイコン更新
    }

    // ===== オプションメニューを設置 =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_detail, menu);
        menuFavorite = menu.findItem(R.id.menu_detail_favorite);
        updateFavoriteIcon();
        return true;
    }

    // ===== オプションメニューの設定 =====
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemId = item.getItemId();

        // お気に入りボタン
        if (itemId == R.id.menu_detail_favorite) {
            onFavoriteClicked();
            return true;
        }

        // 編集ボタン
        else if (itemId == R.id.menu_detail_edit) {
            Intent intent = new Intent(DetailActivity.this, UpdateActivity.class);
            intent.putExtra("SHOP_ID", DetailId); // idを渡す
            startActivity(intent);
            return true;
        }

        // 削除ボタン
        else if (itemId == R.id.menu_detail_delete) {
            DeleteConfirmDialogFragment dialogFragment = new DeleteConfirmDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "DeleteConfirmDialogFragment");
            return true;
        }

        // 戻るボタン
        else if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadDetailFromDB() {
        // ===== データベースの接続と情報の抜き取り =====
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql = "SELECT * FROM myfoodreviews WHERE id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{shopId});
        if(cursor.moveToFirst()){
            // 店名
            int idxShopName = cursor.getColumnIndex("shop_name");
            String shopName = cursor.getString(idxShopName);
            tvDetailShopName.setText(shopName);

            // ジャンル
            int idxGenre = cursor.getColumnIndex("genre");
            String genre = cursor.getString(idxGenre);

            // 「選択してください」の場合は「未選択」に変換
            if ("選択してください".equals(genre)) {
                genre = "未選択";
            }

            tvDetailGenre.setText(genre);

            // 訪問日
            int idxVisitedDate = cursor.getColumnIndex("visited_date");
            String visitedDate = cursor.getString(idxVisitedDate);
            visitedDate = formatVisitedDate(visitedDate);
            tvDetailVisitedDate.setText(visitedDate);


            // --- 写真読み込みロジックを photos テーブルへ変更 ---
            loadPhotosFromDB();

            // 都道府県
            int idxPrefecture = cursor.getColumnIndex("prefecture");
            String prefecture = cursor.getString(idxPrefecture);

            // 「選択してください」の場合は「未選択」に変換
            if ("選択してください".equals(prefecture)) {
                prefecture = "未選択";
            }
            tvDetailPrefecture.setText(prefecture);

            // エリア
            int idxArea = cursor.getColumnIndex("area");
            String area = cursor.getString(idxArea);
            tvDetailArea.setText(area);

            // URL
            int idxUrl = cursor.getColumnIndex("url");
            String url = cursor.getString(idxUrl);
            tvDetailUrl.setText(url);

            // メモ
            int idxMemo = cursor.getColumnIndex("memo");
            String memo = cursor.getString(idxMemo);
            tvDetailMemo.setText(memo);

            // 行った or 行ってない
            int idxWant = cursor.getColumnIndex("want_to_go");
            int want = cursor.getInt(idxWant);

            if (want == 0) {
                // 行った → Chip 非表示
                chipVisitStatus.setVisibility(View.GONE);
            } else {
                // 行ってみたい → Chip 表示
                chipVisitStatus.setVisibility(View.VISIBLE);
                chipVisitStatus.setText("行きたい");
            }

            // お気に入り
            stars = new ImageView[]{
                    findViewById(R.id.ivDetailStar1),
                    findViewById(R.id.ivDetailStar2),
                    findViewById(R.id.ivDetailStar3),
                    findViewById(R.id.ivDetailStar4),
                    findViewById(R.id.ivDetailStar5)
            };

            int idxRating = cursor.getColumnIndex("rating");
            if (idxRating != -1) {
                int rating = cursor.getInt(idxRating);
                setRatingStars(rating);
            } else {
                // rating カラムがない場合のデフォルト値
                setRatingStars(0);
            }
        }

        cursor.close();
    }

    private void loadPhotosFromDB() {
        List<String> currentPaths = new ArrayList<>();

        SQLiteDatabase db = _helper.getReadableDatabase();

        String sqlPhotos = "SELECT photo_path FROM photos WHERE review_id = ? ORDER BY sort_order ASC";
        Cursor pCursor = db.rawQuery(sqlPhotos, new String[]{shopId});

        while (pCursor.moveToNext()) {
            String path = pCursor.getString(pCursor.getColumnIndexOrThrow("photo_path"));
            if (path != null && !path.isEmpty()) {
                currentPaths.add(path); // ★ パスをリストに追加していく
            }
        }
        pCursor.close();

        // 写真が1枚もない場合は noimage を1つ表示するために空文字を入れる
        if (currentPaths.isEmpty()) {
            currentPaths.add("");
        }

        PhotoPagerAdapter adapter = new PhotoPagerAdapter(currentPaths);
        vpDetailPhotos.setAdapter(adapter);

        //20260212 追記
        // 2枚以上あるときだけインジケーターを表示する
        if (currentPaths.size() > 1) {
            tlDetailIndicator.setVisibility(View.VISIBLE);
            new TabLayoutMediator(tlDetailIndicator, vpDetailPhotos, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    // ドット用なので空でOK
                }
            }).attach();
        } else {
            // 1枚以下のときは隠す
            tlDetailIndicator.setVisibility(View.GONE);
        }


    }

    // ===== ☆の塗りつぶしの設定 =====
    private void setRatingStars(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_baseline_star_24);  // 塗りつぶし星
            } else {
                stars[i].setImageResource(R.drawable.ic_baseline_star_border_24);  // 白い星
            }
        }
    }

    // ===== お気に入りクリック =====
    public void onFavoriteClicked() {
        toggleFavorite();
        updateFavoriteIcon();
    }

    // ===== お気に入りON/OFFのデータベースへの設定 =====
    private void toggleFavorite() {
        isFavorite = !isFavorite;

        SQLiteDatabase db = _helper.getWritableDatabase();
        String sqlUpdate = "UPDATE myfoodreviews SET is_favorite = ? WHERE id = ?";
        SQLiteStatement stmt = db.compileStatement(sqlUpdate);
        stmt.bindLong(1, isFavorite ? 1:0);
        stmt.bindLong(2, DetailId);
        stmt.executeUpdateDelete();
    }

    // ===== データベースからお気に入りの検索 =====
    private void loadFavoriteState() {
        SQLiteDatabase db = _helper.getReadableDatabase();
        String sql = "SELECT is_favorite FROM myfoodreviews WHERE id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{shopId});

        if (cursor.moveToFirst()) {
            isFavorite = cursor.getInt(0) == 1;
        }

        cursor.close();
    }

    // ===== お気に入りON/OFFのビューの設定 =====
    private void updateFavoriteIcon() {
        if (menuFavorite == null) {
            return;
        }
        if (isFavorite) {
            menuFavorite.setIcon(R.drawable.ic_baseline_favorite_24);
        } else {
            menuFavorite.setIcon(R.drawable.ic_baseline_favorite_border_24);
        }
    }


    // ===== 削除メソッド =====
    public void deleteReview(){
        SQLiteDatabase db = _helper.getWritableDatabase();
        String sqlDeletePhotos = "DELETE FROM photos WHERE review_id = ?";
        SQLiteStatement stmtPhotos = db.compileStatement(sqlDeletePhotos);
        stmtPhotos.bindLong(1, DetailId);
        stmtPhotos.executeUpdateDelete();

        // 2. myfoodreviews テーブルから本体を削除
        String sqlDeleteReview = "DELETE FROM myfoodreviews WHERE id = ?";
        SQLiteStatement stmtReview = db.compileStatement(sqlDeleteReview);
        stmtReview.bindLong(1, DetailId);
        stmtReview.executeUpdateDelete();
    }

    // ===== URLクリック =====
    private class UrlClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String url = tvDetailUrl.getText().toString();

            if (url == null || url.isEmpty()) return;

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    // ===== 訪問日フォーマット統一 =====
    private String formatVisitedDate(String date) {
        if (date == null) return "";

        // 20260202 → 2026/02/02
        if (date.matches("\\d{8}")) {
            return date.substring(0, 4) + "/"
                    + date.substring(4, 6) + "/"
                    + date.substring(6, 8);
        }

        // すでに yyyy-MM-dd の場合はそのまま
        return date;
    }

    //20260212松村追加
    private class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {
        private List<String> paths;

        PhotoPagerAdapter(List<String> paths) {
            this.paths = paths;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // プログラムから ImageView を作成
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new PhotoViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            String path = paths.get(position);
            if (path == null || path.isEmpty()) {
                holder.imageView.setImageResource(R.drawable.noimage);
            } else {
                Glide.with(holder.imageView.getContext())
                        .load(new File(path))
                        .into(holder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return paths.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            PhotoViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
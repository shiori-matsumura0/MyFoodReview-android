package com.example.myfoodreview0202;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 検索結果を表示する画面。
 * 検索条件に基づくDBクエリの実行、ソート機能、および複数選択による一括操作を提供します。
 */
public class ResultActivity extends AppCompatActivity {

    private String currentSort = "created_desc"; // デフォルト：作成日新しい順

    // ===== 検索条件を保持する変数群 =====
    private String keyword = "";
    private String prefecture = "";
    private String area = "";
    private String genre = "";
    private int rating = 0;
    private boolean visited = false;
    private boolean want = false;
    private boolean favorite = false;
    private RecyclerView rvResult;

    // 検索条件表示バーのUIコンポーネント
    private ConstraintLayout layoutSearchCondition;
    private TextView tvDebug;
    private ImageView ivResultCancel;

    // 20260205山本:複数選択と一括処理に対応する為の変数を用意しています。
    private boolean isSelectionMode = false;      // 複数選択モード中かどうかのフラグ
    private LinearLayout layoutSelectionMenu;      // 下から出るメニューバー
    private List<Integer> selectedIds = new ArrayList<>(); // 選択されたアイテムのIDリスト

    /**
     * 画面生成時の初期化処理。
     * Intentからのパラメータ取得、UIの構築、各リスナーの設定を行います。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setTitle("検索結果");

        // ===== アクションバーの戻るボタン有効化 =====
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // ===== 前画面（検索ダイアログやメイン画面）から渡された条件を取得 =====
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            keyword = bundle.getString("keyword", "");
            prefecture = bundle.getString("prefecture", "");
            area = bundle.getString("area", "");
            genre = bundle.getString("genre", "");
            rating = bundle.getInt("rating", 0);
            visited = bundle.getBoolean("visited", false);
            want = bundle.getBoolean("want", false);
            favorite = bundle.getBoolean("favorite", false);
        }

        // ===== RecyclerView 設定 =====
        rvResult = findViewById(R.id.rvResult);
        rvResult.setLayoutManager(new GridLayoutManager(this, 3)); // 3列グリッド表示
        rvResult.setHasFixedSize(true);

        //ここから山本追記
        // ダイアログで設定可能な全項目をチェックし、有効な条件のみを表示します。
        layoutSearchCondition = findViewById(R.id.layoutSearchCondition);
        tvDebug = findViewById(R.id.tvDebug);
        ivResultCancel = findViewById(R.id.ivResultCancel);
        List<String> conditions = new ArrayList<>();

        // 文字入力・選択系の項目をリストに追加
        if (!keyword.isEmpty()) conditions.add(keyword);
        if (!genre.isEmpty() && !genre.equals("未選択")) conditions.add(genre);
        if (!prefecture.isEmpty() && !prefecture.equals("未選択")) conditions.add(prefecture);
        if (!area.isEmpty()) conditions.add(area);
        if (rating > 0) {
            conditions.add("★" + rating);
        }

        // チェックボックス系の項目をリストに追加
        if (visited) conditions.add("行った");
        if (want) conditions.add("行きたい");
        if (favorite) conditions.add("お気に入り");

        // 条件がある場合のみ上部バーを表示
        if (!conditions.isEmpty()) {
            // 全ての条件を「 / 」で連結して表示
            tvDebug.setText("検索条件：" + android.text.TextUtils.join(" / ", conditions));
            layoutSearchCondition.setVisibility(View.VISIBLE);
        } else {
            layoutSearchCondition.setVisibility(View.GONE);
        }
        //追記箇所ここまで

        // --- 20260206山本追記: 検索条件バーの設定 ---
        layoutSearchCondition = findViewById(R.id.layoutSearchCondition);
        tvDebug = findViewById(R.id.tvDebug);
        ivResultCancel = findViewById(R.id.ivResultCancel);
        // (中略：条件テキストの生成処理)
        ivResultCancel.setOnClickListener(new ClearButtonClickListener());

        // --- 20260206山本修正: MainActivityと複数選択メニューの設定 ---
        // XML側で定義した新しいID（main_llSelectionBar等）を使用して紐付けを行います。
        layoutSelectionMenu = findViewById(R.id.result_llSelectionBar);
        Button btnBulkFavorite = findViewById(R.id.result_btnSelectionFavorite);
        Button btnBulkDelete = findViewById(R.id.result_btnSelectionDelete);
        Button btnCancelSelection = findViewById(R.id.result_btnSelectionCancel);

        // 一括お気に入り登録ボタンのリスナー
        btnBulkFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performBulkFavorite();
            }
        });

        // 一括削除ボタンのリスナー（確認ダイアログ表示）
        btnBulkDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBulkDeleteDialog();
            }
        });

        // 選択モード解除（戻る）ボタンのリスナー
        btnCancelSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectionMode(false);
            }
        });

        ivResultCancel.setOnClickListener(new ClearButtonClickListener());
        // 初期検索の実行
        loadResultList();
    }

    /**
     * 画面復帰時にリストを再読み込みし、詳細画面での変更を反映させます。
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadResultList();
    }

    // ===== オプションメニューを設置 =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_result, menu);
        return true;
    }

    // ===== オプションメニューの設定 =====
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // 検索ボタン
        if (itemId == R.id.menu_result_search) {
            showSearchDialog();
            return true;
        }

        // ソートボタン
        if (itemId == R.id.menu_result_sort) {
            View anchor = findViewById(R.id.menu_result_sort);
            PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);

            popup.getMenu().add("訪問日（新しい順）");
            popup.getMenu().add("訪問日（古い順）");
            popup.getMenu().add("作成日（新しい順）");
            popup.getMenu().add("作成日（古い順）");

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    String title = menuItem.getTitle().toString();
                    switch (title) {
                        case "訪問日（新しい順）":
                            currentSort = "visited_desc";
                            break;
                        case "訪問日（古い順）":
                            currentSort = "visited_asc";
                            break;
                        case "作成日（新しい順）":
                            currentSort = "created_desc";
                            break;
                        case "作成日（古い順）":
                            currentSort = "created_asc";
                            break;
                    }

                    loadResultList(); // 再読み込み
                    return true;
                }
            });

            popup.show();
            return true;
        }

        // 戻るボタン
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ===== 検索画面の設定 =====
    private void showSearchDialog() {
        SearchDialogFragment dialogFragment = new SearchDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "SearchDialogFragment");
    }

    // ===== 検索条件クリアの設定 =====
    private class ClearButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            // 検索条件のクリア
            keyword = "";
            prefecture = "";
            area = "";
            genre = "";
            rating = 0;
            visited = false;
            want = false;
            favorite = false;

            // 検索条件バーを非表示
            layoutSearchCondition.setVisibility(View.GONE);
            tvDebug.setText("");

            // RecyclerView を全件表示に戻す
            loadResultList();
        }
    }

    // 20260206山本変更:データベースが空文字だった場合に検索結果画面で「その他エリア」が表示されない問題に対応
    /**
     * 現在の検索条件に基づいてDBを検索し、RecyclerViewのアダプターを更新します。
     * SQLのWHERE句を動的に生成しています。
     */
    private void loadResultList() {
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, is_favorite FROM myfoodreviews WHERE 1=1 ");

        List<String> args = new ArrayList<>();

        // --- 1. ジャンル判定 ---
        if (genre != null && !genre.isEmpty() && !genre.equals("未選択")) {
            if (genre.equals("ジャンル未設定")) {
                sql.append(" AND (genre IS NULL OR genre = '')");
            } else {
                sql.append(" AND genre = ?");
                args.add(genre);
            }
        }

        //エリア判定 (スペース混じりの空文字にも対応) ---
        if (area != null && !area.isEmpty()) {
            if (area.equals("その他エリア") || area.equals("エリア未設定")) {
                // 20260206山本修正：
                // 1. area自体がNULLである
                // 2. trim(area)の結果が空文字（スペースのみの場合も含む）である
                // この両方を「その他エリア」としてヒットさせます
                sql.append(" AND (area IS NULL OR trim(area) = '')");
            } else {
                // 具体的な地名が入っている場合
                sql.append(" AND area = ?");
                args.add(area);
            }
        }

        // 都道府県判定 (同様にスペース対応) ---
        if (prefecture != null && !prefecture.isEmpty() && !prefecture.equals("未選択")) {
            if (prefecture.equals("未設定") || prefecture.equals("都道府県未設定")) {
                sql.append(" AND (prefecture IS NULL OR trim(prefecture) = '')");
            } else {
                sql.append(" AND prefecture = ?");
                args.add(prefecture);
            }
        }

        // --- 4. キーワード検索（店名・メモ） ---
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (shop_name LIKE ? OR memo LIKE ?)");
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }

        if (rating > 0) {
            sql.append(" AND rating >= ?");
            args.add(String.valueOf(rating));
        }
        if (visited) sql.append(" AND want_to_go = 0");
        if (want) sql.append(" AND want_to_go = 1");
        if (favorite) sql.append(" AND is_favorite = 1");

        // ソート処理
        if ("visited_desc".equals(currentSort)) sql.append(" ORDER BY visited_date DESC");
        else if ("visited_asc".equals(currentSort)) sql.append(" ORDER BY visited_date ASC");
        else if ("created_desc".equals(currentSort)) sql.append(" ORDER BY created_at DESC");
        else if ("created_asc".equals(currentSort)) sql.append(" ORDER BY created_at ASC");

        Cursor cursor = db.rawQuery(sql.toString(), args.toArray(new String[0]));

        List<MyFoodReview> resultList = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite"));

            String path = null;
            Cursor pCursor = db.rawQuery(
                    "SELECT photo_path FROM photos WHERE review_id = ? AND sort_order = 0",
                    new String[]{String.valueOf(id)}
            );

            if (pCursor.moveToFirst()) {
                path = pCursor.getString(0);
            }
            pCursor.close();

            resultList.add(new MyFoodReview(id, path, isFavorite));
        }
        cursor.close();
        db.close();

        // アダプターへ反映
        ResultAdapter adapter = new ResultAdapter(resultList);
        rvResult.setAdapter(adapter);
    }

    // ===== データクラス =====
    /**
     * リスト表示に必要な最小限のデータを保持する内部クラス。
     */
    public static class MyFoodReview {
        public int id;
        public String photoPath;
        public int isFavorite;

        public MyFoodReview(int id, String photoPath, int isFavorite) {
            this.id = id;
            this.photoPath = photoPath;
            this.isFavorite = isFavorite;
        }
    }

    // ===== Adapter =====
    /**
     * 検索結果リスト（写真グリッド）を制御するアダプター。
     */
    public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

        private List<MyFoodReview> list;

        public ResultAdapter(List<MyFoodReview> list) {
            this.list = list;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPhoto;
            ImageView ivResultFavorite;

            ViewHolder(View view) {
                super(view);
                ivPhoto = view.findViewById(R.id.ivPhoto);
                ivResultFavorite = view.findViewById(R.id.ivResultFavorite);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_result, parent, false);
            return new ViewHolder(view);
        }


        /**
         * 各アイテムの描画処理。
         * 画像のロード、お気に入りアイコンの表示、選択モード時の外観変更およびクリックイベントを設定します。
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MyFoodReview item = list.get(position);

            // 画像セット
            if (item.photoPath != null && !item.photoPath.isEmpty()) {
//                try {
//                    Uri uri = Uri.parse(item.photoPath);
//                    holder.ivPhoto.setImageURI(uri);
//                } catch (Exception e) {
//                    holder.ivPhoto.setImageResource(R.drawable.noimage);
//                }
                Glide.with(holder.ivPhoto.getContext())
                        .load(item.photoPath) // 画像パス
                        .placeholder(R.drawable.noimage) // 読み込み中の画像
                        .into(holder.ivPhoto); // ImageView にセット
            } else {
                holder.ivPhoto.setImageResource(R.drawable.noimage);
            }
            if (item.isFavorite == 1) {
                holder.ivResultFavorite.setVisibility(View.VISIBLE);
            } else {
                holder.ivResultFavorite.setVisibility(View.GONE);
            }

//            if (item.photoPath != null && !item.photoPath.isEmpty()) {
//                File imgFile = new File(item.photoPath);
//                if (imgFile.exists()) {
//                    holder.ivPhoto.setImageURI(Uri.fromFile(imgFile));
//                } else {
//                    holder.ivPhoto.setImageResource(R.drawable.noimage);
//                }
//            } else {
//                holder.ivPhoto.setImageResource(R.drawable.noimage);
//            }

           /* 20260205山本:複数選択&一括処理に対応する処理にsetOnClickListenerを統合するのでコメントアウト
            // クリックで DetailActivity に飛ぶ
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DetailActivity.class);
                    intent.putExtra("SHOP_ID", item.id); // 選んだデータの id を渡す
                    v.getContext().startActivity(intent);
                }
            });
            */
            // 20260206山本追記: 複数選択モード中の見た目（透過度と背景色）を制御
            if (isSelectionMode && selectedIds.contains(item.id)) {
                holder.ivPhoto.setAlpha(0.4f);
                holder.itemView.setBackgroundColor(android.graphics.Color.LTGRAY);
            } else {
                holder.ivPhoto.setAlpha(1.0f);
                holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            // クリックで DetailActivity に飛ぶ（既存の処理を条件分岐で拡張）
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 20260206山本追記: 選択モード中は選択状態の反転のみ行い、通常時のみ画面遷移する
                    if (isSelectionMode) {
                        toggleSelection(item.id);
                    } else {
                        Intent intent = new Intent(v.getContext(), DetailActivity.class);
                        intent.putExtra("SHOP_ID", item.id); // 選んだデータの id を渡す
                        v.getContext().startActivity(intent);
                    }
                }
            });

            // 20260206山本追記: 長押しで複数選択モードを開始するためのリスナーを設定
            holder.ivPhoto.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isSelectionMode) {
                        setSelectionMode(true);
                        toggleSelection(item.id);
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    // 20260206山本追記: 選択モードの表示切り替え（メニューバーの出し入れのみ実行）
    /**
     * 複数選択モードのON/OFFを切り替えます。
     * OFF時は選択リストをクリアし、UIをリセットします。
     */
    private void setSelectionMode(boolean mode) {
        this.isSelectionMode = mode;
        if (mode) {
            layoutSelectionMenu.setVisibility(View.VISIBLE);
            // メニューバーが表示される時、RecyclerViewの下部に余白（例: 80dp）を作る
            // ※数値はメニューバーの高さに合わせて調整してください
            float density = getResources().getDisplayMetrics().density;
            int paddingBottom = (int) (80 * density);
            rvResult.setPadding(0, 0, 0, paddingBottom);
            rvResult.setClipToPadding(false); // パディング部分までスクロールできるようにする
        } else {
            layoutSelectionMenu.setVisibility(View.GONE);
            // モード解除時はパディングをゼロに戻す
            rvResult.setPadding(0, 0, 0, 0);
        }
        // アダプターの表示を更新して背景色やアルファ値を反映
        if (rvResult.getAdapter() != null) {
            rvResult.getAdapter().notifyDataSetChanged();
        }
    }

    // 20260206山本追記: 選択リスト（selectedIds）の操作
    /**
     * 指定されたIDの選択状態を反転させます。
     * 選択アイテムが0件になった場合は自動的にモードを終了します。
     */
    private void toggleSelection(int id) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(Integer.valueOf(id));
        } else {
            selectedIds.add(id);
        }
        // 選択状態が変わるたびにリストを再描画
        if (rvResult.getAdapter() != null) {
            rvResult.getAdapter().notifyDataSetChanged();
        }
        // 選択が0になったら自動でモード解除
        if (selectedIds.isEmpty()) {
            setSelectionMode(false);
        }
    }

    // 20260206山本追記: 選択された複数のレコードを一括削除する
    /**
     * 選択中のアイテムを一括削除するための確認ダイアログを表示し、
     * 肯定された場合はトランザクションを使用してDBから削除します。
     */
    private void showBulkDeleteDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("一括削除")
                .setMessage(selectedIds.size() + "件のデータを削除しますか？")
                .setPositiveButton("削除", (dialog, which) -> {
                    DatabaseHelper helper = new DatabaseHelper(this);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.beginTransaction();
                    try {
                        for (int id : selectedIds) {
                            String idStr = String.valueOf(id);

                            // 1. まず紐付く写真を削除 (review_id を指定)
                            db.delete("photos", "review_id = ?", new String[]{idStr});

                            // 2. 次にレビュー本体を削除 (id を指定)
                            db.delete("myfoodreviews", "id = ?", new String[]{idStr});
                        }
                        db.setTransactionSuccessful();
                    } catch (Exception e) {
                        Log.e("ResultActivity", "一括削除中にエラーが発生しました", e);
                    } finally {
                        db.endTransaction();
                        db.close();
                    }
                    setSelectionMode(false);
                    loadResultList(); // リストの再読み込み
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    // 20260207山本追記: 選択されたアイテムのお気に入り状態にする
    /**
     * 選択中のアイテムを一括でお気に入りに登録します。
     */
    private void performBulkFavorite() {
        int count = selectedIds.size();
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("is_favorite", 1); // ここを「反転」ではなく「1」に固定する

            for (Integer id : selectedIds) {
                db.update("myfoodreviews", values, "id = ?", new String[]{String.valueOf(id)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

        // トーストも登録に統一
        Toast.makeText(this, count + "件をお気に入りに登録しました", Toast.LENGTH_SHORT).show();


        setSelectionMode(false); // 選択モード終了
        loadResultList();       // リスト再読み込み
    }
}

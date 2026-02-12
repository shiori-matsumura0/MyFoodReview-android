package com.example.myfoodreview0202;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class UpdateActivity extends AppCompatActivity {

    // ===== UI =====
    private EditText etUpdateShopName; //お店の名前
    private Spinner spUpdatePrefecture; //都道府県
    private EditText etUpdateArea; //エリア
    private Spinner spUpdateGenre; //ジャンル
    private EditText etUpdateUrl; //URL
    private ImageView[] stars; //評価改訂版
    private ImageView ivWantToGo; //行きたいフラグ
    private EditText edVisitedDate; //訪問日
    private EditText etUpdateMemo; //メモ
    private ImageButton btUpdatePhoto; //写真登録用ボタン
    private MenuItem isFavoriteIcon; //お気に入りﾌﾗｸﾞのアイコン

    //20260210松村追記
    private PhotoController photoController;   // PhotoController を追加
    private ArrayList<String> photoPathList;   // 写真パスリスト
    private ImageView ivUpdateMainPhoto;          // メイン写真
    //20260210松村追記　ここまで

    // ===== DB値用 =====
    private int shopId; //検索用主キー
    private String shopName; //店名
    private String prefecture; //都道府県
    private String area; //エリア
    private String genre; //ジャンル
    private String url; //URL
    private int ratingDb = 0; //評価（☆タップで設定されDBに保存する値）
    private int wantToGoFlag; //行きたいフラグ
    private String visitedDate; //訪問日
    private int isFavorite; //お気に入りﾌﾗｸﾞ
    private String memo; //メモ
    private String photoPath; //写真

    // ===== DB =====
    private DatabaseHelper _helper; //データベースヘルパー
    // ===== 色々 =====
    private static final int REQUEST_CODE_PICK_IMAGE = 100; //ギャラリー選択識別

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        //タイトル変更
        setTitle("レビュー変更");

        //部品取得のメソッド呼び出し
        initViews();
        //リスナー設定のメソッド呼び出し
        setupListeners();

        //戻るボタンを配置
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //インテントオブジェクトを取得
        Intent intent = getIntent();
        //詳細画面から渡されたデータを取得→検索用IDにセット。
        shopId = intent.getIntExtra("SHOP_ID",0);

        //DBから値を取得して部品にセットするメソッド呼び出し
        loadDataFromDb();
    }

    // ===== データベースから値を取得する処理 =====
    private void loadDataFromDb() {

        //DBヘルパーの生成
        _helper = new DatabaseHelper(UpdateActivity.this);
        //DBオブジェクト取得
        SQLiteDatabase db = _helper.getWritableDatabase();

        // --- 1. レビュー基本情報の取得 (既存) ---
        //検索SQL文字列
        String sql = "SELECT * FROM myfoodreviews WHERE id = ?";
        String[] params = {String.valueOf(shopId)};
        //SQLの実行
        Cursor cursor = db.rawQuery(sql,params);

        //データが1件以上あれば部品セットのメソッド呼び出し
        if (cursor.moveToFirst()) {
            bindDbToViews(cursor);
        }
        cursor.close();

        //20260210松村追記
        // --- 2. 写真データの取得 ---
        // photosテーブルから、このショップIDに紐づく写真を並び順通りに取得
        String photoSql = "SELECT photo_path FROM photos WHERE review_id = ? ORDER BY sort_order ASC";
        Cursor photoCursor = db.rawQuery(photoSql, params);

        // PhotoControllerが持っているリストを一度クリアして、DBの値を入れ直す
        ArrayList<String> currentPaths = photoController.getPhotoPathList();
        currentPaths.clear();

        while (photoCursor.moveToNext()) {
            int colPath = photoCursor.getColumnIndex("photo_path");
            String path = photoCursor.getString(colPath);
            if (path != null && !path.isEmpty()) {
                currentPaths.add(path);
            }
        }
        photoCursor.close();

        // 最後に表示を更新する
        photoController.updatePhotoViews();

        db.close();

        //20260210松村追記　ここまで
    }


    // ===== ①カラムindexを取得 ②値を変数に格納 ③部品にセット =====
    private void bindDbToViews(Cursor cursor) {
        //店名
        int colShopName = cursor.getColumnIndex("shop_name");
        shopName = cursor.getString(colShopName);
        etUpdateShopName.setText(shopName);
        //都道府県（Spinner）
        int colPrefecture = cursor.getColumnIndex("prefecture");
        prefecture = cursor.getString(colPrefecture);
        setSpinnerSelection(spUpdatePrefecture, prefecture);
        //エリア
        int colarea = cursor.getColumnIndex("area");
        area = cursor.getString(colarea);
        etUpdateArea.setText(area);
        // ジャンル（Spinner）
        int colGenre = cursor.getColumnIndex("genre");
        genre = cursor.getString(colGenre);
        setSpinnerSelection(spUpdateGenre, genre);
        // URL
        int colUrl = cursor.getColumnIndex("url");
        url = cursor.getString(colUrl);
        etUpdateUrl.setText(url);

        // 評価
        int colRating = cursor.getColumnIndex("rating");
        ratingDb = cursor.getInt(colRating);
        updateStarView(ratingDb);

        // 行きたいフラグ
        int colWantToGo = cursor.getColumnIndex("want_to_go");
        wantToGoFlag = cursor.getInt(colWantToGo);
        //フラグを切り替え
        if (wantToGoFlag == 1) {
            ivWantToGo.setColorFilter(Color.parseColor("#99CCFF")); // ONの色
        } else {
            ivWantToGo.setColorFilter(Color.GRAY); // OFFの色
        }
        // 訪問日
        int colVisitedDate = cursor.getColumnIndex("visited_date");
        visitedDate = cursor.getString(colVisitedDate);

        //表示形式の調整
        if (visitedDate != null && !visitedDate.isEmpty()) {
            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
                SimpleDateFormat viewFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
                Date date = dbFormat.parse(visitedDate); //訪問日をString→Dateに変換
                edVisitedDate.setText(viewFormat.format(date)); //表示形式yyyy/MM/ddに変換
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //お気に入りフラグ
        int colFavorite = cursor.getColumnIndex("is_favorite");
        isFavorite = cursor.getInt(colFavorite);
        // メモ
        int colMemo = cursor.getColumnIndex("memo");
        memo = cursor.getString(colMemo);
        etUpdateMemo.setText(memo);
//        // 写真パス
//        int colPhotoPath = cursor.getColumnIndex("photo_path");
//        photoPath = cursor.getString(colPhotoPath);
//        dbPhotoPath = photoPath;
        //画像が登録されていなければ初期画像
//        if (photoPath != null && !photoPath.isEmpty()) {
//            Uri uri = Uri.parse(photoPath);
//            photoController.loadPhotoFromPath(photoPath);
//        } else {
//            ivUpdateMainPhoto.setImageResource(R.drawable.noimage);
//        }
    }

    //部品取得の処理
    private void initViews(){
        etUpdateShopName = findViewById(R.id.etUpdateShopName); //お店の名前
        spUpdatePrefecture = findViewById(R.id.spUpdatePrefecture); //都道府県
        etUpdateArea = findViewById(R.id.etUpdateArea); //エリア
        spUpdateGenre = findViewById(R.id.spUpdateGenre); //ジャンル
        etUpdateUrl = findViewById(R.id.etUpdateUrl); //URL

        //評価改訂版
        stars = new ImageView[]{
                findViewById(R.id.ivUpdateStar1),
                findViewById(R.id.ivUpdateStar2),
                findViewById(R.id.ivUpdateStar3),
                findViewById(R.id.ivUpdateStar4),
                findViewById(R.id.ivUpdateStar5)};
        ivWantToGo = findViewById(R.id.ivUpdateWantToGo); //行きたいフラグ
        edVisitedDate = findViewById(R.id.edVisitedDate); //訪問日
        etUpdateMemo = findViewById(R.id.etUpdateMemo); //メモ
//        ivPhotoPath = findViewById(R.id.ivAddMainPhoto); //写真

        //20260210松村追記
        ivUpdateMainPhoto = findViewById(R.id.ivUpdateMainPhoto);
        ImageView[] subPhotos = new ImageView[]{
                findViewById(R.id.ivUpdateSubPhoto1),
                findViewById(R.id.ivUpdateSubPhoto2),
                findViewById(R.id.ivUpdateSubPhoto3),
                findViewById(R.id.ivUpdateSubPhoto4)
        };


        // PhotoController 作成
        photoController = new PhotoController(this, ivUpdateMainPhoto, subPhotos);

        //20260210松村追記　ここまで
    }
    //リスナー設定の処理
    private void setupListeners(){
        // ☆（評価）クリックリスナー設定
        for (int i = 0; i < stars.length; i++) {
            stars[i].setOnClickListener(new StarClickListener(i)); //この☆に、その☆専用のリスナーを登録する
        }
        //訪問日を手入力不可に設定
        edVisitedDate.setFocusable(false);
        edVisitedDate.setClickable(true); //クリックのみ有効
        //今日の日付用のcalendarを生成
        Calendar calendar = Calendar.getInstance(); //今or選択した日付
        //訪問日に日付選択(カレンダー)するリスナーをセット
        edVisitedDate.setOnClickListener(new UpdateActivity.DateClickListener(edVisitedDate,calendar));

        //行きたいフラグにリスナー設定
        ivWantToGo.setOnClickListener(new UpdateActivity.WantToGoListener());

        //写真登録ボタンに写真ギャラリーを開くリスナーを設定
        btUpdatePhoto = findViewById(R.id.btUpdatePhoto);
//        btUpdatePhoto.setOnClickListener(new UpdateActivity.PhotoClickListener());
        //20260210松村変更
        photoController.attachImageButton(btUpdatePhoto); // PhotoController に委譲
    }

    @Override
    protected void onDestroy(){
        //DBヘルパーオブジェクトの解放
        _helper.close();
        super.onDestroy();
    }

    //オプションメニューを表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_update, menu);
        return true;
    }

    //アクションバーのお気に入りアイコンを取得
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null) return true;

        isFavoriteIcon = menu.findItem(R.id.action_update_favorite);
        if (isFavoriteIcon == null) return true;

        if (isFavorite == 1) {
            isFavoriteIcon.setIcon(R.drawable.ic_baseline_favorite_24);
        } else {
            isFavoriteIcon.setIcon(R.drawable.ic_baseline_favorite_border_24);
        }
        return true;
    }

    //アクションバークリックされた時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //アクションバーの登録ボタンを取得
        int actionRegisterId = item.getItemId();
        //登録ボタンクリック時、ダイアログを表示
        if (actionRegisterId == R.id.action_update_register) {
            // ===== 店名がnull/空文字,都道府県未選択の場合はダイアログを表示 =====
            // ===== 入力値取得 =====
            String shopName = etUpdateShopName.getText().toString();
            String prefecture = spUpdatePrefecture.getSelectedItem().toString();
            // ===== 入力チェック =====
            if (TextUtils.isEmpty(shopName)) {
                showErrorDialog("お店の名前を入力してください"); //msgを渡してアラートダイアログ表示メソッド呼び出す
                return true;
            }
            if (TextUtils.equals(prefecture, "選択してください")) {
                showErrorDialog("都道府県を選択してください");
                return true;
            }
            // ===== 問題なければ登録確認ダイアログ =====
            UpdateConfirmDialogFragment dialogFragment = new UpdateConfirmDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "UpdateConfirmDialogFragment");
            return true;
        }
        //お気に入りボタンクリック時
        else if(actionRegisterId == R.id.action_update_favorite){
            //フラグとアイコンの切り替え
            if (isFavorite == 0){
                isFavorite = 1; //フラグを切り替え
                isFavoriteIcon.setIcon(R.drawable.ic_baseline_favorite_24); //アイコン切り替え
            }else{
                isFavorite = 0; //フラグを切り替え
                isFavoriteIcon.setIcon(R.drawable.ic_baseline_favorite_border_24); //アイコン切り替え
            }
        }
        //戻るボタンをクリック時
        else if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ===== データ更新の処理 =====
    public void shopUpdate(){
        // ===== 最新の画像リストをコントローラーから直接取得する =====
        ArrayList<String> currentPaths = photoController.getPhotoPathList();

        //===== 部品 → 変数 =====
        shopName = etUpdateShopName.getText().toString();
        prefecture = spUpdatePrefecture.getSelectedItem().toString();
        area = etUpdateArea.getText().toString();
        genre = spUpdateGenre.getSelectedItem().toString();
        url = etUpdateUrl.getText().toString();
        memo = etUpdateMemo.getText().toString();
        // 写真パス
//        photoPath = dbPhotoPath;
        photoPath = !currentPaths.isEmpty() ? currentPaths.get(0) : null;

        // --- 1. メインテーブルの更新 ---
        //更新SQL文字列
        String sql = "UPDATE myfoodreviews SET shop_name = ?, prefecture = ?, area = ?, genre = ?, url = ?, rating = ?, want_to_go = ?, visited_date = ?, is_favorite = ?, memo = ? WHERE id = ?";
        SQLiteDatabase db = _helper.getWritableDatabase(); //データベースオブジェクト
        SQLiteStatement stmt = db.compileStatement(sql);

        stmt.bindString(1, shopName);
        stmt.bindString(2, prefecture);
        stmt.bindString(3, area);
        stmt.bindString(4, genre);
        stmt.bindString(5, url);
        stmt.bindLong(6, ratingDb);
        stmt.bindLong(7, wantToGoFlag);
        // visitedDate が null の可能性あり
        if (visitedDate != null) {
            stmt.bindString(8, visitedDate);
        } else {
            stmt.bindNull(8);
        }
        stmt.bindLong(9, isFavorite);
        stmt.bindString(10, memo);
//        // photoPath も null 対応
//        if (photoPath != null) {
//            stmt.bindString(11, photoPath);
//        } else {
//            stmt.bindNull(11);
//        }
        // WHERE id = ?
        stmt.bindLong(11, shopId);
        //SQL実行
        stmt.executeUpdateDelete();
        //クローズ
        stmt.close();

        //20260210松村追記
        // --- 2. 写真データの更新 (ここを追加) ---
        // 一度古い紐付けを全削除
        db.execSQL("DELETE FROM photos WHERE review_id = ?", new Object[]{shopId});

        // 最新のリスト(currentPaths)をループで回して再登録
        String sqlPhoto = "INSERT INTO photos (review_id, photo_path, sort_order) VALUES (?,?,?)";
        SQLiteStatement photoStmt = db.compileStatement(sqlPhoto);

        for (int i = 0; i < currentPaths.size(); i++) {
            photoStmt.clearBindings();
            photoStmt.bindLong(1, shopId);
            photoStmt.bindString(2, currentPaths.get(i));
            photoStmt.bindLong(3, i);
            photoStmt.executeInsert();
        }
        photoStmt.close();
        //20260210松村追記　ここまで


        db.close();
        Toast.makeText(UpdateActivity.this, "更新しました", Toast.LENGTH_LONG).show();
        finish();

    }

    //選択画像を取得する処理
    @Override
    //引数①requestCode「どのstartActivityForResult から戻ってきたか」②resultCode「成功？キャンセル？」③data「URI」
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        //写真選択（ギャラリー）から画像が正常に返ってきた場合	//写真選択（ギャラリー）から画像が正常に返ってきた場合
        // if(requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null){	if(requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null){
        // Uri imageUri = data.getData(); //選択した画像のURIを取得	Uri imageUri = data.getData(); //選択した画像のURIを取得
        // ivPhotoPath.setImageURI(imageUri); //ImageViewに表示	ivPhotoPath.setImageURI(imageUri); //ImageViewに表示
        // dbPhotoPath = imageUri.toString(); //DB用に文字列として保存	dbPhotoPath = imageUri.toString(); //DB用に文字列として保存
        // }
        // 松村
//        if(requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null){
//            Uri imageUri = data.getData();
//            String copiedPath = ImageStorageUtil.copyImageToAppDir(this, imageUri);
//            if (copiedPath != null) {
//                ivPhotoPath.setImageURI(Uri.fromFile(new File(copiedPath)));
//                dbPhotoPath = copiedPath;
//            } else {
//                Toast.makeText(this, "画像の保存に失敗しました", Toast.LENGTH_SHORT).show();
//            }
//        }

        //20260210松村追記
        photoController.onActivityResult(requestCode, resultCode, data);
        photoPathList = photoController.getPhotoPathList(); // 必要ならDB登録用に保持
    }

    //Spinnerに値をセット
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner == null || value == null) return;

        SpinnerAdapter adapter = spinner.getAdapter();
        if (adapter == null) return;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (value.equals(adapter.getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }
    //必須項目未入力時、ダイアログを表示する処理
    private void showErrorDialog(String msg) {
        AddAlertDialogFragment dialogFragment = new AddAlertDialogFragment();
        // DialogFragmentに項目名を渡す(Bundle）
        Bundle args = new Bundle();
        args.putString("Msg", msg);
        dialogFragment.setArguments(args); //DialogFragmentにmsgをセット
        //dialogFragment表示
        dialogFragment.show(getSupportFragmentManager(), "AddAlertDialogFragment");
    }

    //行きたいフラグをクリックしたとき、フラグ＆色の切り替えの処理
    private class WantToGoListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //フラグを切り替え
            if (wantToGoFlag == 0) {
                wantToGoFlag = 1;
                ivWantToGo.setColorFilter(Color.parseColor("#009944")); // ONの色
            }else{
                wantToGoFlag = 0;
                ivWantToGo.setColorFilter(Color.GRAY); // OFFの色
            }
        }
    }
    //評価★表示切り替え用メソッドを追加
    private void updateStarView(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_baseline_star_24); // 塗り★
            } else {
                stars[i].setImageResource(R.drawable.ic_baseline_star_border_24);// 空☆
            }
        }
    }
    //訪問日をクリックしたとき、日付選択する処理
    private class DateClickListener implements View.OnClickListener{

        private EditText editText;
        private Calendar calendar;

        //コンストラクタで必要な情報を渡す
        public DateClickListener(EditText editText, Calendar calendar) {
            this.editText = editText;
            this.calendar = calendar;
        }

        @Override
        public void onClick(View v) {
            // 現在のカレンダーの年月日を取得
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // DatePickerDialog を作る
            DatePickerDialog dialog = new DatePickerDialog(UpdateActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        // 日付を受け取って処理する
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            //1.選択日をcalendarにセット
                            calendar.set(year, month, dayOfMonth);
                            //2.EditTextに表示用（yyyy/MM/dd）
                            SimpleDateFormat displayFmt = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
                            editText.setText(displayFmt.format(calendar.getTime()));
                            // DB保存用（yyyyMMdd）
                            SimpleDateFormat dbFmt = new SimpleDateFormat("yyyyMMdd", Locale.JAPAN);
                            visitedDate = dbFmt.format(calendar.getTime());
                        }
                    },
                    //DatePickerDialogの最後の引数に、初期表示する年月日
                    year, month, day
            );
            dialog.show(); //画面に表示
        }
    }

    //評価☆を塗りつぶす処理
    private class StarClickListener implements View.OnClickListener {

        private int starIndex;//0～4

        public StarClickListener(int starIndex) { //タップされた☆のインデックス（0〜4））
            this.starIndex = starIndex;
        }

        @Override
        public void onClick(View v) {
            int tappedRating = starIndex + 1;

            if (ratingDb == tappedRating) {
                // 同じ星をもう一度押したら解除
                ratingDb = 0;
            } else {
                // 新しい評価をセット
                ratingDb = tappedRating;
            }

            updateStarView(ratingDb);
        }
    }
}
package com.example.myfoodreview0202;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class AddActivity extends AppCompatActivity {

    // ===== UI =====
    private EditText etAddShopName; //お店の名前
    private Spinner spAddPrefecture; //都道府県
    private EditText etAddArea; //エリア
    private Spinner spAddGenre; //ジャンル
    private EditText etAddUrl; //URL
    private ImageView[] stars; //評価改訂版
    private ImageView ivWantToGo; //行きたいフラグ
    private EditText edVisitedDate; //訪問日
    private EditText etAddMemo; //メモ
    private ImageView ivAddMainPhoto; //メイン写真
    private ImageButton btAddPhoto; //写真登録用ボタン

    //20260210松村追記
    private PhotoController photoController;

    //20260210松村追記ここまで

    //評価（☆タップで設定されDBに保存する値）
    private int ratingDb = 0;
    //行きたいフラグの初期値
    private int wantToGoFlag = 0;
    // 訪問日DB変換用をクラスフィールドとして宣言
    private String dbVisitedDate = "";
    //写真のDB登録用
    //private String dbPhotoPath = "";
    //20260210松村変更
    private ArrayList<String> photoPathList;
//    //ギャラリー選択識別
//    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    //データベース
    DatabaseHelper _helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        setTitle("レビュー追加");

        //部品を取得
        etAddShopName = findViewById(R.id.etAddShopName); //お店の名前
        spAddPrefecture = findViewById(R.id.spAddPrefecture); //都道府県
        etAddArea = findViewById(R.id.etAddArea); //エリア
        spAddGenre = findViewById(R.id.spAddGenre); //ジャンル
        etAddUrl = findViewById(R.id.etAddUrl); //URL

        //評価改訂版
        stars = new ImageView[]{
                findViewById(R.id.ivAddStar1),
                findViewById(R.id.ivAddStar2),
                findViewById(R.id.ivAddStar3),
                findViewById(R.id.ivAddStar4),
                findViewById(R.id.ivAddStar5)};
        ivWantToGo = findViewById(R.id.ivAddWantToGo); //行きたいフラグ
        edVisitedDate = findViewById(R.id.edVisitedDate); //訪問日
        etAddMemo = findViewById(R.id.etAddMemo); //メモ

        // ☆（評価）クリックリスナー設定
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1; // 1〜5
            stars[i].setOnClickListener(new StarClickListener(rating)); //この☆に、その☆専用のリスナーを登録する
        }

        //20260210松村追記
        // メイン画像 + サブ画像の取得
        ivAddMainPhoto = findViewById(R.id.ivAddMainPhoto);
        ImageView[] subPhotos = new ImageView[]{
                findViewById(R.id.ivAddSubPhoto1),
                findViewById(R.id.ivAddSubPhoto2),
                findViewById(R.id.ivAddSubPhoto3),
                findViewById(R.id.ivAddSubPhoto4)
        };

        // 写真追加ボタン
        btAddPhoto = findViewById(R.id.btAddPhoto);

        // PhotoController 作成（クリック・長押し・追加ボタン 全て管理）
        photoController = new PhotoController(this, ivAddMainPhoto, subPhotos);
        photoController.attachImageButton(btAddPhoto);

        // PhotoController の写真リストを取得
        photoPathList = photoController.getPhotoPathList();


        //20260210松村追記ここまで


        //今日の日付用のcalendarを生成
        Calendar calendar = Calendar.getInstance(); //今or選択した日付
        //訪問日を手入力不可に設定
        edVisitedDate.setFocusable(false);
        edVisitedDate.setClickable(true); //クリックのみ有効
        //訪問日に日付選択(カレンダー)するリスナーをセット
        edVisitedDate.setOnClickListener(new DateClickListener(edVisitedDate, calendar));

        //行きたいフラグにリスナー設定
        ivWantToGo.setOnClickListener(new WantToGoListener());

        //写真登録ボタンに写真ギャラリーを開くリスナーを設定
        btAddPhoto = findViewById(R.id.btAddPhoto);
//        btAddPhoto.setOnClickListener(new PhotoClickListener());
        //20260210松村変更
        photoController.attachImageButton(btAddPhoto);

        //DBヘルパーの生成
        _helper = new DatabaseHelper(AddActivity.this);

        //戻るボタンを配置
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        //DBヘルパーオブジェクトの解放
        _helper.close();
        super.onDestroy();
    }

    //オプションメニューを表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_add, menu);
        return true;
    }

    //選択画像を取得する処理
    @Override
    //引数①requestCode「どのstartActivityForResult から戻ってきたか」②resultCode「成功？キャンセル？」③data「URI」
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
//        //写真選択（ギャラリー）から画像が正常に返ってきた場合
//        if(requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null){
//            Uri imageUri = data.getData(); //選択した画像のURIを取得
//
//            String copiedPath = ImageStorageUtil.copyImageToAppDir(this, imageUri);
//
//            if (copiedPath != null) {
//                ivPhotoPath.setImageURI(Uri.fromFile(new File(copiedPath)));
//                dbPhotoPath = copiedPath; // ← DBにはこれだけ保存
//
//
//                if (photoPathList.size() >= 5) {
//                    Toast.makeText(this, "写真は5枚までです", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                photoPathList.add(copiedPath);
//                updatePhotoViews();
//
//            } else {
//                Toast.makeText(this, "画像の保存に失敗しました", Toast.LENGTH_SHORT).show();
//            }
//        }

        //20260210 松村変更
        //機能をphotoControllerに移動
        photoController.onActivityResult(requestCode, resultCode, data);
    }


    //アクションバークリックされた時の処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //アクションバーの登録ボタンを取得
        int actionRegisterId = item.getItemId();
        //登録ボタンクリック時、ダイアログを表示
        if (actionRegisterId == R.id.action_register) {
            // ===== 店名がnull/空文字,都道府県未選択の場合はダイアログを表示 =====
            // ===== 入力値取得 =====
            String shopName = etAddShopName.getText().toString();
            String prefecture = spAddPrefecture.getSelectedItem().toString();
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
            AddConfirmDialogFragment dialogFragment = new AddConfirmDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "AddConfirmDialogFragment");
            return true;
        }
        //戻るボタンをクリック時
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //shop登録の処理
    public void shopListRegister() {

        //作成日時（yyyyMMddHHmmss）を取得
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN);
        String createdToday = sdf.format(new Date()); //今の日時

        //入力された(部品の)値を取得
        String shopName = etAddShopName.getText().toString(); //お店の名前
        String prefecture = spAddPrefecture.getSelectedItem().toString(); //都道府県
        String area = etAddArea.getText().toString(); //エリア
        String genre = spAddGenre.getSelectedItem().toString(); //ジャンル
        String url = etAddUrl.getText().toString(); //URL
        //削除★String ratingStr = spAddRating.getSelectedItem().toString(); //評価
        String memo = etAddMemo.getText().toString(); //メモ

        /**評価改訂版問題なければ、削除OK
         //評価★をintに変換
         if (TextUtils.equals(ratingStr,"選択してください")){
         ratingDb = 0;
         } else {
         ratingDb = ratingStr.length();
         }
         **/

        //書き込み可能なDBオブジェクト取得
        SQLiteDatabase db = _helper.getWritableDatabase();
        //insert用SQL文字列を用意
        String sqlInsert = "INSERT INTO myfoodreviews (created_at,shop_name,prefecture,area,genre,url,rating,want_to_go,visited_date,is_favorite,memo) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        //SQL文字列をもとにプリペア度ステートメントを取得
        SQLiteStatement stmt = db.compileStatement(sqlInsert);
        //変数のバインド
        stmt.bindString(1, createdToday);
        stmt.bindString(2, shopName);
        stmt.bindString(3, prefecture);
        stmt.bindString(4, area);
        stmt.bindString(5, genre);
        stmt.bindString(6, url);
        stmt.bindLong(7, ratingDb);
        stmt.bindLong(8, wantToGoFlag);
        stmt.bindString(9, dbVisitedDate);
        stmt.bindLong(10, 0); // is_favorite
        stmt.bindString(11, memo);
        //stmt.bindString(12, dbPhotoPath);


//        //SQLの実行
//        stmt.executeInsert();

        //20260210松村追記
        //レビューのidを取得
        long reviewId = stmt.executeInsert();

        //20260210松村追記
        // ===== 写真登録 =====
        String sqlPhoto =
                "INSERT INTO photos (review_id, photo_path, sort_order) VALUES (?,?,?)";

        SQLiteStatement photoStmt = db.compileStatement(sqlPhoto);

        for (int i = 0; i < photoPathList.size(); i++) {
            photoStmt.clearBindings();
            photoStmt.bindLong(1, reviewId);                // ★紐づけ
            photoStmt.bindString(2, photoPathList.get(i));  // パス
            photoStmt.bindLong(3, i);                       // 並び順

            photoStmt.executeInsert();
        }

        //登録完了のトーストを表示
        Toast.makeText(AddActivity.this, "登録しました", Toast.LENGTH_LONG).show();
        /** 登録完了後はfinish()するのでリセット処理不要
         //入力値をリセットするメソッド呼び出し
         clearInput();
         **/
        // MainActivityに戻る
        finish();
    }

    // ===== ☆の塗りつぶしの設定 =====
    private void setRatingStars(int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_baseline_star_24); // 塗り★
            } else {
                stars[i].setImageResource(R.drawable.ic_baseline_star_border_24); // 空☆
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
    /** 登録完了後はfinish()するのでリセット処理不要
     //登録後のリセット処理
     private void clearInput() {
     etAddShopName.setText(""); //お店の名前
     spAddPrefecture.setSelection(0); //都道府県
     etAddArea.setText(""); //エリア
     spAddGenre.setSelection(0); //ジャンル
     etAddUrl.setText(""); //URL
     spAddRating.setSelection(0); //評価
     wantToGoFlag = 0; //行きたいフラグ
     ivWantToGo.setColorFilter(Color.GRAY); // OFF状態
     edVisitedDate.setText(""); //訪問日
     etAddMemo.setText(""); //メモ
     ivPhotoPath.setImageResource(R.drawable.noimage); // 初期画像に戻す
     dbPhotoPath = null; //画像パスをnull
     }
     **/


    //行きたいフラグをクリックしたとき、フラグ＆色の切り替えの処理
    private class WantToGoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //フラグを切り替え
            if (wantToGoFlag == 0) {
                wantToGoFlag = 1;
                ivWantToGo.setColorFilter(Color.parseColor("#99CCFF")); // ONの色
            } else {
                wantToGoFlag = 0;
                ivWantToGo.setColorFilter(Color.GRAY); // OFFの色
            }
        }
    }

    //訪問日をクリックしたとき、日付選択する処理
    private class DateClickListener implements View.OnClickListener {

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
            DatePickerDialog dialog = new DatePickerDialog(AddActivity.this,
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
                            dbVisitedDate = dbFmt.format(calendar.getTime());
                            // insert 用：dbVisitedDate
                        }
                    },
                    //DatePickerDialogの最後の引数に、初期表示する年月日
                    year, month, day
            );
            dialog.show(); //画面に表示
        }
    }

    // 評価☆を塗りつぶす処理
    private class StarClickListener implements View.OnClickListener {

        private int rating; // タップされた☆（1〜5）

        public StarClickListener(int rating) {  //引数＿タップされた☆の評価値（1〜5）
            this.rating = rating;
        }

        @Override
        public void onClick(View v) {

            if (ratingDb == rating) {
                // 同じ星をもう一度押したら解除
                ratingDb = 0;
            } else {
                // 新しい評価をセット
                ratingDb = rating;
            }

            setRatingStars(ratingDb); //評価値に応じて☆の塗りつぶしを更新
        }
    }
}
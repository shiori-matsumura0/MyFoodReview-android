package com.example.myfoodreview0202;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myfoodreviews.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        // 20260210 画像複数登録対応のため、myfoodreviewsテーブルからphoto_pathカラムを削除
        // photosテーブルで画像を管理
        
        // ===== レビューテーブル =====
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE myfoodreviews (");
        sb.append("id INTEGER PRIMARY KEY AUTOINCREMENT,"); // id 自動採択
        sb.append("created_at TEXT,");                      // 作成日時
        sb.append("shop_name TEXT,");                       // 店名
        sb.append("prefecture TEXT,");                      // 都道府県
        sb.append("area TEXT,");                            // エリア
        sb.append("genre TEXT,");                           // ジャンル
        sb.append("url TEXT,");                             // url
        sb.append("rating INTEGER,");                       // 評価
        sb.append("want_to_go INTEGER,");                   // 行った0 or 行きたい1
        sb.append("visited_date TEXT,");                    // 訪問日
        sb.append("is_favorite INTEGER,");                  // お気に入り 0or1
        sb.append("memo TEXT");                             // メモ
        sb.append(");");

        db.execSQL(sb.toString());

        // ===== 写真テーブル（複数対応）=====
        StringBuilder sbPhoto = new StringBuilder();
        sbPhoto.append("CREATE TABLE photos (");
        sbPhoto.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");    // id　自動採択
        sbPhoto.append("review_id INTEGER,");                       // myfoodreviews.id
        sbPhoto.append("photo_path TEXT,");                         // 画像のコピーパス
        sbPhoto.append("sort_order INTEGER,");                      // 並び順
        sbPhoto.append("FOREIGN KEY(review_id) REFERENCES myfoodreviews(id)");
        sbPhoto.append(");");

        db.execSQL(sbPhoto.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}


}

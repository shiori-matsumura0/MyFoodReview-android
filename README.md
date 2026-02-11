# My Food Review App (Android)

## 概要
職業訓練校でのグループワークとして制作した、写真付きの外食記録管理Androidアプリです。

## 開発形態
チーム開発

## 担当箇所
- 詳細画面の実装（DetailActivity）
- 検索画面の実装（SerchDialogFragment）
- 検索結果画面の実装（ResultActivity：他メンバーと共同）
- SQLite を用いたデータ管理  
  （取得・検索・削除処理、DatabaseHelper の設計）
- 画像保存処理（内部ストレージへのコピー）
- 単一画像から複数画像への対応を実装（AddActivity、UpdateActivity、DetailActivity）
- 共通処理をまとめたクラスの作成（ImageManager、PhotoContoroller、ImageStrageUtil）

## 使用技術
- Android Studio
- Java
- SQLite
- RecyclerView
- Glide

## 工夫した点
- onCreate と onResume を使い分け、
  画面表示時・画面復帰時に最新の状態が反映されるようにした
- Glideを使用して画像を非同期で読み込み、
  一覧表示時のスクロール性能を意識した
- 画像はアプリ内部ストレージにコピーして管理し、
  データベースには画像パスのみを保存する構成にした
- 単一画像から複数画像への対応を実装し、
  複数の画像を安全かつ効率的に管理できるようにした
- 共通処理はメソッドをまとめてクラス化し、
  複数画面から再利用できるようにした
- 削除処理は確認ダイアログを表示し、
  誤操作を防ぐUIにした

  ## スクリーンショット
  

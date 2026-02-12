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
 <img width="30%" height="2400" alt="Screenshot_1770875497" src="https://github.com/user-attachments/assets/8fea168f-ad81-4586-a717-cdb008b86dbf" />　<img width="30%" height="2400" alt="Screenshot_1770875607" src="https://github.com/user-attachments/assets/8d7858e7-6508-4ac0-99e3-1f90801bb4f0" />　<img width="30%" height="2400" alt="Screenshot_1770875732" src="https://github.com/user-attachments/assets/6c16c98b-7c8d-44f3-9d20-7d3fb631f6cd" />
 
 <img width="30%" height="2400" alt="Screenshot_1770875752" src="https://github.com/user-attachments/assets/bbcba548-9e88-48b0-b226-e9f20922d795" />　<img width="30%" height="2400" alt="Screenshot_1770875674" src="https://github.com/user-attachments/assets/750a16cc-579e-43bc-9a07-64a651c64986" />　<img width="30%" height="2400" alt="Screenshot_1770875678" src="https://github.com/user-attachments/assets/a81f0d2a-aa4a-4a45-b88f-9a64b76cc908" />

  







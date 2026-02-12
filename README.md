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

- **ライフサイクルの適切な管理**  
  `onCreate` と `onResume` を使い分け、画面遷移から戻った際にも最新の状態が常に反映されるように制御しました。
- **Glideによる画像表示の最適化**  
  非同期読み込みを採用し、画像が多い一覧画面でもカクつかない、スムーズなスクロール体験を意識しました。
- **効率的なデータ設計**  
  画像本体は内部ストレージで管理し、DBにはパスのみを保存する構成に。さらに単一画像から複数画像への拡張にも柔軟に対応させました。
- **保守性を意識したリファクタリング**  
  共通処理はメソッド化・クラス化を行い、コードの重複を避けて再利用性を高めました。
- **誤操作を防ぐUI**  
  削除処理時の確認ダイアログ実装など、ユーザーが安心して操作できる設計を心がけました。


  ## スクリーンショット
  <p align="center">
    <img width="30%" alt="Screenshot_1770875497" src="https://github.com/user-attachments/assets/8fea168f-ad81-4586-a717-cdb008b86dbf" />　
    <img width="30%" alt="Screenshot_1770875607" src="https://github.com/user-attachments/assets/8d7858e7-6508-4ac0-99e3-1f90801bb4f0" />　
    <img width="30%" alt="Screenshot_1770875732" src="https://github.com/user-attachments/assets/6c16c98b-7c8d-44f3-9d20-7d3fb631f6cd" />
  </p>

 <p align="center">
   <img width="30%" alt="Screenshot_1770875752" src="https://github.com/user-attachments/assets/bbcba548-9e88-48b0-b226-e9f20922d795" />　
   <img width="30%" alt="Screenshot_1770875674" src="https://github.com/user-attachments/assets/750a16cc-579e-43bc-9a07-64a651c64986" />　
   <img width="30%" alt="Screenshot_1770875678" src="https://github.com/user-attachments/assets/a81f0d2a-aa4a-4a45-b88f-9a64b76cc908" />
 </p>

 ## 動作デモ

 <table>
  <tr>
    <th width="30%">メイン</th>
    </th> <th width="30%">詳細</th>
    </th> <th width="30%">検索・検索結果</th>
  </tr>
  <tr>
    <td>
      <video src="https://github.com/user-attachments/assets/35f31592-629c-4946-8319-bce35abc3b0c" width="100%" autoplay muted loop playsinline></video>
    </td>
    <td>
      <video src="https://github.com/user-attachments/assets/976d61e0-d27d-415e-97b3-9fc7e5ce25f7" width="100%" autoplay muted loop playsinline></video>
    </td>
    <td>
      <video src="https://github.com/user-attachments/assets/84573128-7b7e-4d9c-bd50-b540521fdb10" width="100%" autoplay muted loop playsinline></video>
    </td>
  </tr>
</table>
 
 














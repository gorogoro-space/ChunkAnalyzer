# ChunkAnalyzer

読み込まれているチャンクのエンティティー・タイルエンティティーの数を数え、多いワールド・チャンク・種類を表示する Paper 用プラグイン(Paper 1.21.11 以降・Java 21 必須)。
リポジトリ: https://github.com/gorogoro-space/ChunkAnalyzer (LICENSE は GPL v3。ソースのヘッダーには「LGPv3」と書かれている)

## 作業の進め方(必ず守ること)

- **設計が確定するまで実装しない。** 機能追加や仕様変更は、まず設計案(何を・なぜ・どう変えるか、影響範囲)を提示し、承認を得てからコードを書く。
- 判断が必要な点は、選択肢を示して質問する。勝手に決めない。
- やり取りは日本語で行う。新しく書くコードのコメントは日本語でよい。既存の英語のコメントは、頼まれない限り書き換えない。
- 変更は必要最小限にする。頼まれていないリファクタリングや機能追加はしない。
- 作業後は、変更・追加・削除したファイルの一覧と変更内容を報告する。
- 仕様を変えたら README.md と CLAUDE.md も合わせて更新する。
- `git push` の前には必ず確認を取る。コミットは意味のある単位で分ける。
- リポジトリへの初回の `git push` の前には、「GitHub で Watch 設定(Custom → Issues と Pull requests)はしましたか?」と日本語で確認する。自動 Watch は廃止されており、設定しないと issue や PR の通知が届かない。
- コミットメッセージや PR に `Co-Authored-By: Claude` などの署名を付けない。`.claude/` は Git に入れない(`.git/info/exclude` で除外済み)。
- 実装中に設計の抜けや穴に気づいたら、黙って対処せず報告して相談する。
- プルリクエストをチェックするときは、次の観点で確認して報告する。
  - 変更概要(何を・なぜ変えているか)
  - 脆弱性(権限チェックの漏れ、入力値の検証、パスの扱い、コマンドに埋め込む文字列の検証など)
  - 安全面(データの破損・消失、再読み込み時の後始末、他プラグインの妨げなど)
  - 性能(TPS など)の低下(高頻度イベントでの重い処理、メインスレッドでの同期 I/O、定期タスクの追加など。「最重要の設計方針」に沿っているか)

## 環境

- Paper 1.21.11(`io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT`、`compileOnly`)/ `plugin.yml` の `api-version: '1.21.11'`(1.21.11 未満のサーバーでは読み込まれない)。勝手に変えない
- **Java 21 必須。** `build.gradle.kts` の toolchain で JDK 21 を使い、Java 21 の class ファイルを出力する。勝手に変えない
- ビルド: Gradle(Kotlin DSL、wrapper 9.7.1)。`gradlew.bat clean build`(Windows)。**JDK 21 で実行する**(JDK の場所は `CLAUDE.local.md`)。「ビルドして」と言われたら常にクリーンビルドする。成果物は `build/libs/ChunkAnalyzer-<version>.jar`
  - IntelliJ の「アーティファクトのビルド」はクラスファイルが入らないことがある。必ず Gradle でビルドする
- バージョンは `gradle.properties` の `version` だけで管理する。`plugin.yml` の `version: '${version}'` は `processResources` でビルド時に置き換わる
- パッケージ: `space.gorogoro.chunkanalyzer`(**すべて小文字**。大文字が混ざると plugin.yml の main と一致せず起動しない)
- メッセージは Adventure(`Component`・`NamedTextColor`・`ClickEvent`・`HoverEvent`)で送る。非推奨の `ChatColor`・BungeeCord の chat API(`TextComponent` など)・`sender.spigot()` は使わない
- 動作確認はサーバーを再起動して行う。PlugManX での読み込みは権限やコマンドの登録が不完全になることがある。jar を差し替えるときは先に古い jar を消す(同じ名前のプラグインが 2 つあると、どちらが読まれるかわからない)
- サーバーと開発 PC の環境情報は `CLAUDE.local.md`(Git に入れない)を参照する

## 最重要の設計方針

### TPS に影響させない
- 高頻度イベント(PlayerInteractEvent、PlayerMoveEvent など)を使うときは、安い判定を先に行い、対象外なら即座に抜ける
- BlockPhysicsEvent、VehicleMoveEvent など発生頻度が極端に高いイベントは使わない
- メインスレッドでファイルや DB の同期 I/O をしない(起動時・リロード時に一度だけ行う小さなファイルの読み込みは除く)
- 定期タスクは最小限にし、追加するときは頻度と理由を設計案に書く
- このプラグインは読み込まれている全チャンクを走査する。走査の回数・範囲を増やす変更は、設計案に影響を書く
  - チャンク・エンティティーはメインスレッドでしか読めないので、別スレッド・非同期では数えない
  - タイルエンティティーは `getTileEntities(false)`(複製を作らない)で取得する。取得したものは書き換えない(ワールドが直接変わる)。`getTileEntities()` はチェスト・樽などの中身を複製するため、2.0 より前は `/chunka <world>` の 1 回で約 2.5 秒止まっていた(2026-11-01 の spark の記録)
  - **今の実装は、コマンド実行時に 1 tick の中で全チャンクを数えている**(Maven のころからそのまま)。新しく走査する処理を足すときは、この形を広げない

### データの保存
- プラグインフォルダ以外には何も書き込まない

### 他プラグインとの関係
- 既存の機能を妨げないこと。イベントをキャンセルする範囲は必要最小限にする

## 機能仕様

(現在の実装の動作。仕様を変えたらここを更新する)

- **起動時(`onEnable`)**: プラグインフォルダが無ければ作り、`config.yml` が無ければ jar 内のものを保存して読み込む
- **コマンド共通**(`onCommand`): プレイヤーのみ(コンソールは「The command can only be executed by the player.」)、OP のみ(`sender.isOp()`。それ以外は「No operator permissions.」)。権限ノードは使っていない(plugin.yml に permission の定義はない)
  - 表示件数は config.yml の `limit`。1〜50 の範囲外なら「Please specify the number of lines to be show, between 1 and 50.」
- **`/chunka`**(`showWorldScore`): ワールドごとに、読み込まれているチャンクのエンティティー数とタイルエンティティー数の合計を、多い順に `limit` 件表示する。読み込まれているチャンクが無いワールドは出ない。行をクリックすると `/chunka <ワールド名>` を実行する
- **`/chunka <world_name>`**(`showChunkScore`): ワールド名が `^[0-9a-z_]+$` に合わなければ「The world name should be a character from 0-9a-z_.」、ワールドが無ければ「Can't get the world from the world name.」。合計数が多いチャンクを `limit` 件表示する
  - 先頭の「＞＞ Teleport to "<world>" world」をクリックすると `/mvtp <world>`(Multiverse-Core)を実行する
  - 各行のホバーに、テレポート先の座標・region ファイル名・種類別の数(上位 `limit` 件。`getEntityMsg`)を出す。クリックすると `/tp x y z`(`getLocation`)を実行する
  - 末尾の「＜＜ Back」をクリックすると `/chunka` を実行する
  - 引数が 2 つ以上なら「Invalid world name argument.」
- **`/chunkc`**(`showCurrentChunkScore`): 今いるチャンクについて、座標・region ファイル名・種類別の数(上位 `limit` 件)を表示する。引数は見ない
- **座標(`getLocation`)**: x・z はチャンク座標 × 16(チャンクの北西の角)。y はその角の `w.getHighestBlockYAt(x, z)`(一番高いブロックの Y)。2.0 より前はブロック座標ではなくチャンク座標を渡しており、別の場所の高さになったり、読み込まれていないチャンクを読み込んだりしていた
- **region ファイル名**: `r.<チャンク x >> 5>.<チャンク z >> 5>.mca`
- 種類の名前は、タイルエンティティーは `Material`、エンティティーは `EntityType` の名前
- 例外は `logStackTrace` でスタックトレースを警告ログに出す

## 過去にハマった点(他プロジェクトでの経験)

- `config.getString(path, "")` のように既定値を渡すと、jar 内 config.yml の既定値が参照されない。既定値なしで取得して null を判定すること
- plugin.yml で `default: true` にした権限でも、登録されないと Bukkit は「OP のみ」として扱う。全員向けの機能を権限で縛らない

## ファイル構成

- `src/main/java/space/gorogoro/chunkanalyzer/ChunkAnalyzer.java` — メインクラス。起動・停止、コマンドの処理、集計と表示をすべて持つ
- `src/main/resources/plugin.yml` — プラグイン定義、コマンド定義(`chunka`、`chunkc`)
- `src/main/resources/config.yml` — `limit`(表示件数。既定 10)
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Gradle の設定(バージョンは `gradle.properties`)

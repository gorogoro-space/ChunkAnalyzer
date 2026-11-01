# ChunkAnalyzer
[![Paper 1.21.11](https://img.shields.io/badge/Paper-1.21.11-brightgreen.svg)](https://fill-ui.papermc.io/projects/paper/version/1.21.11)
[![GitHub release](https://img.shields.io/github/release/gorogoro-space/ChunkAnalyzer.svg)](https://github.com/gorogoro-space/ChunkAnalyzer/releases)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/gorogoro-space/ChunkAnalyzer/issues)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/gorogoro-space/ChunkAnalyzer/blob/main/LICENSE)

Provides commands to parse the chunks and show the results.

読み込まれているチャンクのエンティティー・タイルエンティティーの数を数えて、多いワールド・チャンク・種類を表示するプラグインです。

# Requirements
- Paper 1.21.11 以降(1.21.11 未満のサーバーでは読み込まれません)
- Java 21
- (任意)Multiverse-Core — `/chunka <world>` の「Teleport to "world" world」で `/mvtp` を使います

# Installation method
Please place the .jar file in the Paper plugins folder.

.jar ファイルを Paper の plugins フォルダに置き、サーバーを再起動してください。

# Usage
```
/chunka [<world_name>]
/chunkc
```

どちらも OP のみ・プレイヤーのみ(コンソールからは使えません)です。

- `/chunka` — ワールドごとに、読み込まれているチャンクのエンティティーとタイルエンティティーの合計数を、多い順に表示します。行をクリックすると、そのワールドの `/chunka <world_name>` を実行します
- `/chunka <world_name>` — 指定したワールドで、エンティティーとタイルエンティティーの合計数が多いチャンクを、多い順に表示します
  - 行にマウスを乗せると、そのチャンクの region ファイル名と、多い種類を表示します。クリックするとそのチャンクへテレポート(`/tp`)します
  - 「Teleport to "world" world」をクリックすると `/mvtp <world_name>` を実行します(Multiverse-Core が必要です)
  - 「Back」をクリックすると `/chunka` に戻ります
  - ワールド名に使えるのは `0-9a-z_` の文字だけです
- `/chunkc` — 今いるチャンクの位置・region ファイル名と、エンティティー・タイルエンティティーの多い種類を表示します

チャンクの位置は、チャンク座標(x,z)で表示します。

# Config
`plugins/ChunkAnalyzer/config.yml`

| 項目 | 内容 |
|---|---|
| `limit` | 表示する件数(1〜50)。既定は 10 |

config.yml を書き換えたら、サーバーを再起動してください。

# Disclaimer
Do not assume any responsibility by use. Please use it at your own risk.

## ビルド

JDK 21 が必要です。Gradle はラッパー(`gradlew`)が自動でダウンロードするので、別途インストールする必要はありません。

```
gradlew.bat clean build
```

(macOS / Linux では `./gradlew clean build`)

## IntelliJ IDEA でのビルド手順

本プロジェクトはビルドツールに Gradle を使用しています。
IntelliJ IDEA 上で正しくプラグイン（JARファイル）を生成するには、以下の手順を実行してください。
「ビルド → アーティファクトのビルド」はクラスファイルが入らないことがあるので使わないでください。

### 🛠️ ビルド手順

1. IntelliJ IDEA の画面右端にある **「Gradle」タブ** をクリックして開きます。
2. プロジェクト名（ChunkAnalyzer）を展開し、 **`Tasks`** ツリーを開きます。
3. リスト内にある **`clean`** をダブルクリックして実行します（古いビルドキャッシュを削除します）。
4. 続けてリスト内にある **`build`** をダブルクリックして実行します。

### 📦 生成されたファイルの場所
ビルドが成功すると、プロジェクトのルート直下に `build/libs` フォルダが作成（または更新）され、その中に中身の詰まった正しい JAR ファイルが生成されます。

* **生成先:** `build/libs/ChunkAnalyzer-<version>.jar`(バージョンは `gradle.properties` の `version`)

この JAR ファイルを Minecraft サーバーの `plugins` フォルダに配置してください。

## 開発(IntelliJ IDEA で Claude Code を使う)

このリポジトリには、AI コーディングツール [Claude Code](https://docs.claude.com/ja/docs/claude-code/overview) 向けの作業ルールを書いた `CLAUDE.md` があります。IntelliJ IDEA で使う手順は次のとおりです。

1. Claude の有料プラン(Pro / Max)か、Anthropic API のアカウントを用意します。
2. Windows では、先に [Git for Windows](https://git-scm.com/downloads/win) をインストールします。
3. Claude Code 本体をインストールします。Windows では PowerShell で次を実行します。
   ```
   irm https://claude.ai/install.ps1 | iex
   ```
4. IntelliJ IDEA の「設定 → プラグイン → Marketplace」で「Claude Code」を検索してインストールし、IDE を再起動します。検索結果には Anthropic 以外が作った似た名前のプラグインも表示されます。プラグイン名の下に書かれた提供元が「Anthropic」になっているものを選んでください。
5. プロジェクトを開いて、ターミナルで `claude` を実行します(`Ctrl+Esc` でも起動できます)。初回はブラウザでログインします。

起動すると `CLAUDE.md` が自動で読み込まれ、このプロジェクトの設計方針に沿って作業します。

## ライセンス

[LICENSE](LICENSE) を参照してください。

## kubotan へのメモ

リポジトリを新規作成したら、**必ず Watch を設定すること**(忘れない!)。
GitHub の自動 Watch 機能は 2025 年 5 月に廃止されたため、設定しないと他の人が立てた issue や PR の通知が届かない。

1. リポジトリのページ右上の **「Watch」** を押す
2. **「Custom」** を選び、**Issues** と **Pull requests** にチェックを入れる

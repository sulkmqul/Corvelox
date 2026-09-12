# 単語IDの束を作成するスクリプト

`main.py`は、単語データのJSONからIDを入力順に読み取り、指定件数ごとの束をJSONで出力するPythonスクリプトです。levelによる絞り込みと、束の名前へのprefix追加に対応しています。

## 実行環境

- Python 3.6以上
- Python標準ライブラリのみを使用するため、追加パッケージのインストールは不要です。

以下のコマンド例は、`main.py`があるフォルダで実行してください。相対パスはコマンドを実行したフォルダを基準に解決されます。

## オプション

```text
python main.py --input INPUT --output OUTPUT --size SIZE [--level LEVEL] [--prefix PREFIX]
```

| オプション | 必須 | 説明 |
| --- | --- | --- |
| `--input` | はい | 入力する単語JSONのパス。 |
| `--output` | はい | 出力するJSONのパス。既存ファイルは上書きします。 |
| `--size` | はい | 1束に含める最大件数。1以上の整数を指定します。 |
| `--level` | いいえ | 対象とするlevelの整数値を1つ指定します。省略時は全件が対象です。 |
| `--prefix` | いいえ | 束の名前の先頭に付ける文字列。省略または空文字列の場合は付けません。 |
| `-h`, `--help` | いいえ | ヘルプを表示して終了します。 |

## コマンド例

### 全単語を10件ずつ分割

```powershell
python main.py --input toeic_vocab_3000_complete.json --output bundles.json --size 10
```

IDが1から順番に並んでいる場合、束の名前は`1-10`、`11-20`のようになります。

### levelが1の単語を10件ずつ分割

```powershell
python main.py --input toeic_vocab_3000_complete.json --output bundles_level1.json --size 10 --level 1
```

先にlevelで絞り込み、その結果を入力順に10件ずつ分割します。

### 名前にprefixを追加

```powershell
python main.py --input toeic_vocab_3000_complete.json --output bundles_toeic.json --size 10 --prefix "TOEIC"
```

束の名前は`TOEIC 1 - 10`のようになります。

### levelとprefixを組み合わせる

```powershell
python main.py --input toeic_vocab_3000_complete.json --output bundles_level2.json --size 20 --level 2 --prefix "TOEIC Level 2"
```

空白を含むprefixやファイルパスは、上記のように引用符で囲んでください。

### ヘルプを表示

```powershell
python main.py --help
```

## 入力JSON

最上位を配列とし、各単語をオブジェクトで記述します。

```json
[
  { "id": 1, "word": "achieve", "level": 1 },
  { "id": 5, "word": "address", "level": 2 },
  { "id": 6, "word": "advertise", "level": 1 }
]
```

- 全単語に整数の`id`が必要です。
- `--level`を指定する場合は、全単語に整数の`level`も必要です。
- `word`など、その他の項目は処理に使用しません。
- 文字コードはUTF-8です。UTF-8のBOM付きファイルも読み込めます。
- 標準JSONを入力してください。コメントや配列・オブジェクト末尾の余分なカンマは使用できません。サンプルファイルに構文エラーがある場合は、修正してから実行してください。

## 出力JSONと分割ルール

上記の入力を`--size 2 --prefix "TOEIC"`で処理すると、以下を出力します。

```json
[
  {
    "name": "TOEIC 1 - 5",
    "idList": [1, 5]
  },
  {
    "name": "TOEIC 6 - 6",
    "idList": [6]
  }
]
```

- 入力配列の順序を維持します。IDの並べ替え、欠番の補完、重複の除去は行いません。
- `name`には束の先頭と末尾のIDを使用します。
- prefix指定時の形式は`prefix 開始id - 終了id`です。prefixなしの場合は`開始id-終了id`です。
- 最後の束は、指定件数に満たなくても出力します。1件だけの束では開始IDと終了IDが同じになります。
- 対象が0件の場合は`[]`を出力します。
- 出力はUTF-8・インデント付きJSONです。

## 制約とエラー

- 入力と出力に同じファイルを指定することはできません。
- 出力先の親フォルダは事前に作成してください。
- 入力全体をメモリに読み込むため、大きなファイルでは使用メモリが増えます。
- ファイルの読み書きに失敗した場合、不正なJSON、必要な項目の欠落、不正な引数などはエラーとして表示し、終了コードは0以外になります。正常終了時は0です。

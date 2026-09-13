"""単語JSONを、指定件数ごとのIDの束に変換する。"""

import argparse
import json
import uuid
from pathlib import Path


def positive_integer(value):
    """引数valueを正の整数に変換して返す。不正な値は例外にする。"""
    number = int(value)
    if number <= 0:
        raise argparse.ArgumentTypeError("1以上の整数を指定してください")
    return number


def create_bundles(words, size, level=None, prefix=None):
    """入力順を保ち、単語のIDを束にまとめる。

    Args:
        words: idを持つ単語オブジェクトの配列。
        size: 1束に含める最大件数（正の整数）。
        level: 抽出するlevel。Noneの場合は全件を対象とする。
        prefix: 束の名前に付ける文字列。省略または空文字列の場合は付けない。

    Returns:
        UUID文字列のid、name、idListを持つ辞書の配列。
    """
    if type(size) is not int or size <= 0:
        raise ValueError("sizeは1以上の整数である必要があります")
    if not isinstance(words, list):
        raise ValueError("入力JSONの最上位は配列である必要があります")

    ids = []
    for position, word in enumerate(words, start=1):
        if not isinstance(word, dict):
            raise ValueError(f"{position}件目の単語はオブジェクトである必要があります")
        if type(word.get("id")) is not int:
            raise ValueError(f"{position}件目のidは整数である必要があります")
        if level is not None:
            if type(word.get("level")) is not int:
                raise ValueError(f"{position}件目のlevelは整数である必要があります")
            if word["level"] != level:
                continue
        ids.append(word["id"])

    bundles = []
    for index, start in enumerate(range(0, len(ids), size), start=1):
        group = ids[start : start + size]
        name = (
            f"{prefix} - {index:03d}"
            if prefix
            else f"{index:03d}"
        )
        bundles.append({"id": str(uuid.uuid4()), "name": name, "idList": group})
    return bundles


def main():
    """コマンドライン引数に従ってJSONを変換・保存する。戻り値はNone。"""
    parser = argparse.ArgumentParser(
        description="単語JSONのIDを、指定した件数ごとの束にまとめます。"
    )
    parser.add_argument("--input", type=Path, required=True, help="入力JSONのパス")
    parser.add_argument("--output", type=Path, required=True, help="出力JSONのパス")
    parser.add_argument(
        "--size", type=positive_integer, required=True, help="1束の単語数（1以上）"
    )
    parser.add_argument("--level", type=int, help="対象のlevel（省略時は全件）")
    parser.add_argument("--prefix", help="束の名前に付けるprefix（省略時はなし）")
    args = parser.parse_args()

    try:
        if args.input.resolve() == args.output.resolve() or (
            args.output.exists() and args.input.samefile(args.output)
        ):
            raise ValueError("入力と出力には異なるファイルを指定してください")
        words = json.loads(args.input.read_text(encoding="utf-8-sig"))
        bundles = create_bundles(words, args.size, args.level, args.prefix)
        args.output.write_text(
            json.dumps(bundles, ensure_ascii=False, indent=2) + "\n",
            encoding="utf-8",
        )
    except (OSError, ValueError) as exc:
        parser.exit(1, f"エラー: {exc}\n")

    print(f"{len(bundles)}束を出力しました: {args.output}")


if __name__ == "__main__":
    main()

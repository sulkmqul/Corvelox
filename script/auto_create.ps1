$SIZE = 20
python main.py --input "..\data\wordlist.json" --output "..\data\all.json" --size $SIZE
python main.py --input "..\data\wordlist.json" --output "..\data\lv1.json" --size $SIZE --level 1
python main.py --input "..\data\wordlist.json" --output "..\data\lv2.json" --size $SIZE --level 2
python main.py --input "..\data\wordlist.json" --output "..\data\lv3.json" --size $SIZE --level 3


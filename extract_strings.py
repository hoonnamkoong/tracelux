import re
import sys

# Set stdout encoding to utf-8
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

with open(r'C:\Users\Hoon_DT\gemini\tracelux\tracelux_decompiled\sources\com\hoonnk\landscapephotoassistant\ui\screens\SkyScreenKt.java', 'r', encoding='utf-8') as f:
    content = f.read()

strings = re.findall(r'"([^"]*)"', content)
for s in strings:
    if len(s) > 10:
        print(s)

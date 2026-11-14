import urllib.request
import re

for sid in ['1', '2', '10', '27', '28', '29', '30']:
    url = f'http://localhost:8080/story?action=detail&id={sid}'
    with urllib.request.urlopen(url) as res:
        html = res.read().decode('utf-8')
        is_comic = 'COMIC' in html or 'truyện tranh' in html.lower() or 'comic' in html.lower()
        title_match = re.search(r'<h1[^>]*>(.*?)</h1>', html, re.DOTALL)
        t = title_match.group(1).strip() if title_match else 'Unknown'
        print(f"Story {sid}: {t} | IsComic: {is_comic}")

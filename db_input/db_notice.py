import requests
from bs4 import BeautifulSoup
import re
import chromadb
from openai import OpenAI
import os
import time

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
}

board_kor_names = {
    "FA1": "일반공지",                              #30055 28574
    "FA2": "학사공지",                              #9492
    "econo01": "정경대학/공지사항",                 #3151
    "20013DA1": "공과대학/공지사항",                #15776
    "human01": "인문대학/공지사항",                 #1939
    "scien01": "자연과학대학/공지사항",             #1867
    "artandsport01": "예술체육대학/공지사항",       #1286
    "clacds01": "자유융합대학/공지사항"             #1829
}

def crawled_multiple_parts(url):
    response = requests.get(url, headers=headers)
    soup = BeautifulSoup(response.text, 'html.parser')

    base_div_1 = soup.select_one('#contents > div > div:nth-of-type(1) > div:nth-of-type(1)')
    if base_div_1:
        h4_text = base_div_1.select_one('h4')
        span1 = base_div_1.select_one('div > div > span:nth-of-type(1)')
        span2 = base_div_1.select_one('div > div > span:nth-of-type(2)')
        span3 = base_div_1.select_one('div > div > span:nth-of-type(3)')
        h4_text = h4_text.get_text(strip=True) if h4_text else ''
        span1 = span1.get_text(strip=True) if span1 else ''
        span2 = span2.get_text(strip=True) if span2 else ''
        span3 = span3.get_text(strip=True) if span3 else ''
        part1_text = f"{h4_text}\n{span1}\n{span2}\n{span3}"
    else:
        part1_text = '첫 번째 영역을 찾을 수 없습니다.'

    base_div_3 = soup.select_one('#contents > div > div:nth-of-type(1) > div:nth-of-type(3)')
    part3_text = base_div_3.get_text(strip=True) if base_div_3 else '세 번째 영역을 찾을 수 없습니다.'

    combined_text = part1_text + '\n\n' + part3_text
    return combined_text

def format_crawled_text(text, list_id, source):
    lines = text.splitlines()
    title = lines[0] if len(lines) > 0 else ''
    name = lines[1] if len(lines) > 1 else ''
    department = lines[2] if len(lines) > 2 else ''
    date_line = lines[3] if len(lines) > 3 else ''
    hashtags = lines[-1] if len(lines) > 0 else ''

    def clean_field(s):
        return s.strip()

    title_c = clean_field(title)
    name_c = clean_field(name)
    department_c = clean_field(department)
    hashtags_c = clean_field(hashtags)

    match = re.search(r'(\d{4}-\d{2}-\d{2})\(등록일\s*:\s*(\d{4}-\d{2}-\d{2})\)', date_line)
    if match:
        date = match.group(1)
        reg_date = match.group(2)
    else:
        date = ''
        reg_date = ''

    if not (title_c or name_c or department_c or date or reg_date or hashtags_c):
        return None, None

    tags = [tag for tag in hashtags.split('#') if tag.strip() != '']
    hashtags_comma = ', '.join(f"#{tag.strip()}" for tag in tags)

    kor_board = board_kor_names.get(list_id, list_id)

    formatted_text = (
        f"{kor_board}\n"
        f"제목: {title}\n"
        f"작성자: {name}\n"
        f"부서: {department}\n"
        f"수정일: {date}\n"
        f"등록일: {reg_date}\n"
        f"해시태그: {hashtags_comma}\n"
        f"출처: {source}"
    )
    metadata = {
        "list_id": list_id,
        "title": title,
        "name": name,
        "department": department,
        "update_date": date,
        "register_date": reg_date,
        "hashtags": hashtags_comma,
        "source": source
    }
    return formatted_text, metadata

def crawl_collect_data(list_ids, seq_start, seq_end):
    documents = []
    metadatas = []
    ids = []
    for list_id in list_ids:
        for seq in range(seq_start, seq_end + 1):
            url = f"https://www.uos.ac.kr/korNotice/view.do?list_id={list_id}&seq={seq}&identified=anonymous"
            try:
                raw = crawled_multiple_parts(url)
                formatted, metadata = format_crawled_text(raw, list_id, url)
                if formatted is None:
                    print(f"notice-{list_id}-{seq} 빈값, 건너뜀: {url}")
                    continue
                doc_id = f"notice-{list_id}-{seq}"
                documents.append(formatted)
                metadatas.append(metadata)
                ids.append(doc_id)
                print(f"{doc_id} 수집 완료")
                time.sleep(1)  # 속도 조절
            except Exception as e:
                print(f"notice-{list_id}-{seq} 실패: {e}")
    return documents, metadatas, ids

client_openai = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))

def get_embedding(text):
    response = client_openai.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding

def main():
    list_ids = ["FA1"]  # 필요시 추가
    seq_start, seq_end = 28574, 30055

    docs, metas, ids = crawl_collect_data(list_ids, seq_start, seq_end)

    client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
    collection = client.get_or_create_collection(name="SpringAiCollection")

    batch_size = 5
    for i in range(0, len(docs), batch_size):
        batch_docs = docs[i:i+batch_size]
        batch_meta = metas[i:i+batch_size]
        batch_ids = ids[i:i+batch_size]
        embeddings = [get_embedding(doc) for doc in batch_docs]
        try:
            collection.add(
                documents=batch_docs,
                metadatas=batch_meta,
                ids=batch_ids,
                embeddings=embeddings
            )
            print(f"batch {i//batch_size + 1} 저장 및 임베딩 완료")
            time.sleep(1)
        except Exception as e:
            print(f"batch {i//batch_size + 1} 오류: {e}")

if __name__ == "__main__":
    main()

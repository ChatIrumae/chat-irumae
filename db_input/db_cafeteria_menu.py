import requests
from bs4 import BeautifulSoup
from datetime import date, timedelta

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
}

def get_menu(url):
    response = requests.get(url, headers=headers)
    soup = BeautifulSoup(response.text, 'html.parser')
    day_table = soup.find(id="day")
    meal_labels = ["조식", "중식", "석식"]
    target_indexes = [18, 20, 22]
    
    if day_table is None:
        return ["메뉴 정보 없음"]
    
    text = day_table.get_text()
    lines = text.splitlines()
    
    def is_menu_empty(content):
        if not content:
            return True
        keywords = ["글이 없습니다.", "연휴", "휴무", 
        "중식11:30~14:00석식17:30~18:30"] # 자연과학관 조식이 "중식11:30~14:00석식17:30~18:30"로 기록되어 예외처리
        for kw in keywords:
            if kw in content:
                return True
        return False
    
    menus = []
    for idx in target_indexes:
        if idx < len(lines):
            content = lines[idx].strip()
            if not is_menu_empty(content):
                menus.append(content)
    
    if not menus:
        return ["메뉴 정보 없음"]
    else:
        result = []
        for label, idx in zip(meal_labels, target_indexes):
            if idx < len(lines):
                content = lines[idx].strip()
                if is_menu_empty(content):
                    content = "메뉴 정보 없음"
                result.append(f"{label}: {content}")
        return result


cafeteria_codes = {
    "010": "100주년기념관 이룸라운지 (중식 11:30~14:00 / 석식 17:30~19:00)",
    "020": "학생회관 1층식당 (각 코너별 시간 참고)",
    "030": "양식당 (중식 11:30~13:50)",
    "040": "자연과학관 (중식 11:30~14:00 / 석식 17:30~18:30)"
}

start_date = date(2025, 11, 1)
end_date = date(2025, 11, 16)
delta = timedelta(days=1)

with open("cafeteria_menu.txt", "w", encoding="utf-8") as f:
    d = start_date
    while d <= end_date:
        search_date = d.strftime("%Y%m%d")
        for code, name in cafeteria_codes.items():
            url = f"https://www.uos.ac.kr/food/placeList.do?identified=anonymous&rstcde={code}&search_date={search_date}"
            menu = get_menu(url)
            print(search_date + "-" + code + " 크롤링 중...")
            f.write("-" * 40 + "\n")
            f.write(f"{d} | {name} [{code}]\n")
            if menu:
                for line in menu:
                    f.write(line + "\n")
            else:   
                f.write("메뉴 정보 없음\n")
        d += delta

import chromadb
from openai import OpenAI
import os

# OpenAI 클라이언트 초기화
client_openai = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))

# OpenAI 임베딩 함수
def get_embedding(text):
    response = client_openai.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding

def load_cafeteria_file(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    # 각 식단 블록 기준으로 분리
    blocks = content.split("-" * 40)
    records = [b.strip() for b in blocks if b.strip()]
    return records

def parse_cafeteria_record(record):
    # 날짜와 식당명 파싱 (첫 줄에 "날짜 | 식당명 [코드]" 형태임)
    lines = record.split("\n")
    header = lines[0]
    menu_lines = lines[1:]
    
    import re
    m = re.match(r"(\d{4}-\d{2}-\d{2}) \| (.+) \[(\d{3})\]", header)
    if not m:
        print(f"헤더 형식 오류: {header}")
        date_str = ""
        cafeteria_name = ""
        cafeteria_code = ""
    else:
        date_str, cafeteria_name, cafeteria_code = m.groups()
    
    # 메뉴 텍스트 합치기
    menu_text = "\n".join(menu_lines).strip()
    # 문서 = 날짜+식당명+메뉴 텍스트 요약 아닌 전체 원문 저장 가능
    document = f"{date_str} {cafeteria_name} \n{menu_text}"
    
    metadata = {
        "source": "uos_cafeteria_crawling",
        "date": date_str,
        "cafeteria_code": cafeteria_code,
        "cafeteria_name": cafeteria_name
    }
    
    # 고유 id는 날짜와 식당 코드 결합
    id_str = f"menu-{date_str.replace('-', '')}-{cafeteria_code}"
    
    return document, metadata, id_str
def build_collections(records):
    documents = []
    metadatas = []
    ids = []
    for rec in records:
        doc, meta, id_str = parse_cafeteria_record(rec)
        documents.append(doc)
        metadatas.append(meta)
        ids.append(id_str)
    return documents, metadatas, ids

# 배치 단위로 데이터 추가 함수 (OpenAI 임베딩 사용, batch_size=10)
def batch_add(collection, documents, metadatas, ids, batch_size=10):
    total = len(documents)
    for i in range(0, total, batch_size):
        batch_docs = documents[i:i+batch_size]
        batch_meta = metadatas[i:i+batch_size]
        batch_ids = ids[i:i+batch_size]

        # OpenAI 임베딩 생성
        embeddings = [get_embedding(doc) for doc in batch_docs]

        collection.add(
            documents=batch_docs,
            metadatas=batch_meta,
            ids=batch_ids,
            embeddings=embeddings
        )
        print(f"Batch {i // batch_size + 1}: {len(batch_docs)}개 문서 추가 완료.")

# 메인 함수
def main():
    # ChromaDB 서버 연결
    client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
    print("ChromaDB 서버에 성공적으로 연결되었습니다.")

    collection_name = "SpringAiCollection"
    collection = client.get_or_create_collection(name=collection_name)
    print(f"'{collection_name}' 컬렉션을 준비했습니다.")

    # 텍스트 파일 로드, 파싱 (cafeteria_menu.txt)
    records = load_cafeteria_file("cafeteria_menu.txt")
    documents, metadatas, ids = build_collections(records)

    # 배치 단위로 데이터 추가
    batch_add(collection, documents, metadatas, ids, batch_size=10)

    print(f"총 {len(documents)}개의 문서를 컬렉션에 저장 완료.")

if __name__ == "__main__":
    main()
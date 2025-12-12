import requests
from bs4 import BeautifulSoup
from datetime import date, timedelta
import chromadb
from openai import OpenAI
import os
import re

# ==========================================
# 1. 크롤링 및 텍스트 파일 저장 파트
# ==========================================

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
}

def get_menu_by_meal(url):
    """
    URL에서 조식, 중식, 석식 데이터를 분리하여 반환합니다.
    Return: [{"meal": "조식", "content": "메뉴..."}, ...]
    """
    try:
        response = requests.get(url, headers=headers)
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # XPath: //*[@id="day"]/table/tbody/tr/td[1] ~ [3]
        # CSS Selector로 변환: #day table tbody tr td
        day_table_row = soup.select_one('#day table tbody tr')
        
        if not day_table_row:
            return []

        cells = day_table_row.find_all('td')
        meal_labels = ["조식", "중식", "석식"]
        results = []

        # 셀 개수가 3개 미만일 경우 예외 처리
        if len(cells) < 3:
            return []

        for i, label in enumerate(meal_labels):
            # td[1] -> index 0, td[2] -> index 1, td[3] -> index 2
            content = cells[i].get_text(separator="\n").strip()
            
            results.append({"meal": label, "content": content})
            
        return results
        
    except Exception as e:
        print(f"Error parsing {url}: {e}")
        return []

def is_menu_valid(content):
    if not content:
        return False
    keywords = ["연휴", "휴무",
                "중식\n11:30~14:00\n석식\n17:30~18:30"] # 예외 키워드 유지
    for kw in keywords:
        if kw in content:
            return False
    return True

cafeteria_codes = {
    "010": "100주년기념관 이룸라운지",
    "020": "학생회관 1층식당",
    "030": "양식당",
    "040": "자연과학관"
}

start_date = date(2025, 6, 2)
end_date = date(2025, 12, 1)
delta = timedelta(days=1)

# 크롤링 실행 및 파일 저장
print("크롤링 시작...")
with open("cafeteria_menu.txt", "w", encoding="utf-8") as f:
    d = start_date
    while d <= end_date:
        search_date = d.strftime("%Y%m%d")
        for code, name in cafeteria_codes.items():
            url = f"https://www.uos.ac.kr/food/placeList.do?identified=anonymous&rstcde={code}&search_date={search_date}"
            
            # 조/중/석식 분리된 데이터 가져오기
            meals = get_menu_by_meal(url)
            
            print(f"{search_date}-{code} 처리 중...")
            
            if not meals:
                # 데이터가 아예 없는 경우도 기록 (선택 사항)
                continue

            for meal_data in meals:
                meal_type = meal_data['meal']
                content = meal_data['content']
                
                # [수정됨] 유효하지 않은 메뉴는 파일 쓰기를 건너뜀 (DB에도 안 들어감)
                if not is_menu_valid(content):
                    continue

                # 구분자 라인
                f.write("-" * 40 + "\n")
                # 헤더 포맷 변경: 날짜 | 식당명 [코드] | 식사종류
                f.write(f"{d} | {name} [{code}] | {meal_type}\n")
                f.write(content + "\n")
                
        d += delta
print("크롤링 및 파일 저장 완료.")


# ==========================================
# 2. 벡터 DB 저장 파트 (ChromaDB)
# ==========================================

# OpenAI 클라이언트 초기화
client_openai = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))

def get_embedding(text):
    text = text.replace("\n", " ")
    response = client_openai.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding

def load_cafeteria_file(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    # 구분선 기준으로 블록 분리
    blocks = content.split("-" * 40)
    records = [b.strip() for b in blocks if b.strip()]
    return records

def parse_cafeteria_record(record):
    lines = record.split("\n")
    header = lines[0]
    menu_lines = lines[1:]
    
    # 정규표현식 업데이트: 날짜 | 식당명 [코드] | 식사종류
    # 예: 2025-11-01 | 학생회관 1층식당 [020] | 중식
    pattern = r"(\d{4}-\d{2}-\d{2}) \| (.+) \[(\d{3})\] \| (.+)"
    m = re.match(pattern, header)
    
    if not m:
        print(f"헤더 형식 오류: {header}")
        return None, None, None
    
    date_str, cafeteria_name, cafeteria_code, meal_type = m.groups()
    
    menu_text = "\n".join(menu_lines).strip()
    
    # 문서 내용 구성: 질문에 답변하기 좋게 텍스트 구성
    document = f"날짜: {date_str}\n식당: {cafeteria_name}\n구분: {meal_type}\n메뉴:\n{menu_text}"
    
    metadata = {
        "source": "uos_cafeteria_crawling",
        "date": date_str,
        "cafeteria_code": cafeteria_code,
        "cafeteria_name": cafeteria_name,
        "meal_type": meal_type  # 메타데이터에 식사 종류 추가
    }
    
    # 고유 ID 생성: 날짜-식당코드-식사종류 (중복 방지)
    # 예: menu-20251101-020-lunch
    meal_suffix = {"조식": "breakfast", "중식": "lunch", "석식": "dinner"}.get(meal_type, "unknown")
    id_str = f"menu-{date_str.replace('-', '')}-{cafeteria_code}-{meal_suffix}"
    
    return document, metadata, id_str

def build_collections(records):
    documents = []
    metadatas = []
    ids = []
    for rec in records:
        doc, meta, id_str = parse_cafeteria_record(rec)
        if doc is not None:
            documents.append(doc)
            metadatas.append(meta)
            ids.append(id_str)
    return documents, metadatas, ids

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

def main():
    # ChromaDB 서버 연결
    try:
        client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
        print("ChromaDB 서버에 성공적으로 연결되었습니다.")
        
        collection_name = "SpringAiCollection"
        collection = client.get_or_create_collection(name=collection_name)
        print(f"'{collection_name}' 컬렉션을 준비했습니다.")

        # 파일 로드 및 파싱
        records = load_cafeteria_file("cafeteria_menu.txt")
        documents, metadatas, ids = build_collections(records)

        if documents:
            # 배치 단위로 데이터 추가
            batch_add(collection, documents, metadatas, ids, batch_size=10)
            print(f"총 {len(documents)}개의 문서를 컬렉션에 저장 완료.")
        else:
            print("저장할 데이터가 없습니다.")
            
    except Exception as e:
        print(f"오류 발생: {e}")

if __name__ == "__main__":
    main()

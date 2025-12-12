import chromadb
from openai import OpenAI
import os
import re

# OpenAI 클라이언트 초기화
client_openai = OpenAI(api_key=os.getenv('OPENAI_API_KEY'))

# OpenAI 임베딩 함수
def get_embedding(text):
    # 텍스트가 비어있으면 기본값 반환 혹은 예외 처리
    if not text:
        return []
    response = client_openai.embeddings.create(
        model="text-embedding-3-small",
        input=text
    )
    return response.data[0].embedding

# 파일 읽기 - '======' 구분자로 각 조항 분리
def load_regulations_file(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    
    # 파일 내 구분자인 '======' 로 분리
    raw_records = content.split("======")
    # 공백만 있는 항목 제거
    records = [r.strip() for r in raw_records if r.strip()]
    return records

# 각 조항 텍스트를 구조화된 dict로 변환
def parse_regulation(record):
    lines = record.split("\n")
    
    # 첫 줄은 제목 (예: 제1조(목적), 제18조의2(성적평가))
    title = lines[0].strip()
    content = "\n".join(lines[1:]).strip()
    
    # ID 생성을 위한 번호 추출 로직 변경
    # 1. "제18조의2" 형태 감지 -> group(1)=18, group(3)=2
    # 2. "제1조" 형태 감지 -> group(1)=1, group(3)=None
    match = re.search(r"제(\d+)조(의(\d+))?", title)
    
    if match:
        main_num = match.group(1)      # 본 번호 (예: 1, 18)
        sub_num = match.group(3)       # 가지 번호 (예: 2) - 없을 수도 있음
        
        # 본 번호에 0 채우기 (1 -> 01, 10 -> 10)
        main_num_str = main_num.zfill(2)
        
        if sub_num:
            # 가지 번호가 있는 경우: rule-18-2
            id_suffix = f"{main_num_str}-{sub_num}"
        else:
            # 가지 번호가 없는 경우: rule-01
            id_suffix = f"{main_num_str}"
            
        article_id = f"rule-{id_suffix}"
    else:
        article_id = f"rule-unknown-{title[:5]}" # 예외 처리

    return {
        "title": title,
        "content": content,
        "article_id": article_id,  # 생성된 ID 저장
        "full_text": f"{title}\n{content}"
    }

# 문서, 메타데이터, 고유 id 만들기
def build_collections(records):
    documents = []
    metadatas = []
    ids = []

    for rec in records:
        data = parse_regulation(rec)
        
        documents.append(data['full_text'])
        
        metadatas.append({
            "title": data['title'],
            "original_id": data['article_id'] # 메타데이터에도 ID 정보 남김
        })
        
        # 위에서 만든 ID 사용
        ids.append(data['article_id'])

    return documents, metadatas, ids

# 배치 단위로 데이터 추가 함수 (OpenAI 임베딩 사용, batch_size=10)
def batch_add(collection, documents, metadatas, ids, batch_size=10):
    total = len(documents)
    for i in range(0, total, batch_size):
        batch_docs = documents[i:i+batch_size]
        batch_meta = metadatas[i:i+batch_size]
        batch_ids = ids[i:i+batch_size]

        # OpenAI 임베딩 생성
        try:
            embeddings = [get_embedding(doc) for doc in batch_docs]
            
            collection.add(
                documents=batch_docs,
                metadatas=batch_meta,
                ids=batch_ids,
                embeddings=embeddings
            )
            print(f"Batch {i // batch_size + 1}: {len(batch_docs)}개 문서 추가 완료. (ID: {batch_ids[0]} ~ {batch_ids[-1]})")
        except Exception as e:
            print(f"Error adding batch {i}: {e}")

# 메인 함수
def main():
    # ChromaDB 서버 연결 (기존 설정 유지)
    try:
        client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
        print("ChromaDB 서버에 성공적으로 연결되었습니다.")
    except Exception as e:
        print(f"ChromaDB 연결 실패: {e}")
        return

    collection_name = "SpringAiCollection" # 컬렉션 이름 변경
    
    # 기존 컬렉션이 있다면 가져오고, 없으면 생성
    collection = client.get_or_create_collection(name=collection_name)
    print(f"'{collection_name}' 컬렉션을 준비했습니다.")

    # 텍스트 파일 로드
    filename = "school_regulations.txt" # 업로드한 파일명
    if not os.path.exists(filename):
        print(f"파일을 찾을 수 없습니다: {filename}")
        return

    records = load_regulations_file(filename)
    print(f"파일에서 총 {len(records)}개의 조항을 읽어왔습니다.")

    # 데이터 구축
    documents, metadatas, ids = build_collections(records)

    # 배치 단위로 데이터 추가
    batch_add(collection, documents, metadatas, ids, batch_size=10)

    print(f"총 {len(documents)}개의 문서를 컬렉션에 저장 완료.")

if __name__ == "__main__":
    main()

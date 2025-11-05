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

# 파일 읽기 - 각 항목 구분 후 리스트 반환
def load_building_room_file(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    raw_records = content.split("-" * 40)
    records = [r.strip() for r in raw_records if r.strip()]
    return records

# 각 항목을 key:value dict로 변환
def parse_record(record):
    data = {}
    for line in record.split("\n"):
        if ":" in line:
            key, val = line.split(":", 1)
            data[key.strip()] = val.strip()
    return data

# 문서, 메타데이터, 고유 id 만들기 (장소 정보용)
def build_collections(records):
    documents = []
    metadatas = []
    ids = []

    for rec in records:
        data = parse_record(rec)
        # 장소 관련 요약 문장, 자연어화
        doc_text = (
            f"{data.get('분반번호(공간명)', '')}, "
            f"{data.get('건물이름', '')} {data.get('강의실명', '')}에 있습니다."
        )
        documents.append(doc_text)
        metadatas.append(data)
        # 고유 ID: 건물코드-강의실코드
        bldg_cd = data.get('건물코드', '')
        room_cd = data.get('강의실코드', '')
        id_str = f"location-{bldg_cd}-{room_cd}"
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

    # 텍스트 파일 로드, 파싱 (장소 파일로 변경)
    records = load_building_room_file("building_room_info.txt")
    documents, metadatas, ids = build_collections(records)

    # 배치 단위로 데이터 추가
    batch_add(collection, documents, metadatas, ids, batch_size=10)

    print(f"총 {len(documents)}개의 문서를 컬렉션에 저장 완료.")

if __name__ == "__main__":
    main()
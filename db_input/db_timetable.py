import chromadb

# 파일 읽기 - 각 항목 구분 후 리스트 반환
def load_timetable_file(filepath):
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

# 문서, 메타데이터, 고유 id 만들기
def build_collections(records):
    documents = []
    metadatas = []
    ids = []
    
    for rec in records:
        data = parse_record(rec)
        doc_text = (
            f"{data.get('학년도', '')}년 {data.get('학기', '')} "
            f"{data.get('교과구분', '')} 과목 '{data.get('교과목명', '')}' 분반 {data.get('분반번호', '')}, "
            f"{data.get('학년', '')}학년 대상. 담당교수: {data.get('담당교수명', '')}. "
            f"강의시간: {data.get('강의시간 및 강의실', '')}. 학점 {data.get('학점', '')}점."
        )
        documents.append(doc_text)
        metadatas.append(data)
        
        year = data.get("학년도", "")
        term = data.get("학기", "").replace("학기", "")
        subject_no = data.get("교과목번호", "")
        dvcl_no = data.get("분반번호", "")
        id_str = f"{year}-{term}-{subject_no}-{dvcl_no}"
        ids.append(id_str)
    
    return documents, metadatas, ids

# batch 단위로 데이터 추가 함수
def batch_add(collection, documents, metadatas, ids, batch_size=100):
    total = len(documents)
    for i in range(0, total, batch_size):
        batch_docs = documents[i:i+batch_size]
        batch_meta = metadatas[i:i+batch_size]
        batch_ids = ids[i:i+batch_size]
        collection.add(
            documents=batch_docs,
            metadatas=batch_meta,
            ids=batch_ids
        )
        print(f"Batch {i // batch_size + 1}: {len(batch_docs)}개 문서 추가 완료.")

# 메인 함수
def main():
    # ChromaDB 서버 연결
    client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
    print("ChromaDB 서버에 성공적으로 연결되었습니다.")
    
    collection_name = "TimetableCollection"
    collection = client.get_or_create_collection(name=collection_name)
    print(f"'{collection_name}' 컬렉션을 준비했습니다.")
    
    # 텍스트 파일 로드, 파싱
    records = load_timetable_file("timetable_info.txt")
    documents, metadatas, ids = build_collections(records)
    
    # 배치 단위로 데이터 추가
    batch_add(collection, documents, metadatas, ids, batch_size=100)
    
    print(f"총 {len(documents)}개의 문서를 컬렉션에 저장 완료.")

if __name__ == "__main__":
    main()

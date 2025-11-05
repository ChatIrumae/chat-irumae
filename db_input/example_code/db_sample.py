import chromadb

# 1. ChromaDB 서버에 연결할 클라이언트 생성
#    - host: ChromaDB 서버의 IP 주소 (Docker로 로컬에 띄웠다면 'localhost')
#    - port: ChromaDB 서버의 포트 (기본값은 8000)
client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)

print("ChromaDB 서버에 성공적으로 연결되었습니다.")
print(f"ChromaDB 버전: {client.get_version()}")


# 2. 컬렉션(Collection) 가져오기 또는 생성하기
#    - get_or_create_collection은 컬렉션이 있으면 가져오고, 없으면 새로 만듭니다.
collection_name = "SpringAiCollection"
collection = client.get_or_create_collection(name=collection_name)

print(f"'{collection_name}' 컬렉션을 준비했습니다.")


# 3. 데이터(Document) 추가하기
collection.add(
    documents=[
        "This is a document about cats.",
        "This is a document about cars.",
        "This is a document about stars."
    ],
    metadatas=[
        {"category": "animal"}, 
        {"category": "vehicle"}, 
        {"category": "space"}
    ],
    ids=["id1", "id2", "id3"] # 각 문서의 고유 ID
)

print("3개의 문서를 컬렉션에 추가했습니다.")


# 4. 데이터 검색(Query)하기
query_text = "What is a fast animal?"
results = collection.query(
    query_texts=[query_text],
    n_results=2 # 가장 유사한 결과 2개를 반환
)

print(f"\n'{query_text}'와 유사한 문서를 검색합니다...")
print(results)
import chromadb

def delete_cafeteria_data():
    # 1. ChromaDB 서버 연결
    try:
        client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
        print("ChromaDB 서버에 연결되었습니다.")

        collection_name = "SpringAiCollection"
        # 컬렉션이 없으면 에러가 날 수 있으므로 get_collection 사용
        collection = client.get_collection(name=collection_name)
        
        # 2. 삭제 전 상태 확인
        before_count = collection.count()
        print(f"삭제 작업 전 전체 문서 개수: {before_count}")

        # 3. 조건부 삭제 실행
        # metadata 필드 중 "source" 값이 "uos_cafeteria_crawling"인 모든 문서를 삭제
        print("조건(source='uos_cafeteria_crawling')에 맞는 데이터를 삭제하는 중...")
        
        collection.delete(
            where={"source": "uos_cafeteria_crawling"}
        )

        # 4. 삭제 후 상태 확인
        after_count = collection.count()
        deleted_count = before_count - after_count
        
        print("-" * 30)
        print(f"삭제 후 전체 문서 개수: {after_count}")
        print(f"총 {deleted_count}개의 문서가 삭제되었습니다.")
        print("-" * 30)

    except Exception as e:
        print(f"작업 중 오류가 발생했습니다: {e}")

if __name__ == "__main__":
    delete_cafeteria_data()

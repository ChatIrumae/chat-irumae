import chromadb
import uuid
import os
import data

from dotenv import load_dotenv

load_dotenv()

OPENAI_API_KEY = os.environ.get('OPENAI_API_KEY')

# -------------------------------------------------------------
# 1. OpenAI API 키 확인
# -------------------------------------------------------------
# 코드 실행 전에 반드시 OPENAI_API_KEY 환경 변수를 설정해주세요.
if not OPENAI_API_KEY:
    print("OPENAI_API_KEY 환경 변수를 설정해주세요.")
    exit(1)
else:
    print("OpenAI API 키가 설정되었습니다.")

# -------------------------------------------------------------
# 2. 원격 ChromaDB 서버 연결 (ChromaDB 0.5.0 호환)
# -------------------------------------------------------------
# ChromaDB 0.5.0에서는 인증 없이 연결할 때 settings 매개변수를 사용합니다.
try:
    client = chromadb.HttpClient(
        host='54.180.203.59', 
        port=8000,
        settings=chromadb.Settings(allow_reset=True)
    )
    print("ChromaDB 서버에 연결되었습니다.")
except Exception as e:
    print(f"ChromaDB 연결 오류: {e}")
    # 대안: 로컬 ChromaDB 사용
    print("로컬 ChromaDB로 전환합니다...")
    client = chromadb.Client()
    print("로컬 ChromaDB에 연결되었습니다.")

# -------------------------------------------------------------
# 3. ChromaDB에 OpenAI 임베딩 함수 설정
# -------------------------------------------------------------
from chromadb.utils import embedding_functions

# OpenAI 임베딩 함수를 생성합니다.
# model_name을 지정하여 원하는 모델을 사용할 수 있습니다.
# 최신 모델: "text-embedding-3-small" (가성비), "text-embedding-3-large" (최고 성능)
openai_ef = embedding_functions.OpenAIEmbeddingFunction(
                OPENAI_API_KEY,
                model_name="text-embedding-3-small"
            )

# 컬렉션을 생성할 때, embedding_function으로 위에서 만든 OpenAI 함수를 지정합니다.
# 이렇게 하면 이 컬렉션의 모든 임베딩 작업이 자동으로 OpenAI API를 통해 처리됩니다.
collection_name = "SpringAiCollection_OpenAI"
collection = client.get_or_create_collection(
    name=collection_name,
    embedding_function=openai_ef
)
print(f"'{collection_name}' 컬렉션을 준비했습니다. 임베딩 모델: text-embedding-3-small")


# -------------------------------------------------------------
# 4. 데이터 준비 및 저장
# -------------------------------------------------------------
# 임시 테스트 데이터 (실제 크롤링 데이터가 없는 경우)
url = 'https://www.uos.ac.kr/food/placeList.do?rstcde=040&menuid=2000005006002000000&identified=anonymous&search_date=20251013'
crawled_data = data.crawled_url(url)

ids = [str(uuid.uuid4()) for _ in crawled_data]

# 데이터를 추가합니다.
# 이제 embeddings를 직접 만들어서 넣어줄 필요가 없습니다.
# documents만 제공하면, ChromaDB가 내부적으로 OpenAI API를 호출하여 임베딩을 생성하고 저장합니다.
print("\nOpenAI API를 통해 데이터를 임베딩하고 저장합니다...")
collection.add(
    documents=crawled_data,
    ids=["doc-1"]
)
print("데이터 저장을 완료했습니다.")


# -------------------------------------------------------------
# 5. 저장 결과 확인 (의미 기반 검색)
# -------------------------------------------------------------
count = collection.count()
print(f"\n현재 컬렉션에 저장된 총 문서 개수: {count}개")

# 검색할 때도 query_texts만 입력하면 알아서 임베딩 후 검색을 수행합니다.
results = collection.query(
    query_texts=["AI 애플리케이션 프레임워크"],
    n_results=1
)

print("\n[테스트 검색] 'AI 애플리케이션 프레임워크'와 가장 유사한 검색 결과:")
print(results['documents'])

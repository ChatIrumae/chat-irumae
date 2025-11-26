import chromadb
import math
from itertools import combinations

# 1. ChromaDB 서버 연결 및 임베딩 가져오기
def get_embeddings_by_ids(collection_name, target_ids):
    client = chromadb.HttpClient(host='chat-irumae.kro.kr', port=8000, ssl=True)
    collection = client.get_or_create_collection(name=collection_name)
    results = collection.get(ids=target_ids, include=["embeddings"])
    # 리스트 반환
    return results["ids"], results["embeddings"]

# 2. 코사인 유사도 함수
def cos_sim(arr1, arr2):
    if len(arr1) == len(arr2):
        dots = 0
        norm1 = 0
        norm2 = 0
        for i in range(len(arr1)):
            norm1 += arr1[i]**2
            norm2 += arr2[i]**2
            dots += arr1[i]*arr2[i]
        return dots/(math.sqrt(norm1*norm2))
    else:
        print(f"ERR! LENGTH NOT MATCH | arr1 => {len(arr1)} arr2 => {len(arr2)}")
        return 0

# 3. 유사도 계산 및 결과 저장
def save_similarity_results(ids, embeddings, output_file="similarity_results.txt"):
    num_embeddings = len(embeddings)
    sim_results = []
    for (i, j) in combinations(range(num_embeddings), 2):
        sim = cos_sim(embeddings[i], embeddings[j])
        sim_results.append((ids[i], ids[j], sim))
    with open(output_file, "w", encoding="utf-8") as f:
        for id1, id2, sim in sim_results:
            f.write(f"{id1} | {id2} | similarity: {sim}\n")
            print(f"{id1} | {id2} | similarity: {sim}")
    print(f"총 {len(sim_results)}개의 쌍 유사도 값을 '{output_file}'에 저장했습니다.")

### 사용 예시
if __name__ == "__main__":
    # 원하는 id 값 목록 입력
    target_ids = [
        "timetable-2025-1-01115-01",
        "timetable-2025-1-01115-03",
        "timetable-2025-1-01116-01",
        "timetable-2025-2-01115-01",
        "timetable-2025-2-01115-03",
        "timetable-2025-2-01116-01",
        "menu-20250305-010",
        "menu-20250305-020",
        "menu-20250305-030",
        "menu-20250305-040",
        "menu-20251111-010",
        "menu-20251111-020",
        "menu-20251111-030",
        "menu-20251111-040",
        "location-0028-001269",
        "location-0028-001270",
        "location-0028-001276",
        "location-0035-001865",
        "location-0035-001866",
        "location-0035-001921"
        # 원하는 만큼 추가
    ]
    collection_name = "SpringAiCollection"
    ids, embeddings = get_embeddings_by_ids(collection_name, target_ids)
    save_similarity_results(ids, embeddings, output_file="selected_similarity_results.txt")
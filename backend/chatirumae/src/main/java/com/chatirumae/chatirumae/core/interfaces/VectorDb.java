package com.chatirumae.chatirumae.core.interfaces;

import com.chatirumae.chatirumae.infra.ChromaDtos;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface VectorDb {
    public void addDocuments(String collectionName, List<String> ids, List<String> documents, List<Map<String, String>> metadatas);
    public ChromaDtos.QueryResult query(String collectionName, String queryText, int nResults);
}

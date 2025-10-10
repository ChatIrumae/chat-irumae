package com.chatirumae.chatirumae.infra;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;


@Repository
public class MongoConnection {
    private final MongoDatabase database;

    public MongoConnection() {
        String connectionString = "mongodb+srv://chat-irumae:u63rzo9SrJwInbzj@chat-irumae.xczfrhu.mongodb.net/";
        String databaseName = "chat-irumae";

        MongoClient mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(databaseName);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
}

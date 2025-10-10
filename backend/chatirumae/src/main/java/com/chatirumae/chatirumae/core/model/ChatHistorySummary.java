package com.chatirumae.chatirumae.core.model;

public class ChatHistorySummary {
    private String _id;
    private String title;

    // 기본 생성자
    public ChatHistorySummary() {}

    // 전체 생성자
    public ChatHistorySummary(String _id, String title) {
        this._id = _id;
        this.title = title;
    }

    // Getters and Setters
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "ChatHistorySummary{" +
                "_id='" + _id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}

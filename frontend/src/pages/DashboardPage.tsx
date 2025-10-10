import React, { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { tokenUtils, chatApi } from "../utils/api";
import HamburgerMenu from "../components/HamburgerMenu";
import "./DashboardPage.css";

export interface Message {
  id: string;
  content: string;
  sender: string;
  timestamp: Date;
  currentChatId?: string | null;
}

export interface ChatHistory {
  id: string;
  title: string;
  messages: Message[];
  createdAt: Date;
  updatedAt: Date;
}

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [chatHistories, setChatHistories] = useState<ChatHistory[]>([]);
  const [currentChatId, setCurrentChatId] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // UUID 생성 함수
  const generateChatId = (): string => {
    return (
      "chat_" +
      Date.now() +
      "_" +
      Math.random().toString(36).substr(2, 9) +
      "_" +
      Math.random().toString(36).substr(2, 9)
    );
  };

  // 컴포넌트 마운트 시 인증 확인 및 채팅 히스토리 로드
  useEffect(() => {
    if (!tokenUtils.isAuthenticated()) {
      navigate("/login");
    } else {
      loadChatHistories();
    }
  }, [navigate]);

  // 채팅 히스토리 로드
  const loadChatHistories = () => {
    const saved = localStorage.getItem("chatHistories");
    if (saved) {
      const histories = JSON.parse(saved).map((history: any) => ({
        ...history,
        createdAt: new Date(history.createdAt),
        updatedAt: new Date(history.updatedAt),
        messages: history.messages.map((msg: any) => ({
          ...msg,
          timestamp: new Date(msg.timestamp),
        })),
      }));
      setChatHistories(histories);
    }
  };

  // 채팅 히스토리 저장
  const saveChatHistories = (histories: ChatHistory[]) => {
    localStorage.setItem("chatHistories", JSON.stringify(histories));
    setChatHistories(histories);
  };

  // 새 채팅 시작
  const startNewChat = () => {
    setMessages([]);
    const newChatId = generateChatId();
    setCurrentChatId(newChatId);
    setIsMenuOpen(false);
  };

  // 채팅 히스토리 로드
  const loadChat = (chatId: string) => {
    const chat = chatHistories.find((h) => h.id === chatId);
    if (chat) {
      setMessages(chat.messages);
      setCurrentChatId(chatId);
      setIsMenuOpen(false);
    }
  };

  // 채팅 히스토리 삭제
  const deleteChat = (chatId: string) => {
    const updatedHistories = chatHistories.filter((h) => h.id !== chatId);
    saveChatHistories(updatedHistories);
    if (currentChatId === chatId) {
      startNewChat();
    }
  };

  // 메시지 목록이 업데이트될 때마다 스크롤을 맨 아래로
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputMessage.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputMessage.trim(),
      sender: tokenUtils.getStudentId()?.toString() || "user",
      timestamp: new Date(),
      currentChatId: currentChatId,
    };

    const updatedMessages = [...messages, userMessage];
    setMessages(updatedMessages);
    const currentMessage = inputMessage.trim();
    setInputMessage("");
    setIsLoading(true);

    try {
      // 실제 API 호출
      const response = await chatApi.sendMessage(userMessage);

      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: response || "죄송합니다. 응답을 받을 수 없습니다.",
        sender: "assistant",
        timestamp: new Date(),
        currentChatId: currentChatId,
      };

      const finalMessages = [...updatedMessages, assistantMessage];
      setMessages(finalMessages);

      // 채팅 히스토리 저장
      saveChatToHistory(finalMessages, currentMessage);
    } catch (error) {
      console.error("메시지 전송 오류:", error);

      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        content:
          "죄송합니다. 서버와의 통신 중 오류가 발생했습니다. 다시 시도해주세요.",
        sender: "assistant",
        timestamp: new Date(),
      };

      const finalMessages = [...updatedMessages, errorMessage];
      setMessages(finalMessages);
      saveChatToHistory(finalMessages, currentMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // 채팅을 히스토리에 저장
  const saveChatToHistory = (messages: Message[], firstMessage: string) => {
    const now = new Date();
    const chatId = currentChatId || generateChatId();

    const chatTitle =
      firstMessage.length > 30
        ? firstMessage.substring(0, 30) + "..."
        : firstMessage;

    const chatHistory: ChatHistory = {
      id: chatId,
      title: chatTitle,
      messages: messages,
      createdAt: currentChatId
        ? chatHistories.find((h) => h.id === currentChatId)?.createdAt || now
        : now,
      updatedAt: now,
    };

    let updatedHistories;
    if (currentChatId) {
      // 기존 채팅 업데이트
      updatedHistories = chatHistories.map((h) =>
        h.id === currentChatId ? chatHistory : h
      );
    } else {
      // 새 채팅 추가
      updatedHistories = [chatHistory, ...chatHistories];
      setCurrentChatId(chatId);
    }

    saveChatHistories(updatedHistories);
  };

  const handleLogout = () => {
    tokenUtils.logout();
    navigate("/login");
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage(e);
    }
  };

  return (
    <div className="dashboard-container">
      {/* 햄버거 메뉴 */}
      <HamburgerMenu
        isOpen={isMenuOpen}
        onClose={() => setIsMenuOpen(false)}
        onNewChat={startNewChat}
        onLoadChat={loadChat}
        onDeleteChat={deleteChat}
        chatHistories={chatHistories}
        currentChatId={currentChatId}
      />

      {/* 헤더 */}
      <header className="dashboard-header">
        <div className="header-content">
          <button
            className="hamburger-button"
            onClick={() => setIsMenuOpen(!isMenuOpen)}
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path
                d="M3 12H21M3 6H21M3 18H21"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
          <h1>Chat Irumae</h1>
          {/* <div className="user-info">
            <span className="user-name">
              {tokenUtils.getUserName() || "사용자"}
            </span>
            <span className="user-id">
              ({tokenUtils.getStudentId() || "학번"})
            </span>
          </div> */}
          <button onClick={handleLogout} className="logout-button">
            로그아웃
          </button>
        </div>
      </header>

      {/* 메시지 영역 */}
      <main className="messages-container">
        <div className="messages-list">
          {messages.length === 0 ? (
            <div className="welcome-message">
              <h2>안녕하세요! 👋</h2>
              <p>무엇을 도와드릴까요?</p>
            </div>
          ) : (
            messages.map((message) => (
              <div
                key={message.id}
                className={`message ${
                  message.sender === "assistant" ? "assistant" : "user"
                }`}
              >
                <div className="message-content">
                  <div className="message-text">{message.content}</div>
                  <div className="message-time">
                    {message.timestamp.toLocaleTimeString("ko-KR", {
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </div>
                </div>
              </div>
            ))
          )}
          {isLoading && (
            <div className="message assistant">
              <div className="message-content">
                <div className="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>
      </main>

      {/* 입력 영역 */}
      <footer className="input-container">
        <form onSubmit={handleSendMessage} className="input-form">
          <div className="input-wrapper">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="메시지를 입력하세요..."
              className="message-input"
              rows={1}
              disabled={isLoading}
            />
            <button
              type="submit"
              className="send-button"
              disabled={!inputMessage.trim() || isLoading}
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M22 2L11 13M22 2L15 22L11 13M22 2L2 9L11 13"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </button>
          </div>
        </form>
      </footer>
    </div>
  );
};

export default DashboardPage;

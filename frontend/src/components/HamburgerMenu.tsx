import React from "react";
import "./HamburgerMenu.css";

export interface ChatHistory {
  id: string;
  title: string;
  messages: any[];
  createdAt: Date;
  updatedAt: Date;
}

interface HamburgerMenuProps {
  isOpen: boolean;
  onClose: () => void;
  onNewChat: () => void;
  onLoadChat: (chatId: string) => void;
  onDeleteChat: (chatId: string) => void;
  chatHistories: ChatHistory[];
  currentChatId: string | null;
}

const HamburgerMenu: React.FC<HamburgerMenuProps> = ({
  isOpen,
  onClose,
  onNewChat,
  onLoadChat,
  onDeleteChat,
  chatHistories,
  currentChatId,
}) => {
  return (
    <>
      {/* 햄버거 메뉴 오버레이 */}
      {isOpen && <div className="menu-overlay" onClick={onClose} />}

      {/* 사이드바 메뉴 */}
      <div className={`sidebar ${isOpen ? "open" : ""}`}>
        <div className="sidebar-header">
          <button onClick={onNewChat} className="new-chat-button">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path
                d="M12 5V19M5 12H19"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            새 채팅
          </button>
        </div>

        <div className="chat-history">
          <h3>채팅 히스토리</h3>
          {chatHistories.length === 0 ? (
            <p className="no-history">아직 채팅 기록이 없습니다.</p>
          ) : (
            <div className="history-list">
              {chatHistories.map((chat) => (
                <div
                  key={chat.id}
                  className={`history-item ${
                    currentChatId === chat.id ? "active" : ""
                  }`}
                >
                  <div
                    className="history-content"
                    onClick={() => onLoadChat(chat.id)}
                  >
                    <div className="history-title">{chat.title}</div>
                    <div className="history-date">
                      {chat.updatedAt.toLocaleDateString("ko-KR", {
                        month: "short",
                        day: "numeric",
                      })}
                    </div>
                  </div>
                  <button
                    className="delete-button"
                    onClick={(e) => {
                      e.stopPropagation();
                      onDeleteChat(chat.id);
                    }}
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                      <path
                        d="M18 6L6 18M6 6L18 18"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default HamburgerMenu;

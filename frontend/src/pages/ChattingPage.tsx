// src/pages/ChattingPage.tsx
import { useState } from "react";
import "../styles/chatting-page.css";

export default function ChattingPage() {
  const [text, setText] = useState("");

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    const v = text.trim();
    if (!v) return;
    console.log("[Send]", v);
    setText("");
  };

  return (
    <div className="chatting-root">
      {/* Top Bar */}
      <header className="chat-topbar">
        <div className="chat-topbar__inner">
          <div className="brand">
            <img src="/uos-logo.png" alt="UOS" />
            <span className="brand__title">Chatrumae</span>
          </div>
          <button className="hamburger" aria-label="메뉴 열기" type="button">
            <span />
          </button>
        </div>
      </header>

      {/* Stage */}
      <main className="stage">
        <div className="owl">
          <img className="owl__img" src="/mascot.png" alt="아우래요 마스코트" draggable={false} />
          <p className="bubble">
            안녕하세요, 서울시립대학교 AI, 이루매에요😀
            <br />
            궁금한 점을 질문해주세요!
          </p>
        </div>
      </main>

      {/* Ask Bar */}
      <form className="askbar" onSubmit={submit}>
        <input
          className="askbar__input"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Send a message…"
          aria-label="메시지 입력"
        />
        <button className="askbar__send" type="submit" aria-label="보내기">
          {/* paper-plane 아이콘 (SVG) */}
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path d="M3 11.5l17-8-7.5 17-2.3-6.3L3 11.5zM13.2 13.2L20 4 10.8 10.8l2.4 2.4z" />
          </svg>
        </button>
      </form>
    </div>
  );
}

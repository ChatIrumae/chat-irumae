import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/chatting-page.css";
import { chatApi, authApi } from "../utils/api";

type Role = "assistant" | "user";
type ChatMessage = { id: string; role: Role; text: string; at: number };

type ChatSession = {
  id: string;
  title: string;       // 첫 질문으로 생성
  messages: ChatMessage[]; // 모든 메시지
  updatedAt: number;   // 마지막 갱신 시간
};

const STORAGE_KEY = "CHAT_SESSIONS_V1";

/** ✅ 상단바 배치 변경
 *  - "logo-left"  : 로고 좌측, 메뉴 우측(기본)
 *  - "logo-right" : 메뉴 좌측, 로고 우측
 *  - "logo-center": 로고 가운데, 메뉴 우측(아이폰 스타일)
 */
const TOPBAR_LAYOUT: "logo-left" | "logo-right" | "logo-center" = "logo-right";

function now() {
  return Date.now();
}
function clamp(s: string, n: number) {
  const t = s.replace(/\s+/g, " ").trim();
  return t.length > n ? t.slice(0, n).trimEnd() + "…" : t;
}

/** 실제 API를 통한 봇 응답 생성 */
async function generateBotReply(
  userText: string,
  currentChatId: string
): Promise<string> {
  try {
    const message = {
      id: crypto.randomUUID(),
      content: userText,
      sender: "user",
      timestamp: new Date(),
      currentChatId: currentChatId,
    };

    const response = await chatApi.sendMessage(message);
    return response;
  } catch (error) {
    console.error("API 호출 중 오류 발생:", error);
    return `죄송합니다. "${userText}"에 대한 응답을 생성하는 중 오류가 발생했습니다. 다시 시도해 주세요.`;
  }
}

/** 한 글자씩 출력하는 타이핑 효과 */
// 한 글자씩 출력하는 타이핑 효과
function typeOut({
  full,
  step = 18, // 숫자 높일수록 느림(12~24 권장)
  onUpdate,
  onDone,
}: {
  full: string;
  step?: number;
  onUpdate: (txt: string) => void;
  onDone: () => void;
}) {
  let i = 0;
  const tick = () => {
    i++;
    onUpdate(full.slice(0, i));
    if (i < full.length) {
      const jitter = Math.random() * 60;
      setTimeout(tick, step + jitter); // 타이핑 속도에 조절을 둬서 부드럽게 진행
    } else {
      onDone();
    }
  };

  // 처음부터 부드럽게 시작
  setTimeout(tick, 450 + Math.random() * 250); // 시작 전 짧은 딜레이 추가
}


/* =========================
   ✅ 실측 기반 말풍선 폭 계산
   ========================= */
const MEASURE_FONT =
  '14px ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Noto Sans KR", Arial, "Apple SD Gothic Neo", "Malgun Gothic", sans-serif';

/** 주어진 텍스트(개행 포함)의 실제 픽셀 폭 측정(가장 긴 줄 기준) */
function measureTextPx(text: string, font = MEASURE_FONT): number {
  const canvas =
    (measureTextPx as any)._canvas ||
    ((measureTextPx as any)._canvas = document.createElement("canvas"));
  const ctx = canvas.getContext("2d");
  if (!ctx) return 0;
  ctx.font = font;

  // 여러 줄이면 가장 긴 줄의 폭을 사용
  const lines = (text || "").split(/\r?\n/);
  let max = 0;
  for (const line of lines) {
    const w = ctx.measureText(line).width;
    if (w > max) max = w;
  }
  return Math.ceil(max);
}

/** 실제 텍스트 폭 + 패딩으로 말풍선 폭을 산출하고 min/max로 클램프 */
function bubbleWidthByText(
  text: string,
  {
    min = 300,     // ✅ 짧은 말풍선도 작게
    max = 700,
    padding = 28, // .bubble 좌우 패딩(12px + 14px) 합과 맞춤
    font = MEASURE_FONT,
  }: { min?: number; max?: number; padding?: number; font?: string } = {}
) {
  const raw = measureTextPx(text?.trim?.() ?? "", font) + padding;
  return Math.max(min, Math.min(raw, max));
}

export default function ChattingPage() {
  const navigate = useNavigate();

  // ----- 로컬스토리지 로드 -----
  const [sessions, setSessions] = useState<ChatSession[]>(() => {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    } catch {
      return [];
    }
  });

  // 현재 세션 id
  const [activeId, setActiveId] = useState<string>(
    () => sessions[0]?.id ?? crypto.randomUUID()
  );

  // 현재 세션(없으면 초기 세션 생성)
  const active = useMemo<ChatSession>(() => {
    const found = sessions.find((s) => s.id === activeId);
    if (found) return found;
    // 초기 세션(웰컴 메시지 포함)
    return {
      id: activeId,
      title: "새 채팅",
      messages: [
        {
          id: "welcome",
          role: "assistant",
          text: "안녕하세요, 서울시립대학교 AI, 이루매에요😀\n궁금한 점을 질문해주세요!",
          at: now(),
        },
      ],
      updatedAt: now(),
    };
  }, [sessions, activeId]);

  const [text, setText] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isTyping, setIsTyping] = useState(false);

  // 제목 인라인 편집 상태
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState("");
  const editInputRef = useRef<HTMLInputElement | null>(null);

  const listRef = useRef<HTMLDivElement>(null);
  const started = active.messages.some((m) => m.role === "user"); // 첫 질문 이후면 true

  // ----- 로그아웃 함수 -----
  const handleLogout = async () => {
    if (confirm("로그아웃 하시겠습니까?")) {
      try {
        await authApi.logout();
        localStorage.removeItem("authToken");
        navigate("/login");
      } catch (error) {
        console.error("로그아웃 중 오류:", error);
        // 에러가 발생해도 로컬 토큰을 제거하고 로그인 페이지로 이동
        localStorage.removeItem("authToken");
        navigate("/login");
      }
    }
  };

  // ----- 저장 헬퍼 -----
  const persist = (
    next: ChatSession[] | ((prev: ChatSession[]) => ChatSession[])
  ) => {
    setSessions((prev) => {
      const value = typeof next === "function" ? (next as any)(prev) : next;
      localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
      return value;
    });
  };

  const upsertActive = (session: ChatSession) => {
    persist((prev) => {
      const others = prev.filter((s) => s.id !== session.id);
      // 최신순(updatedAt desc) 정렬
      return [session, ...others].sort((a, b) => b.updatedAt - a.updatedAt);
    });
  };

  // ----- 봇 타이핑/응답 시작 -----
  const startBotReply = async (userText: string) => {
    const botId = crypto.randomUUID();

    // 1) 우선 빈 assistant 메시지를 추가
    setSessions((prev) => {
      const next = prev.map((s) =>
        s.id === activeId
          ? {
              ...s,
              messages: [
                ...s.messages,
                { id: botId, role: "assistant" as const, text: "", at: now() },
              ],
              updatedAt: now(),
            }
          : s
      );
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });

    setIsTyping(true);

    try {
      // 2) API 호출하여 실제 응답 받기
      const full = await generateBotReply(userText, activeId);

      // 3) 한 글자씩 채워가기
      typeOut({
        full,
        step: 18,
        onUpdate: (partial) => {
          setSessions((prev) => {
            const next = prev.map((s) =>
              s.id === activeId
                ? {
                    ...s,
                    messages: s.messages.map((m) =>
                      m.id === botId ? { ...m, text: partial } : m
                    ),
                    updatedAt: now(),
                  }
                : s
            );
            localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
            return next;
          });
        },
        onDone: () => setIsTyping(false),
      });
    } catch (error) {
      console.error("봇 응답 생성 중 오류:", error);
      // 에러 발생 시 에러 메시지 표시
      const errorMessage =
        "죄송합니다. 응답을 생성하는 중 오류가 발생했습니다.";
      setSessions((prev) => {
        const next = prev.map((s) =>
          s.id === activeId
            ? {
                ...s,
                messages: s.messages.map((m) =>
                  m.id === botId ? { ...m, text: errorMessage } : m
                ),
                updatedAt: now(),
              }
            : s
        );
        localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
        return next;
      });
      setIsTyping(false);
    }
  };

  // ----- 메시지 전송 -----
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const v = text.trim();
    if (!v) return;

    // active 세션을 갱신(없으면 생성)
    const isNew = !sessions.find((s) => s.id === active.id);
    const firstUser = !active.messages.some((m) => m.role === "user");
    const next: ChatSession = {
      ...active,
      title: firstUser ? clamp(v, 28) : active.title,
      messages: [
        ...active.messages,
        { id: crypto.randomUUID(), role: "user", text: v, at: now() },
      ],
      updatedAt: now(),
    };

    if (isNew) {
      // 세션 목록에 처음 추가
      persist([next, ...sessions]);
    } else {
      upsertActive(next);
    }

    setText("");

    // ✅ 봇 자동 응답(타이핑) 시작
    await startBotReply(v);
  };

  // ----- 새 채팅 만들기 -----
  const createNewChat = () => {
    const id = crypto.randomUUID();
    const fresh: ChatSession = {
      id,
      title: "새 채팅",
      messages: [
        {
          id: "welcome",
          role: "assistant",
          text: "안녕하세요, 서울시립대학교 AI, 이루매에요😀\n궁금한 점을 질문해주세요!",
          at: now(),
        },
      ],
      updatedAt: now(),
    };
    persist((prev) => [fresh, ...prev]);
    setActiveId(id);
    setSidebarOpen(false);
    setIsTyping(false);
    setTimeout(() => listRef.current?.scrollTo({ top: 0 }), 0);
  };

  // ----- 단일 세션 삭제 -----
  const deleteSession = (id: string) => {
    setIsTyping(false);

    // 1) 목록에서 제거
    setSessions((prev) => {
      const next = prev.filter((s) => s.id !== id);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });

    // 2) 지운 게 active면 대체 세션으로 이동(없으면 새로 생성)
    setTimeout(() => {
      setSessions((curr) => {
        if (id === activeId) {
          if (curr.length > 0) {
            setActiveId(curr[0].id);
          } else {
            const freshId = crypto.randomUUID();
            const fresh: ChatSession = {
              id: freshId,
              title: "새 채팅",
              messages: [
                {
                  id: "welcome",
                  role: "assistant",
                  text: "안녕하세요, 서울시립대학교 AI, 이루매에요😀\n궁금한 점을 질문해주세요!",
                  at: now(),
                },
              ],
              updatedAt: now(),
            };
            const nextArr = [fresh];
            localStorage.setItem(STORAGE_KEY, JSON.stringify(nextArr));
            setActiveId(freshId);
            return nextArr;
          }
        }
        return curr;
      });
    }, 0);
  };

  // ----- 세션 전환(사이드바에서 아이템 클릭) -----
  const switchTo = (id: string) => {
    setActiveId(id);
    setSidebarOpen(false);
    setIsTyping(false);
    setTimeout(() => listRef.current?.scrollTo({ top: 0 }), 0);
  };

  // ----- 제목 편집 -----
  const beginEdit = (id: string, current: string) => {
    setEditingId(id);
    setEditingTitle(current || "");
    // 다음 tick에 포커스
    setTimeout(() => editInputRef.current?.focus(), 0);
  };

  const commitEdit = () => {
    if (!editingId) return;
    const newTitle = editingTitle.replace(/\s+/g, " ").trim();
    setSessions((prev) => {
      const next = prev
        .map((s) =>
          s.id === editingId
            ? { ...s, title: newTitle || "새 채팅", updatedAt: now() }
            : s
        )
        .sort((a, b) => b.updatedAt - a.updatedAt);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
    setEditingId(null);
    setEditingTitle("");
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditingTitle("");
  };

  // ----- ESC 로 사이드바 닫기 -----
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        if (editingId) cancelEdit();
        else setSidebarOpen(false);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [editingId]);

  // 새 메시지 생기면 스크롤 하단
  useEffect(() => {
    if (started)
      listRef.current?.scrollTo({
        top: listRef.current.scrollHeight,
        behavior: "smooth",
      });
  }, [active.messages.length, isTyping]);

  return (
    <div className="chatting-root">
      {/* Top Bar */}
      <header className="chat-topbar">
        {/* ✅ 레이아웃 모드 클래스 부여 */}
        <div className={`chat-topbar__inner ${TOPBAR_LAYOUT}`}>
          {/* ✅ 로고/타이틀(클릭 시 새 채팅) */}
          <button
            className="brand brand-btn"
            onClick={createNewChat}
            aria-label="새 채팅 시작"
            title="새 채팅 시작"
          >
            <img src="/uos-logo.png" alt="UOS" />
            <span className="brand__title">Chat Irumae</span>
          </button>

          {/* 우측/좌측으로 이동 가능한 액션 영역 */}
          <div
            className="topbar-actions"
            style={{ display: "flex", gap: 8, alignItems: "center" }}
          >
            <button className="btn btn--light" onClick={createNewChat}>
              새 채팅
            </button>
            <button
              className="btn btn--logout"
              onClick={handleLogout}
              aria-label="로그아웃"
              title="로그아웃"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path
                  d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
              로그아웃
            </button>
            <button
              className="hamburger"
              aria-label="메뉴"
              onClick={() => setSidebarOpen(true)}
            >
              <span />
            </button>
          </div>
        </div>
      </header>

      {/* 화면 본문: 웰컴 vs 채팅 */}
      {!started ? (
        <main className="stage fade-in">
          <div className="owl">
            <img
              className="owl__img"
              src="/mascot.png"
              alt="아우래요 마스코트"
              draggable={false}
            />
            <p
              className="bubble a"
              style={{
                width: bubbleWidthByText(text), // ✅ 입력 길이에 따라 실측 기반 폭
                maxWidth: "90vw",
              }}
            >
              안녕하세요, 서울시립대학교 AI, 이루매에요😀
              <br />
              궁금한 점을 질문해주세요!
            </p>
          </div>
        </main>
      ) : (
        <main className="chat-body slide-fade-in" ref={listRef}>
          {active.messages.map((m, i) => {
            const isAssistant = m.role === "assistant";
            const showAvatar = isAssistant && i === 0;

            // ✅ 실제 텍스트 폭 기반(역할별 min/max 튜닝 가능)
            const bubbleW = bubbleWidthByText(m.text, {
              min: isAssistant ? 72 : 68,
              max: isAssistant ? 520 : 480,
            });

            return (
              <div
                key={m.id}
                className={`msg-row ${isAssistant ? "left" : "right"} msg-enter`}
              >
                {showAvatar && (
                  <img
                    className="avatar"
                    src="/mascot.png"
                    alt="아우래요 마스코트"
                    draggable={false}
                  />
                )}
                <p
                  className={`bubble ${isAssistant ? "a" : "u"}`}
                  style={{ width: bubbleW, maxWidth: "90vw" }}
                >
                  {m.text}
                </p>
              </div>
            );
          })}

          {/* 타이핑 인디케이터 */}
          {isTyping && (
            <div className="msg-row left msg-enter">
              <p className="bubble a typing" style={{ width: 180 }}>
                <span className="dots">
                  <i></i>
                  <i></i>
                  <i></i>
                </span>
              </p>
            </div>
          )}
        </main>
      )}

      {/* 입력 바 */}
      <form className="askbar" onSubmit={submit}>
        <input
          className="askbar__input"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Send a message…"
          aria-label="메시지 입력"
          disabled={isTyping}
        />
        <button
          className="askbar__send"
          type="submit"
          aria-label="보내기"
          disabled={isTyping}
        >
          <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path d="M3 11.5l17-8-7.5 17-2.3-6.3L3 11.5zM13.2 13.2L20 4 10.8 10.8l2.4 2.4z" />
          </svg>
        </button>
      </form>

      {/* 사이드바: 저장된 모든 세션 목록 (+ 개별 삭제/제목편집) */}
      <div
        className={`sidebar ${sidebarOpen ? "open" : ""}`}
        role="dialog"
        aria-label="저장된 대화"
      >
        <div className="sidebar__header">
          <strong>저장된 대화</strong>
          <div style={{ display: "flex", gap: 8 }}>
            <button className="btn btn--ghost" onClick={createNewChat}>
              + 새 채팅
            </button>
            <button
              className="sidebar__close"
              onClick={() => setSidebarOpen(false)}
              aria-label="닫기"
            >
              ×
            </button>
          </div>
        </div>

        {sessions.length === 0 ? (
          <p className="sidebar__empty">저장된 대화가 없어요.</p>
        ) : (
          <ul className="sidebar__list">
            {sessions.map((s) => {
              const isEditing = editingId === s.id;
              return (
                <li key={s.id}>
                  <div
                    className={`sidebar__row ${
                      s.id === activeId ? "is-active" : ""
                    }`}
                  >
                    {/* 항목(클릭 → 전환, 더블클릭 → 편집) */}
                    <button
                      className="sidebar__item"
                      onClick={() => switchTo(s.id)}
                      onDoubleClick={(e) => {
                        e.stopPropagation();
                        beginEdit(s.id, s.title);
                      }}
                      title={s.title || "새 채팅"}
                    >
                      <span className="dot" />
                      <span className="col">
                        {!isEditing ? (
                          <>
                            <span className="line title">
                              {s.title || "새 채팅"}
                            </span>
                            <span className="line sub">
                              {new Date(s.updatedAt).toLocaleString([], {
                                month: "2-digit",
                                day: "2-digit",
                                hour: "2-digit",
                                minute: "2-digit",
                              })}
                              {" · "}메시지{" "}
                              {
                                s.messages.filter((m) => m.role === "user")
                                  .length
                              }
                              개
                            </span>
                          </>
                        ) : (
                          <input
                            ref={editInputRef}
                            className="sidebar__editInput"
                            value={editingTitle}
                            onChange={(e) => setEditingTitle(e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === "Enter") {
                                e.preventDefault();
                                commitEdit();
                              }
                              if (e.key === "Escape") {
                                e.preventDefault();
                                cancelEdit();
                              }
                            }}
                            onBlur={commitEdit}
                            placeholder="제목 입력"
                          />
                        )}
                      </span>
                    </button>

                    {/* 편집/삭제 버튼 그룹 */}
                    <div className="sidebar__actions">
                      {!isEditing ? (
                        <button
                          className="icon-btn"
                          aria-label="제목 편집"
                          onClick={(e) => {
                            e.stopPropagation();
                            beginEdit(s.id, s.title);
                          }}
                          title="제목 편집"
                        >
                          {/* 연필 아이콘 */}
                          <svg
                            viewBox="0 0 24 24"
                            width="16"
                            height="16"
                            aria-hidden="true"
                          >
                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zm3.04 2.71H5v-1.04l9.06-9.06 1.04 1.04L6.04 19.96zM20.71 7.04a1.003 1.003 0 0 0 0-1.42L18.37 3.29a1.003 1.003 0 0 0-1.42 0L15.13 5.1l3.75 3.75 1.83-1.81z" />
                          </svg>
                        </button>
                      ) : (
                        <button
                          className="icon-btn"
                          aria-label="편집 완료"
                          onMouseDown={(e) => e.preventDefault()}
                          onClick={(e) => {
                            e.stopPropagation();
                            commitEdit();
                          }}
                          title="완료"
                        >
                          {/* 체크 아이콘 */}
                          <svg
                            viewBox="0 0 24 24"
                            width="16"
                            height="16"
                            aria-hidden="true"
                          >
                            <path d="M9 16.2l-3.5-3.5-1.4 1.4L9 19 20.3 7.7l-1.4-1.4z" />
                          </svg>
                        </button>
                      )}

                      <button
                        className="sidebar__delete"
                        aria-label="대화 삭제"
                        onClick={(e) => {
                          e.stopPropagation();
                          if (confirm("이 대화를 삭제할까요?")) {
                            deleteSession(s.id);
                          }
                        }}
                        title="삭제"
                      >
                        <svg
                          viewBox="0 0 24 24"
                          width="16"
                          height="16"
                          aria-hidden="true"
                        >
                          <path d="M9 3h6l1 2h4v2H4V5h4l1-2zm2 7v7h2v-7h-2zm-4 0v7h2v-7H7zm8 0v7h2v-7h-2z" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </li>
              );
            })}
          </ul>
        )}

        <div className="sidebar__footer">
          <button
            className="btn btn--ghost"
            onClick={() => {
              if (confirm("모든 저장된 대화를 삭제할까요?")) {
                localStorage.removeItem(STORAGE_KEY);
                setSessions([]);
                setIsTyping(false);
              }
            }}
          >
            모두 삭제
          </button>
        </div>
      </div>

      {/* 항상 렌더 + show 토글 → 닫힐 때 페이드아웃 */}
      <div
        className={`backdrop ${sidebarOpen ? "show" : ""}`}
        onClick={() => {
          if (editingId) cancelEdit();
          else setSidebarOpen(false);
        }}
      />
    </div>
  );
}

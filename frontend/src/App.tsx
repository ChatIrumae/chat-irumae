// App.tsx
import { Routes, Route, Navigate } from "react-router-dom";
import ChattingPage from "./pages/ChattingPage";
import Login from "./pages/Login";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/dashboard" element={<ChattingPage />} />
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

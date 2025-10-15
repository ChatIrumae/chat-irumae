<<<<<<< HEAD
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { validateEnv } from "./config/env";
=======
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
>>>>>>> 37ff5c9 (Login Page)

// 환경 변수 검증
validateEnv();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>
);

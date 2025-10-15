// src/components/LoginPage/Logo.tsx
// 기존: import uos from "../assets/uos-logo.png";
import uos from '../../assets/uos-logo.png';

export default function Logo() {
  return <img src={uos} alt="UOS" className="logo-img" width={92} height={92} />;
}

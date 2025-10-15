import BackgroundShapes from '../components/LoginPage/BackgroundShapes';
import AuthCard from '../components/LoginPage/AuthCard';
import Logo from '../components/LoginPage/Logo';
import TextInput from '../components/LoginPage/TextInput';
import Button from '../components/LoginPage/Button';
import '../styles/login.css';
import { useState } from 'react';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    alert(`username: ${username}\npassword: ${password}`);
  };

  // 
  return (
    <div className="login-root">
      <BackgroundShapes />

      <div className="login-center">
        <AuthCard>
          <div className="login-logo">
            <Logo />
          </div>

          <form className="login-form" onSubmit={onSubmit}>
            <TextInput
              type="text"
              placeholder="USERNAME"
              icon="user"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
            />
            <TextInput
              type="password"
              placeholder="PASSWORD"
              icon="lock"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />

            <Button type="submit">LOGIN</Button>

            <a className="forgot" href="#">
              Forgot password?
            </a>
          </form>
        </AuthCard>
      </div>
    </div>
  );
}

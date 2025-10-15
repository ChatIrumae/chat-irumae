type Props = {
  children: React.ReactNode;
};

export default function AuthCard({ children }: Props) {
  return <div className="auth-card">{children}</div>;
}

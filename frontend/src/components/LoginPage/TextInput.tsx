import React, { type InputHTMLAttributes } from 'react';

type Props = InputHTMLAttributes<HTMLInputElement> & {
  icon?: 'user' | 'lock';
};

function Icon({ name }: { name: 'user' | 'lock' }) {
  if (name === 'user') {
    return (
      <svg viewBox="0 0 24 24" className="input-icon" aria-hidden>
        <path d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z" />
      </svg>
    );
  }

  return (
    <svg viewBox="0 0 24 24" className="input-icon" aria-hidden>
      <path d="M17 8V7a5 5 0 0 0-10 0v1H5v13h14V8Zm-8-1a3 3 0 0 1 6 0v1H9Zm9 4H6v8h12Z" />
    </svg>
  );
}

export default function TextInput({ icon, ...props }: Props) {
  return (
    <div className="input-wrap">
      {icon && <Icon name={icon} />}
      <input className="input" {...props} />
    </div>
  );
}

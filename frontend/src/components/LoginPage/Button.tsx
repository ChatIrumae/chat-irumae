import React, { type ButtonHTMLAttributes } from 'react';

export default function Button(props: ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button className="btn" {...props}>
      {props.children}
    </button>
  );
}

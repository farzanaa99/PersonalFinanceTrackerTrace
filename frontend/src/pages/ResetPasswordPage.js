import React, { useRef, useState } from "react";
import { useAuth } from "../contexts/AuthContext";

export default function ResetPasswordPage() {
  const emailRef = useRef();
  const { resetPassword } = useAuth();
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setMessage("");
    setError("");
    setLoading(true);
    try {
      await resetPassword(emailRef.current.value);
      setMessage("Check your inbox for further instructions.");
    } catch (err) {
      setError("Failed to reset password: " + err.message);
    }
    setLoading(false);
  }

  return (
    <div className="auth-container">
      <h2>Password Reset</h2>
      {error && <div className="error">{error}</div>}
      {message && <div className="success">{message}</div>}
      <form onSubmit={handleSubmit}>
        <input type="email" ref={emailRef} placeholder="Email" required />
        <button disabled={loading} type="submit">
          Reset Password
        </button>
      </form>
    </div>
  );
}
import React, { useRef, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import GoogleIcon from "../google-icon.svg";
import "./LoginPage.css";

export default function LoginPage() {
  const emailRef = useRef();
  const passwordRef = useRef();
  const { login, signInWithGoogle } = useAuth();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const stopLoading = () => setLoading(false);
  const handleAuthError = (err, fallbackMsg) =>
    setError(err?.message?.replace("Firebase:", "").trim() || fallbackMsg);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(emailRef.current.value, passwordRef.current.value);
      navigate("/dashboard");
    } catch (err) {
      handleAuthError(err, "Failed to log in");
    } finally {
      stopLoading();
    }
  };

  const handleGoogleSignIn = async () => {
    setError("");
    setLoading(true);
    try {
      await signInWithGoogle();
      navigate("/dashboard");
    } catch (err) {
      handleAuthError(err, "Google sign-in failed");
    } finally {
      stopLoading();
    }
  };

  const handleDemoLogin = async () => {
    setError("");
    setLoading(true);
    try {
      const demoEmail = process.env.REACT_APP_DEMO_EMAIL || "demo@trace.com";
      const demoPass  = process.env.REACT_APP_DEMO_PASSWORD || "demo123";
      await login(demoEmail, demoPass);
      navigate("/dashboard");
    } catch (err) {
      handleAuthError(err, "Demo login failed");
    } finally {
      stopLoading();
    }
  };

  return (
    <main className="login">
      <div className="login__container">
        <header className="login__header">
          <h1 className="login__logo">Trace</h1>
          <p className="login__tagline">Welcome back</p>
        </header>

        {error && <div className="login__error">{error}</div>}

        <form className="login__form" onSubmit={handleSubmit}>
          <div className="input-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              ref={emailRef}
              placeholder="you@example.com"
              required
              autoComplete="username"
              disabled={loading}
            />
          </div>

          <div className="input-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              ref={passwordRef}
              placeholder="••••••••"
              required
              autoComplete="current-password"
              disabled={loading}
            />
          </div>

          <div className="login__options">
            <label className="remember-me">
              <input type="checkbox" disabled={loading} />
              Remember me
            </label>
            <Link to="/reset-password" className="forgot-password">
              Forgot password?
            </Link>
          </div>

          <button
            type="submit"
            className="btn btn--primary login__btn"
            disabled={loading}
          >
            {loading ? <div className="spinner" /> : "Sign In"}
          </button>
        </form>

        <button
          className="btn btn--demo"
          onClick={handleDemoLogin}
          disabled={loading}
        >
          Use demo account
        </button>

        <div className="divider"><span>or</span></div>

        <button
          className="btn btn--outline google-btn"
          onClick={handleGoogleSignIn}
          disabled={loading}
        >
          <img src={GoogleIcon} alt="" className="google-icon" />
          Sign in with Google
        </button>

        <footer className="login__footer">
          <span>Don’t have an account?</span>
          <Link to="/signup">Sign up</Link>
        </footer>
      </div>
    </main>
  );
}

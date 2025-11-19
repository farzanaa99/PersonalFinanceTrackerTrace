import React, { useRef, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import GoogleIcon from "../google-icon.svg";
import "./SignUpPage.css";

export default function SignUpPage() {
  const usernameRef = useRef();
  const emailRef = useRef();
  const passwordRef = useRef();
  const passwordConfirmRef = useRef();
  const { signup, signInWithGoogle } = useAuth();
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const stopLoading = () => setLoading(false);
  const handleAuthError = (err, fallbackMsg) =>
    setError(err?.message?.replace("Firebase:", "").trim() || fallbackMsg);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (passwordRef.current.value !== passwordConfirmRef.current.value) {
      return setError("Passwords do not match");
    }
    
    if (usernameRef.current.value.trim().length < 3) {
      return setError("Username must be at least 3 characters long");
    }
    
    setError("");
    setLoading(true);
    
    try {
      await signup(
        emailRef.current.value, 
        passwordRef.current.value, 
        usernameRef.current.value.trim()
      );
      navigate("/dashboard");
    } catch (err) {
      handleAuthError(err, "Failed to create an account");
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

  return (
    <main className="signup">
      <div className="signup__container">
        <header className="signup__header">
          <h1 className="signup__logo">Trace</h1>
          <p className="signup__tagline">Create your account</p>
        </header>

        {error && <div className="signup__error">{error}</div>}

        <form className="signup__form" onSubmit={handleSubmit}>
          <div className="input-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              type="text"
              ref={usernameRef}
              placeholder="Choose a username"
              required
              autoComplete="username"
              disabled={loading}
              minLength={3}
            />
          </div>

          <div className="input-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              ref={emailRef}
              placeholder="you@example.com"
              required
              autoComplete="email"
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
              autoComplete="new-password"
              disabled={loading}
              minLength={6}
            />
          </div>

          <div className="input-group">
            <label htmlFor="passwordConfirm">Confirm Password</label>
            <input
              id="passwordConfirm"
              type="password"
              ref={passwordConfirmRef}
              placeholder="••••••••"
              required
              autoComplete="new-password"
              disabled={loading}
              minLength={6}
            />
          </div>

          <button
            type="submit"
            className="btn btn--primary signup__btn"
            disabled={loading}
          >
            {loading ? <div className="spinner" /> : "Create Account"}
          </button>
        </form>

        <div className="divider"><span>or</span></div>

        <button
          className="btn btn--outline google-btn"
          onClick={handleGoogleSignIn}
          disabled={loading}
        >
          <img src={GoogleIcon} alt="" className="google-icon" />
          Sign up with Google
        </button>

        <footer className="signup__footer">
          <span>Already have an account?</span>
          <Link to="/login">Sign in</Link>
        </footer>
      </div>
    </main>
  );
}
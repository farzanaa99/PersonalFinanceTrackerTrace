import React, { useContext, useState, useEffect, createContext } from "react";
import { auth, googleProvider } from "../firebase";
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signOut,
  signInWithPopup,
  onAuthStateChanged,
  sendPasswordResetEmail,
  updateProfile,
} from "firebase/auth";
import { logEvent } from "../utils/analytics";

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(
    () => JSON.parse(localStorage.getItem("authUser")) || null
  );
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setCurrentUser(user);
      if (user) {
        localStorage.setItem("authUser", JSON.stringify(user));
      } else {
        localStorage.removeItem("authUser");
      }
      setLoading(false);
    });
    return unsubscribe;
  }, []);

  const getDisplayName = (user) => {
    if (!user) return null;
    return user.displayName || user.email?.split('@')[0] || 'User';
  };

  async function signup(email, password, username) {
    logEvent('user_signup', { method: 'email' });
    const result = await createUserWithEmailAndPassword(auth, email, password);
    await updateProfile(result.user, {
      displayName: username
    });
    return result;
  }

  function login(email, password) {
    logEvent('user_login', { method: 'email' });
    return signInWithEmailAndPassword(auth, email, password);
  }

  function logout() {
    logEvent('user_logout');
    return signOut(auth);
  }

  async function signInWithGoogle() {
    logEvent('user_login', { method: 'google' });
    const result = await signInWithPopup(auth, googleProvider);
    if (!result.user.displayName) {
      await updateProfile(result.user, {
        displayName: result.user.email?.split('@')[0] || 'User'
      });
    }
    return result;
  }

  function resetPassword(email) {
    logEvent('password_reset_request');
    return sendPasswordResetEmail(auth, email);
  }

  const value = {
    currentUser,
    getDisplayName,
    signup,
    login,
    logout,
    signInWithGoogle,
    resetPassword,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}
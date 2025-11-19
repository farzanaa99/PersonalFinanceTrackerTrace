import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
import { getAnalytics } from "firebase/analytics";

const firebaseConfig = {
  
    apiKey: "AIzaSyBXucOqAqsrhYQZNLaYLneCvnf7w1JXanE",
    authDomain: "personal-finance-tracker-e8a02.firebaseapp.com",
    projectId: "personal-finance-tracker-e8a02",
    storageBucket: "personal-finance-tracker-e8a02.firebasestorage.app",
    messagingSenderId: "459499129830",
    appId: "1:459499129830:web:4186a782e0eb9a216e6427"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();

let analytics = null;
try {
  analytics = getAnalytics(app);
} catch (error) {
  console.warn('Firebase Analytics not available:', error);
}

export { app, analytics };
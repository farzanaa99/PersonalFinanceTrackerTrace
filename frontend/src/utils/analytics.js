import { logEvent as firebaseLogEvent } from 'firebase/analytics';
import { analytics } from '../firebase';

export const logEvent = (eventName, eventParameters = {}) => {
  if (analytics) {
    try {
      firebaseLogEvent(analytics, eventName, eventParameters);
    } catch (error) {
      console.warn('Failed to log event to Firebase Analytics:', error);
    }
  }
};

export const logPageView = (pagePath = window.location.pathname) => {
  logEvent('page_view', {
    page_path: pagePath,
    page_title: document.title
  });
};

// Initialize analytics (for compatibility)
export const initGA = () => {
  // Firebase Analytics is already initialized in firebase.js
  console.log('Firebase Analytics initialized');
}; 
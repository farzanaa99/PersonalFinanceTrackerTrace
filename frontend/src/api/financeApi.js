import { getAuth } from 'firebase/auth';

const auth = getAuth();

export const getIdToken = async () => {
  const user = auth.currentUser;
  if (!user) {
    throw new Error('No authenticated user');
  }
  return await user.getIdToken();
};

export const authenticatedFetch = async (url, options = {}) => {
  try {
    const token = await getIdToken();
    
    const fullUrl = url.startsWith('http') ? url : `http://localhost:8080${url}`;
    
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers
    };

    const response = await fetch(fullUrl, {
      ...options,
      headers
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('API request failed:', errorText);
      throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
    }

    return response;
  } catch (error) {
    console.error('API request failed:', error);
    throw error;
  }
};

export const apiGet = async (url) => {
  return await authenticatedFetch(url);
};

export const apiPost = async (url, data) => {
  return await authenticatedFetch(url, {
    method: 'POST',
    body: JSON.stringify(data)
  });
};

export const apiPut = async (url, data) => {
  return await authenticatedFetch(url, {
    method: 'PUT',
    body: JSON.stringify(data)
  });
};

export const apiDelete = async (url) => {
  return await authenticatedFetch(url, {
    method: 'DELETE'
  });
};

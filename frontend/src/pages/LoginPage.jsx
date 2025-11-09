import Cookies from 'js-cookie';
import { useNavigate } from 'react-router-dom';
import React, { useState } from 'react';


export default function LoginPage() {

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  
  const login = (event) => {
    event.preventDefault(); // Prevents the default form submission behavior , which is annoying asl
    fetch(`${import.meta.env.VITE_API_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    }).then(response => {
      if (response.ok) {
        return response.text();
      } else {
        throw new Error('Login failed');
      }
    }).then(data => {
      // Set the cookie
      
      Cookies.set('jwt', data, { expires: 7, secure: true, sameSite: 'none' });

      navigate('/discussions'); // Use navigate to change the route
      
    }).catch(error => {
      console.error('Error during login:', error);
      alert('Login failed. Please check your credentials.');
    });
  }

  return (
    <section id = "authWrapper">
      <section id="authSection" className="card card--login">
      <h2>Sign in</h2>
      <form id="loginForm" className="form" onSubmit={login}>
        <label>
          <span>Username</span>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </label>
        
        <label>
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        
        <button type="submit" className="btn btn--primary">Login</button>

      </form>
      </section>
    </section>
  );

}

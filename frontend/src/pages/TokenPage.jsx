import Cookies from 'js-cookie';
import { useNavigate } from 'react-router-dom';
import React, { useState } from 'react';


export default function RegisterPage() {

  const [token , setToken] = useState('');
  const [email , setEmail] = useState('');

  const navigate = useNavigate();

  
  const getToken = (event) => {
    event.preventDefault(); // Prevents the default form submission behavior , which is annoying asl
    fetch(`${import.meta.env.VITE_API_URL}/api/auth/token`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    }).then(response => {
      if (response.ok) {
        alert('Verification token sent to your email!');
      } else {
        throw new Error('token grabbing failed');
      }
    })
  }

  const verify = (event) => {
    event.preventDefault(); // Prevents the default form submission behavior , which is annoying asl
    fetch(`${import.meta.env.VITE_API_URL}/api/auth/verify?token=${token}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    }).then(response => {
      if (response.ok) {
        return response.text();
      } else {
        throw new Error('token grabbing failed');
      }
    }).then(data => {
      // Set the cookie
            
            Cookies.set('jwt', data, { expires: 7, secure: false, sameSite: 'lax' });
      
            navigate('/discussions'); // Use navigate to change the route
    }).catch(error => {
      console.error('Error during token verification:', error);
      alert('Token verification failed. Please check your token.');
    })
  }

  return (
    <section id = "authWrapper">
      <section id="authSection" className="card card--login">
      <h2>Sign Up!!!!</h2>
      <form id="loginForm" className="form" onSubmit={verify}>
        <p>Check your email for your verification token </p>
        <label>
          <span>Token</span>
          <input
            type="Token"
            value={token}
            onChange={(e) => setToken(e.target.value)}
            required
          />
        </label>
        
        
        <button  type="submit" className="btn btn--primary">Login</button>
      <button onClick={getToken} className="btn">Send Verificaiton Token</button>
      </form>
      </section>
    </section>
  );

}

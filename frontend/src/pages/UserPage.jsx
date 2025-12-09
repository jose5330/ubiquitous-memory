import Post from "../components/Post";
import React, { useEffect, useState ,useRef} from "react";
import PostModal from "../components/PostModal";
import { useNavigate, useParams } from 'react-router-dom';
import Header from "../components/Header";
import GetUserData from "../functions/GetUserData";

export default function Discussions() {

  const navigate = useNavigate();

  const { id } = useParams();
  
  const [file,setFile] = useState(null);
  const [userData, setUserData] = useState(null);
  useEffect(() => {
  const fetchUser = async () => {
    const data = await GetUserData();
    setUserData(data);
  };
  fetchUser();

}, []);



  const uploadFile = (e) => {
    e.preventDefault();
    const form = new FormData();
form.append('file', file);

fetch(`${import.meta.env.VITE_API_URL}/api/auth/uploadAvatar`, {
  method: 'POST',
  credentials: 'include', // if you rely on cookies
  body: form
})
.then(res => res.json())
.then(data => console.log('avatar url', data.avatarUrl))
.catch(err => console.error(err));

  }

  console.log("User Data:", userData);
  return (
    <main role="root" className="container">
      <Header />
      <h2>Upload Profile Picture</h2>
      <form id="loginForm" className="form" onSubmit={uploadFile}>
        <label>
          <span>Profile picture</span>
          <input
            type="file"
            onChange={(e) => {
              if (e.target.files[0].size > 1 * 1024 * 1024) {
                alert("File size exceeds 1MB limit.");
              } else {
                setFile(e.target.files[0]);
              }
            }}
            required
          />
        </label>
        
        <button type="submit" className="btn btn--primary">Upload Profile pic</button>

      </form>
      
    </main>
  );
}

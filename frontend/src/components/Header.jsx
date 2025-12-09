import React from "react";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import GetUserData from "../functions/GetUserData";

export default function Header() {
    const navigate = useNavigate();


    const [userData, setUserData] = useState(null);

    useEffect(() => {
      GetUserData().then(data=>{
        console.log("Header fetched user data:", data);
        setUserData(data);
      });
    }
    , []);

    console.log("Header User Data:", userData);

    return (<div>
         <header className="top-bar" role="banner"> 
        <p className="logo">ConnectHub</p>
        {userData && <img onClick={() => navigate(`/user/${userData && userData.id}`)} className="profile-pic" src={userData.userAvatar+`?time=${Date.now()}`}/>}
      </header>

      <header className="site-header" role="banner" aria-label="Site header">
        
        <div>
          <div className="brand">Trinity College ConnectHub</div>
          <nav className="site-nav" aria-label="Primary">
            <a onClick={() => navigate("/discussions")}  aria-current="page">Discussions</a>
            <a  >Documentation</a>
            <a href="#">Report a bug</a>
          </nav>
        </div>
        <div className="small muted">School collaboration prototype</div>
      </header>
    </div>);
}

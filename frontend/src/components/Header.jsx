import React from "react";
import { useNavigate } from "react-router-dom";

export default function Header() {
    const navigate = useNavigate();
    return (<div>
         <header className="top-bar" role="banner"> 
        <p className="logo">ConnectHub</p>
        <img className="profile-pic" src="./public/images/sbeve.jpg"/>
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

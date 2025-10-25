import Post from "../components/Post";
import React, { useEffect, useState ,useRef} from "react";
import PostModal from "../components/PostModal";
import { useNavigate } from 'react-router-dom';

export default function Discussions() {

  const navigate = useNavigate();

  const [posts, setPosts] = React.useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const [showModal, setShowModal] = useState(false);

  const loadMoreRef = useRef();

  useEffect(() => {
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && hasMore && !loading) {
        fetchPosts();
      }
    });

    if (loadMoreRef.current) observer.observe(loadMoreRef.current);
    return () => observer.disconnect();
  }, [hasMore, loading]);

  const onSend = (newPost) => {
    // Logic to send the post data to the server would go here
    setShowModal(false);
    setPosts([newPost, ...posts]);
  }

  const fetchPosts = () => {
    if (loading || !hasMore) return;
    setLoading(true);
    
    fetch(`http://localhost:8080/api/user/posts?page=${page}&size=4`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include', // Include cookies in the request
    })
      .then(response => response.json())
      .then(data => {
        data = data.content;
        console.log('Fetched tasks:', JSON.stringify(data));
        if (data.length === 0) {
          setHasMore(false);
        } else {
          setPosts((prev) => [...prev, ...data]);
          setPage((prev) => prev + 1);
        }

    setLoading(false);
      })
      .catch(error => console.error('Error fetching tasks:', error));
  }

  useEffect(fetchPosts,[]);

  return (
    <main role="root" className="container">
      <header className="top-bar" role="banner"> 
        <p className="logo">ConnectHub</p>
        <img className="profile-pic" src="./public/images/sbeve.jpg"/>
      </header>

      {showModal && <PostModal onFullScreen = {() => useNavigate("/")} onSend = {onSend} onClose = {() => setShowModal(false)} />}

      <header className="site-header" role="banner" aria-label="Site header">
        
        <div>
          <div className="brand">Trinity College ConnectHub</div>
          <nav className="site-nav" aria-label="Primary">
            <a href="#" aria-current="page">Discussions</a>
            <a href="#">Documentation</a>
            <a href="#">Report a bug</a>
          </nav>
        </div>
        <div className="small muted">School collaboration prototype</div>
      </header>

      <div className="action-row" aria-hidden="false">
        <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
          <button
            className="btn"
            aria-haspopup="listbox"
            aria-expanded="false"
            aria-label="Filter categories"
          >
            Categories ▾
          </button>
          <div className="floating-add">
            <button onClick={() => setShowModal(true)} className="btn primary" id="addPostBtn">
              Add Post
            </button>
          </div>
        </div>
      </div>

      <section className="feed" aria-label="Discussion feed">
        {posts.map((post) => {
          return (
            <Post
              id={post.id}
              key={post.id}
              isReply={false}
              username={post.username}
              subject={post.subject}
              title={post.title}
              createdAt={post.createdAt}
              body={post.body}
              tags={post.tags ? post.tags.split(",") : []}
              answered={post.answered}
            />
          );
        })}



        <div
          style={{ textAlign: "center", padding: "12px" }}
          aria-hidden="false"
        >
          <button className="btn">Load more</button>
          <div ref={loadMoreRef}></div>
        </div>
      </section>

      <aside className="docs" aria-label="Documentation sidebar">
        <div className="card" style={{ padding: "18px" }}>
          <div className="title">Explore Documentation</div>
          <p className="small">Quick links to community resources</p>

          <div style={{ height: "12px" }}></div>

          <div id="doc-grid" className="doc-grid" role="list">
            <div className="doc-card" role="listitem">
              <div className="doc-thumb" aria-hidden="true">
                PDF
              </div>
              <div style={{ fontWeight: 700, fontSize: "13px" }}>
                Cheat Sheets
              </div>
            </div>
            <div className="doc-card" role="listitem">
              <div className="doc-thumb" aria-hidden="true">
                PPR
              </div>
              <div style={{ fontWeight: 700, fontSize: "13px" }}>
                Past Papers
              </div>
            </div>
            <div className="doc-card" role="listitem">
              <div className="doc-thumb" aria-hidden="true">
                IMG
              </div>
              <div style={{ fontWeight: 700, fontSize: "13px" }}>
                Acids
              </div>
            </div>
           
          </div>
        </div>
      </aside>
    </main>
  );
}

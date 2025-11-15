import Post from "../components/Post";
import React, { useEffect, useState ,useRef} from "react";
import PostModal from "../components/PostModal";
import { useNavigate, useParams } from 'react-router-dom';
import Header from "../components/Header";

export default function Discussions() {

  const navigate = useNavigate();

  const { id } = useParams();

  const [post, setPost] = useState(null);
  const [replies, setReplies] = React.useState([]);

  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const [showModal, setShowModal] = useState(false);

  const loadMoreRef = useRef();

  

  useEffect(() => {
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && hasMore && !loading) {
        fetchReplies();
      }
    });

    if (loadMoreRef.current) observer.observe(loadMoreRef.current);
    return () => observer.disconnect();
  }, [hasMore, loading]);

  const onUpdate = (id) => {
    console.log("New reply received in PostSection:", id);
    // Logic to send the post data to the server would go here
    setShowModal(false);
    setPost(prev => {
      return {...prev, answered: true};
    });
    setReplies(prev => prev.map(reply => {
      if (reply.id === id) {
        console.log("Marking reply as answer:", {...reply, isAnswer: true});
        return {...reply, isAnswer: true}
      }
      return reply;
    }));
  }

   const onSend = (id) => {
    console.log("New reply received in PostSection:", id);
    // Logic to send the post data to the server would go here
    setShowModal(false);

    setReplies(prev => [{...id, isAnswer: false}, ...prev]);
  }

  const fetchReplies = () => {
    if (loading || !hasMore) return;
    setLoading(true);
    
    fetch(`${import.meta.env.VITE_API_URL}/api/user/posts/replies/${id}?page=${page}&size=4`, {
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
          setReplies((prev) => [...prev, ...data]);
          setPage((prev) => prev + 1);
        }

    setLoading(false);
      })
      .catch(error => console.error('Error fetching tasks:', error));
  }

  //useEffect(fetchReplies,[]);

  useEffect(() => {
    if (!id) return;
    fetch(`${import.meta.env.VITE_API_URL}/api/user/posts/${id}`, {
      credentials: "include",
    })
      .then(res => {
        if (!res.ok) throw new Error("Failed to load post");
        return res.json();
      })
      .then(data => {
        setPost(data);
      })
  }, [id]);
  
  return (
    <main role="root" className="container">
      <Header />


      {showModal && <PostModal parentId={id} onFullScreen = {() => useNavigate("/")} onSend = {onSend} onClose = {() => setShowModal(false)} />}

      
      <div className="action-row" aria-hidden="false">
        <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
          <div className="floating-add">
            <button onClick={() => setShowModal(true)} className="btn primary" id="addPostBtn">
              Reply to Post
            </button>
          </div>
        </div>
      </div>

      {post && <Post
              key={post.id}
              username={post.username}
              subject={post.subject}
              title={post.title}
              createdAt={post.createdAt}
              body={post.body}
              tags={post.tags ? post.tags.split(",") : []}
              answered={post.answered}
            />}
      <br/>
      <section className="feed" aria-label="Discussion feed">
        {replies.map((reply) => {
          return (
            <Post
              onSend = {onUpdate}
              isAnswer={reply.isAnswer}
              isOwner={post?.isOwner}
              key={reply.id}
              isReply={true}
              parentId={id}
              postId={reply.id}
              username={reply.username}
              subject={reply.subject}
              title={reply.title}
              createdAt={reply.createdAt}
              body={reply.body}
              tags={reply.tags ? reply.tags.split(",") : []}
              answered={reply.answered}
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

      
    </main>
  );
}

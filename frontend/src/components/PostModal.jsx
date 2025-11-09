import React, { useState } from "react";

export default function PostModal({parentId, onClose, onSend}) {

  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [subject, setSubject] = useState("");

    const handleSend = () => {
      const createdAt = new Date().toISOString();

      const post = {

          title: title,
          body: body,
          subject: subject,
          createdAt: createdAt,
          parentId: parentId ?? null,
        }
      
      fetch(`${import.meta.env.VITE_API_URL}/api/user/posts`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(post),
        credentials: 'include'
      })
        
      .then(response => {
        if (response.ok) {
          return response.json();
          
        }}).then(data => {
          console.log('Success:', data);
           const sentPost = {
          id: data.id ?? `temp-${Date.now()}`,
          username: data.username ?? data.user?.username ?? "You",
          title: data.title ?? post.title,
          body: data.body ?? post.body,
          subject: data.subject ?? post.subject,
          createdAt: data.createdAt ?? post.createdAt,
          tags: data.tags ?? post.tags,
          answered: data.answered ?? false,
          parentId: data.parentId ?? post.parentId ?? null,
        };
        onSend(sentPost);
        })
      .catch(error => {
        onClose();
        console.error('There was a problem with the fetch operation:', error);
      });
      
    }


    return (
        <div className="modal-backdrop">
          <div className="modal card">
            <div className="modal-header">
              <div className="avatar" aria-hidden="true">EW</div>
              <div>
              <div style={{ fontWeight: 800 }}>Eden</div>
              <span className="meta-text">{"Now"}{parentId == null && " · In"} </span> 
              <span> 
                {parentId == null && <input value={subject} onChange={(e) => setSubject(e.target.value)} className="modal-obj text-body meta-text" placeholder="<Subject>"/>}
              </span>
              
              </div>
            </div>

           {parentId == null && <input value={title} onChange={(e) => setTitle(e.target.value)} className="modal-obj title" placeholder="Title..." />}
            <textarea value={body} onChange={(e) => setBody(e.target.value)} className="modal-obj text-body" placeholder="What’s on your mind?" />
            <div className="actions">
              <button className="btn" onClick={onClose} >Cancel</button>
              <button className="btn primary" onClick={handleSend}>Post</button>
            </div>
          </div>
        </div>
        );
}
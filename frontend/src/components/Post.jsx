import { useNavigate } from "react-router-dom";
import React from "react";

export default function Post({isReply,id,username,subject, createdAt, title, body, tags, answered}) {
  
  
  const navigate = useNavigate();

  function parseServerDate(s) {
      const navigate = useNavigate();
      if (!s) return null;
      // if it already includes Z or an offset, let Date handle it
      if (/[zZ]|[+\-]\d{2}:\d{2}$/.test(s)) return new Date(s);
      // otherwise construct as local date/time to avoid timezone surprises
      const [datePart, timePart = "00:00:00"] = s.split('T');
      const [y, m, d] = datePart.split('-').map(Number);
      const [hh = 0, mm = 0, ss = 0] = timePart.split(':').map(t => Math.floor(Number(t)));
      return new Date(y, m - 1, d, hh, mm, ss);
    }

    const createdDate = parseServerDate(createdAt);
    const now = new Date();

    // difference in seconds
    const diffSeconds = createdDate ? Math.floor((now - createdDate) / 1000) : 0;
    console.log(title,diffSeconds,createdAt)
    const hoursAgo = Math.floor(diffSeconds / 3600) - 10;
    const daysAgo = Math.floor(hoursAgo / 24);
    const minutesAgo = Math.floor((diffSeconds % 3600) / 60) - 30;
    const secondsAgo = diffSeconds % 60;
    let timeAgoText = "Just now";

    const plural = (n) => n !== 1 ? 's' : '';
    
    if (hoursAgo >= 24) {
      timeAgoText = `${daysAgo} day${plural(daysAgo)} ago`;
    } else if (hoursAgo > 0) {
      timeAgoText = `${hoursAgo} hour${plural(hoursAgo)} ago`;
    } else if (minutesAgo > 0) {
      timeAgoText = `${minutesAgo} minute${plural(minutesAgo)} ago`;
    } else if (secondsAgo > 5) {
      timeAgoText = `${secondsAgo} seconds ago`;
    }
    
    
    
    return (<article className="card"  role="article">
          <div className="post-meta">
            <div className="avatar" aria-hidden="true">EW</div>
            <div>
              <div style={{ fontWeight: 800 }}>{username}</div>
              <div className="meta-text">
                {timeAgoText} · In {`<${subject}>`}
              </div>
            </div>
            {!isReply && <div
              className= {answered ? "status answered" : "status unanswered"}
              role="status"
              aria-live="polite"
            >
              {answered ? "This question has been answered" : "This question has not been answered" }
            </div>}
          </div>

          <h2 className="post-title" id="post1-title">
            {title}
          </h2>
          <p className="post-body">
            {body || "No content provided."}
          </p>
          {!isReply && <button className="btn" onClick={() => navigate(`/discussions/post/${id}`)}>
            <img className="icon" src="./public/images/icons/chat-78-32.png"/>
            <span className="meta-text" >3</span>
          </button>}

          <div className="tag-row" aria-hidden="false">
            {tags.map(tag => <span className="tag" key={tag}>{tag}</span>)}
          </div>
        </article>);
}
export default function GetUserData() {
    return fetch(`${import.meta.env.VITE_API_URL}/api/auth/me`, {
          method: "GET",
          credentials: "include", // send HttpOnly cookie!!
        })
          .then(res => res.json())
          //.then(data => data.content)
          ;
      
}


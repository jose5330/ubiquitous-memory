import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children, roleAccess = "user" }) {
  const [auth, setAuth] = useState(null);

  useEffect(() => {
    fetch(`${import.meta.env.VITE_API_URL}/api/auth/me`, {
      method: "GET",
      credentials: "include", // send HttpOnly cookie!!
    })
      .then(res => res.json())
      .then(data => setAuth(data))
      .catch(() => setAuth({ authenticated: false }));
  }, []);

  // Still loading → avoid flicker
  if (auth === null) return <div>Loading...</div>;

  if (!auth.authenticated) return <Navigate to="/login" />;

  // If admin-only page:
  if (roleAccess === "ROLE_ADMIN" && auth.role !== "ROLE_ADMIN") {
    return <Navigate to="/unauthorized" />;
  }

  return children;
}

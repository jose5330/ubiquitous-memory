import Cookies from "js-cookie";
import { Navigate } from "react-router-dom";
import {jwtDecode} from "jwt-decode";

function ProtectedRoute({ children, roleAccess = "user" }) {
  const token = Cookies.get("jwt");

  if (!token) {
    console.log("No token found, redirecting to login.");
    return <Navigate to="/login" />;
  }

  try {
    const decoded = jwtDecode(token);
    const currentTime = Date.now() / 1000; // in seconds

    console.log(JSON.stringify(decoded));

    if (decoded.exp < currentTime || (roleAccess === "ROLE_ADMIN" && decoded.role !== "ROLE_ADMIN")) {
      Cookies.remove("jwt"); // remove expired token
      return <Navigate to="/login" />;
    }

    return children;
  } catch (error) {
    Cookies.remove("jwt"); // remove invalid token
    return <Navigate to="/login" />;
  }
}

export default ProtectedRoute;

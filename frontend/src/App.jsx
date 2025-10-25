import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
//import Header from './components/Header';
import Discussions from './pages/Discussions';
import LoginPage from './pages/LoginPage';
import PostSection from './pages/PostSection';
import ProtectedRoute from './components/ProtectedRoute';


function App() {
  return (
      <Router>
        <Routes>

          <Route path={"/login"} element={<LoginPage />} />
          <Route path={"/discussions" } element={<ProtectedRoute><Discussions/></ProtectedRoute>} />
          <Route path={"/" } element={<ProtectedRoute><Discussions/></ProtectedRoute>} />
          
          <Route path={"/discussions/post/:id" } element={<ProtectedRoute><PostSection/></ProtectedRoute>} />
        </Routes>
      </Router>
  );
}

export default App;

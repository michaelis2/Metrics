import { BrowserRouter as Router, Routes, Route, Navigate, Link } from "react-router-dom";
import { useState } from "react";
import Dashboard from "./Dashboard";
import ThresholdSettings from "./ThresholdSettings";
import AlertPage from "./AlertPage";
import Login from "./Login";
import Users from "./Users";
import EditUser from "./EditUsers";
import ReportMaker from "./ReportMaker";
import MapComponent from "./Map";

function App() {
 
  const [token, setToken] = useState(localStorage.getItem("jwtToken") || null);
  const [role, setRole] = useState(localStorage.getItem("userRole") || null);

  const handleLogin = (jwtToken, userRole) => {
    localStorage.setItem("jwtToken", jwtToken);
    localStorage.setItem("userRole", userRole);
    setToken(jwtToken);
    setRole(userRole);
  };

  const handleLogout = () => {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userRole");
    setToken(null);
    setRole(null);
  };

  const ProtectedRoute = ({ children, requireAdmin = false }) => {
    if (!token) return <Navigate to="/login" replace />;
    if (requireAdmin && role !== "ADMIN") return <Navigate to="/" replace />; 
    return children;
  };

  return (
    <Router>
      <nav>
        {token && (
          <>
            <Link to="/">Dashboard</Link> | 
            <Link to="/map">Map</Link> | 
            <Link to="/alarm">Alert</Link> | 
            <Link to="/thresholds">Thresholds</Link> | 
            <Link to="/report">Reports</Link> | 
            {role === "ADMIN" && <Link to="/users">Users</Link>} {/* Only admin sees Users */}
            <button onClick={handleLogout} style={{ marginLeft: "10px" }}>Logout</button>
          </>
        )}
      </nav>

      <Routes>
        <Route path="/login" element={<Login onLogin={handleLogin} />} />
        <Route path="/" element={<ProtectedRoute><Dashboard token={token} /></ProtectedRoute>} />
        <Route path="/thresholds" element={<ProtectedRoute><ThresholdSettings token={token} /></ProtectedRoute>} />
        <Route path="/users" element={<ProtectedRoute requireAdmin={true}><Users token={token} /></ProtectedRoute>} />
        <Route path="/edit-user/:userId" element={<ProtectedRoute requireAdmin={true}><EditUser token={token} /></ProtectedRoute>} />
        <Route path="/alarm" element={<ProtectedRoute><AlertPage token={token} /></ProtectedRoute>} />
        <Route path="*" element={<Navigate to={token ? "/" : "/login"} replace />} />
        <Route path="/report"  element={<ProtectedRoute><ReportMaker token={token} /></ProtectedRoute>}/>
        <Route path="/map" element={<ProtectedRoute><MapComponent token={token} /></ProtectedRoute>}/>
      </Routes>
    </Router>
  );
}

export default App;

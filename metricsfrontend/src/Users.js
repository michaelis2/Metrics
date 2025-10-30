import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newUser, setNewUser] = useState({ username: "", email: "", password: "" });
  const navigate = useNavigate();
  const token = localStorage.getItem("jwtToken");

  const fetchUsers = async () => {
    try {
      const response = await fetch("http://localhost:9090/api/auth/users", {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!response.ok) throw new Error("Failed to fetch users");
      const data = await response.json();
      setUsers(data);
    } catch (err) {
      console.error("Error:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  
  const handleToggleActive = async (id, currentStatus) => {
    try {
      const response = await fetch(`http://localhost:9090/api/auth/users/${id}/active`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(!currentStatus),
      });

      if (response.ok) {
        setUsers((prev) =>
          prev.map((user) => (user.id === id ? { ...user, active: !currentStatus } : user))
        );
      } else {
        console.error("Failed to update user status");
      }
    } catch (err) {
      console.error("Error updating status:", err);
    }
  };

  // Add new user
  const handleAddUser = async () => {
    if (!newUser.username || !newUser.email || !newUser.password) {
      alert("All fields are required");
      return;
    }

    try {
      const response = await fetch("http://localhost:9090/api/auth/users", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(newUser),
      });

      if (response.ok) {
        const createdUser = await response.json();
        setUsers((prev) => [...prev, createdUser]);
        setShowModal(false);
        setNewUser({ username: "", email: "", password: "" });
      } else {
        alert("Failed to create user");
      }
    } catch (err) {
      console.error("Error creating user:", err);
    }
  };

  const handleEditUser = (id) => {
    navigate(`/edit-user/${id}`);
  };

  if (loading) return <p>Loading users...</p>;

  return (
    <div style={{ padding: "30px" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <h2 style={{ fontSize: "24px" }}>User Management</h2>
        <button
          onClick={() => setShowModal(true)}
          style={{ backgroundColor: "#C9A9A6", color: "white", border: "none", padding: "8px 14px", borderRadius: "6px", cursor: "pointer" }}
        >
          + Add User
        </button>
      </div>

      <table style={{ width: "100%", borderCollapse: "collapse", marginTop: "20px" }}>
        <thead>
          <tr style={{ backgroundColor: "#f3f3f3", textAlign: "left" }}>
            <th style={{ padding: "10px" }}>ID</th>
            <th style={{ padding: "10px" }}>Username</th>
            <th style={{ padding: "10px" }}>Email</th>
            <th style={{ padding: "10px" }}>Password</th>
            <th style={{ padding: "10px" }}>Active</th>
            <th style={{ padding: "10px" }}>Created Date</th>
            <th style={{ padding: "10px" }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id} style={{ borderBottom: "1px solid #ddd" }}>
              <td style={{ padding: "10px" }}>{user.id}</td>
              <td style={{ padding: "10px" }}>{user.username}</td>
              <td style={{ padding: "10px" }}>{user.email}</td>
              <td style={{ padding: "10px", fontFamily: "monospace", fontSize: "13px", color: "#555" }}>
                {user.password ? user.password : "••••••"}
              </td>
              <td style={{ padding: "10px" }}>
                <label className="switch">
                  <input type="checkbox" checked={user.active} onChange={() => handleToggleActive(user.id, user.active)} />
                  <span className="slider"></span>
                </label>
              </td>
              <td style={{ padding: "10px" }}>{new Date(user.createdDate).toLocaleString()}</td>
              <td style={{ padding: "10px" }}>
                <button
                  style={{ opacity: 0.6, cursor: "pointer", padding: "5px 10px", borderRadius: "4px", border: "1px solid #ccc" }}
                  onClick={() => handleEditUser(user.id)}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Add User Modal */}
      {showModal && (
        <div style={{ position: "fixed", top: 0, left: 0, width: "100%", height: "100%", backgroundColor: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center" }}>
          <div style={{ backgroundColor: "white", padding: "25px", borderRadius: "10px", width: "350px", boxShadow: "0 4px 12px rgba(0,0,0,0.2)" }}>
            <h3 style={{ marginBottom: "15px" }}>Add New User</h3>
            <label>Username:</label>
            <input type="text" value={newUser.username} onChange={(e) => setNewUser({ ...newUser, username: e.target.value })} style={{ width: "100%", padding: "8px", marginBottom: "10px" }} />
            <label>Email:</label>
            <input type="email" value={newUser.email} onChange={(e) => setNewUser({ ...newUser, email: e.target.value })} style={{ width: "100%", padding: "8px", marginBottom: "10px" }} />
            <label>Password:</label>
            <input type="password" value={newUser.password} onChange={(e) => setNewUser({ ...newUser, password: e.target.value })} style={{ width: "100%", padding: "8px", marginBottom: "15px" }} />
            <div style={{ textAlign: "right" }}>
              <button onClick={() => setShowModal(false)} style={{ backgroundColor: "#ccc", border: "none", padding: "8px 12px", borderRadius: "5px", marginRight: "10px", cursor: "pointer" }}>Cancel</button>
              <button onClick={handleAddUser} style={{ backgroundColor: "#FFDFF3", color: "white", border: "none", padding: "8px 12px", borderRadius: "5px", cursor: "pointer" }}>Add</button>
            </div>
          </div>
        </div>
      )}

      {/* Switch CSS */}
      <style>
        {`
          .switch { position: relative; display: inline-block; width: 42px; height: 22px; }
          .switch input { opacity: 0; width: 0; height: 0; }
          .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .4s; border-radius: 22px; }
          .slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
          input:checked + .slider { background-color: #FFDFF3; }
          input:checked + .slider:before { transform: translateX(20px); }
        `}
      </style>
    </div>
  );
}

export default Users;

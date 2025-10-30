import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

const EditUser = () => {
  const { userId } = useParams();
  const navigate = useNavigate();
  const token = localStorage.getItem("jwtToken"); 

  const [user, setUser] = useState({
    username: "",
    password: "",
    email: "",
    active: true,
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await axios.get(
          `http://localhost:9090/api/auth/users/${userId}`, 
          { headers: { Authorization: `Bearer ${token}` } }
        );
        setUser({
          username: res.data.username,
          password: "", 
          email: res.data.email,
          active: res.data.active,
        });
      } catch (err) {
        console.error(err);
        if (err.response && err.response.status === 403) {
          setError("You do not have permission to view this user.");
        } else {
          setError("Failed to fetch user data.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, [userId, token]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setUser((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSave = async () => {
    try {
      await axios.put(
        `http://localhost:9090/api/auth/users/${userId}`, 
        user,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      alert("User updated successfully!");
      navigate("/users"); 
    } catch (err) {
      console.error(err);
      if (err.response && err.response.status === 403) {
        setError("You do not have permission to update this user.");
      } else {
        setError("Failed to update user. Try again.");
      }
    }
  };

  if (loading) return <p>Loading user data...</p>;

  return (
    <div
      style={{
        maxWidth: "400px",
        margin: "50px auto",
        padding: "20px",
        border: "1px solid #ccc",
        borderRadius: "8px",
      }}
    >
      <h2>Edit User</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <div style={{ marginBottom: "15px" }}>
        <label>Username:</label>
        <input
          type="text"
          name="username"
          value={user.username}
          onChange={handleChange}
          style={{ width: "100%", padding: "8px" }}
        />
      </div>

      <div style={{ marginBottom: "15px" }}>
        <label>Password:</label>
        <input
          type="password"
          name="password"
          value={user.password}
          onChange={handleChange}
          placeholder="Leave blank to keep current password"
          style={{ width: "100%", padding: "8px" }}
        />
      </div>

      <div style={{ marginBottom: "15px" }}>
        <label>Email:</label>
        <input
          type="email"
          name="email"
          value={user.email}
          onChange={handleChange}
          style={{ width: "100%", padding: "8px" }}
        />
      </div>

      <div style={{ marginBottom: "15px" }}>
        <label>
          Active:
          <input
            type="checkbox"
            name="active"
            checked={user.active}
            onChange={handleChange}
            style={{ marginLeft: "10px" }}
          />
        </label>
      </div>

      <button
        onClick={handleSave}
        style={{
          padding: "10px",
          backgroundColor: "#674494",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
        }}
      >
        Save
      </button>
    </div>
  );
};

export default EditUser;

import axios from "axios";
import React, { useState, useEffect } from "react";

const ThresholdSettings = ({ token }) => {
  const [ipAddress, setIpAddress] = useState("");
  const [metricType, setMetricType] = useState("CPU");
  const [threshold, setThreshold] = useState("");
  const [thresholds, setThresholds] = useState([]); 


  useEffect(() => {
    const fetchThresholds = async () => {
      try {
        const res = await axios.get("http://localhost:9090/api/thresholds", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        setThresholds(res.data);
      } catch (err) {
        console.error("Error fetching thresholds:", err);
      }
    };

    fetchThresholds();
  }, [token]);

  const handleSave = async () => {
    if (!ipAddress || !threshold) {
      alert("Please fill in all fields");
      return;
    }

    const thresholdData = {
      ipAddress,
      metricType,
      threshold: Number(threshold),
    };

    try {
      const res = await axios.post(
        "http://localhost:9090/api/thresholds",
        thresholdData,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      alert("Threshold saved successfully!");
      console.log("Saved threshold:", res.data);

    
      setThresholds((prev) => [...prev, res.data]);

      
      setIpAddress("");
      setThreshold("");
      setMetricType("CPU");
    } catch (err) {
      console.error(err);
      if (err.response && err.response.status === 403) {
        alert("Unauthorized: Please log in again.");
      } else {
        alert("Failed to save threshold. Try again.");
      }
    }
  };

  return (
    <div
      style={{
        maxWidth: "600px",
        margin: "50px auto",
        padding: "20px",
        border: "1px solid #ccc",
        borderRadius: "8px",
        backgroundColor: "#f9f9f9",
      }}
    >
      <h2>Set Threshold</h2>

      <div style={{ marginBottom: "15px" }}>
        <label>IP Address:</label>
        <input
          type="text"
          value={ipAddress}
          onChange={(e) => setIpAddress(e.target.value)}
          placeholder="Enter IP address"
          style={{ width: "90%", padding: "8px", marginTop: "5px" }}
        />
      </div>

      <div style={{ marginBottom: "15px" }}>
        <label>Metric Type:</label>
        <select
          value={metricType}
          onChange={(e) => setMetricType(e.target.value)}
          style={{ width: "95%", padding: "8px", marginTop: "5px" }}
        >
          <option value="CPU">CPU</option>
          <option value="MEMORY">Memory</option>
          <option value="DISK">Disk</option>
        </select>
      </div>

      <div style={{ marginBottom: "15px" }}>
        <label>Threshold (%):</label>
        <input
          type="number"
          value={threshold}
          onChange={(e) => setThreshold(e.target.value)}
          placeholder="Enter threshold value"
          style={{ width: "90%", padding: "8px", marginTop: "5px" }}
        />
      </div>

      <button
        onClick={handleSave}
        style={{
          width: "100%",
          padding: "10px",
          backgroundColor: "#C9A9A6",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
          marginBottom: "20px",
        }}
      >
        Save Threshold
      </button>

      {/* Render the thresholds table if there is any data */}
      {thresholds.length > 0 && (
        <div>
          <h3>Saved Thresholds</h3>
          <table
            style={{
              width: "100%",
              borderCollapse: "collapse",
              marginTop: "10px",
            }}
          >
            <thead>
              <tr>
                <th style={{ border: "1px solid #ccc", padding: "8px" }}>
                  IP Address
                </th>
                <th style={{ border: "1px solid #ccc", padding: "8px" }}>
                  Metric Type
                </th>
                <th style={{ border: "1px solid #ccc", padding: "8px" }}>
                  Threshold (%)
                </th>
              </tr>
            </thead>
            <tbody>
              {thresholds.map((t, index) => (
                <tr key={index}>
                  <td style={{ border: "1px solid #ccc", padding: "8px" }}>
                    {t.ipAddress}
                  </td>
                  <td style={{ border: "1px solid #ccc", padding: "8px" }}>
                    {t.metricType}
                  </td>
                  <td style={{ border: "1px solid #ccc", padding: "8px" }}>
                    {t.threshold}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ThresholdSettings;

import React, { useEffect, useState } from "react";
import "./AlertPage.css";

function AlertsPage({ token }) {
  const [alerts, setAlerts] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchAlerts = async () => {
      try {
        const response = await fetch("http://localhost:9090/alerts", {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });

        if (response.status === 403 || response.status === 401) {
          setError("Unauthorized: Please log in again.");
          return;
        }

        if (!response.ok) throw new Error("Failed to fetch alerts");

        const data = await response.json();
        setAlerts(data);
      } catch (err) {
        console.error("Error fetching alerts:", err);
        setError("Error loading alerts. Please try again.");
      }
    };

    fetchAlerts();
  }, [token]); 

  return (
    <div className="alerts-container">
      <h1 className="alerts-title">Alerts Dashboard</h1>
      {error && <p style={{ color: "red" }}>{error}</p>}

      <table className="alerts-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>IP Address</th>
            <th>Metric</th>
            <th>Threshold</th>
            <th>Actual Value</th>
            <th>Message</th>
            <th>Timestamp</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {alerts.map((alert) => (
            <tr
              key={alert.id}
              className={alert.active ? "active-alert" : "inactive-alert"}
            >
              <td>{alert.id}</td>
              <td>{alert.ipAddress}</td>
              <td>{alert.metricType}</td>
              <td>{alert.thresholdValue}%</td>
              <td>{alert.actualValue}%</td>
              <td>{alert.message}</td>
              <td>{new Date(alert.timestamp).toLocaleString()}</td>
              <td>{alert.active ? "Active" : "Resolved"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AlertsPage;

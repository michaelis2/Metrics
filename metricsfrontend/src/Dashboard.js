import React, { useEffect, useState, useRef } from "react";
import axios from "axios";
import GridLayout from "react-grid-layout";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  PieChart, Pie, Cell
} from "recharts";
import "./Dashboard.css";
import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";

const Dashboard = ({ token }) => {
  const [cpu, setCpu] = useState([]);
  const [memory, setMemory] = useState([]);
  const [disk, setDisk] = useState([]);
  const [heartbeat, setHeartbeat] = useState([]);
  const [layout, setLayout] = useState([]);
  const [error, setError] = useState("");
  const [saveMessage, setSaveMessage] = useState("");

  const username = localStorage.getItem("username");

  const saveTimeout = useRef(null); 

  const defaultLayout = [
    { i: "cpu", x: 0, y: 0, w: 3, h: 2 },
    { i: "memory", x: 3, y: 0, w: 3, h: 2 },
    { i: "disk", x: 0, y: 2, w: 3, h: 2 },
    { i: "heartbeat", x: 3, y: 2, w: 3, h: 2 },
    { i: "pie", x: 6, y: 0, w: 3, h: 4 },
  ];

  useEffect(() => {
    if (!token) {
      setError("No token found. Please log in again.");
      return;
    }

    const fetchData = async () => {
      try {
        const config = { headers: { Authorization: `Bearer ${token}` } };

        const [cpuRes, memRes, diskRes, hbRes] = await Promise.all([
          axios.get("http://localhost:9090/api/metrics/cpu", config),
          axios.get("http://localhost:9090/api/metrics/memory", config),
          axios.get("http://localhost:9090/api/metrics/disk", config),
          axios.get("http://localhost:9090/api/metrics/heartbeat", config),
        ]);
        setCpu(cpuRes.data);
        setMemory(memRes.data);
        setDisk(diskRes.data);
        setHeartbeat(hbRes.data);

        const layoutRes = await axios.get(
          `http://localhost:9090/api/auth/users/${username}/layout`,
          config
        );

        let savedLayout = layoutRes.data;
        if (typeof savedLayout === "string") {
          try {
            savedLayout = JSON.parse(savedLayout);
          } catch {
            savedLayout = defaultLayout;
          }
        }
        setLayout(Array.isArray(savedLayout) && savedLayout.length > 0 ? savedLayout : defaultLayout);
      } catch (err) {
        console.error("Error fetching data:", err);
        setError("Failed to load dashboard data.");
      }
    };

    fetchData();
  }, [token]);

  const handleLayoutChange = (newLayout) => {
    setLayout(newLayout);
    setSaveMessage("");

    
    if (saveTimeout.current) clearTimeout(saveTimeout.current);
    saveTimeout.current = setTimeout(() => {
      saveLayout(newLayout);
    }, 1000);
  };

  const saveLayout = async (layoutToSave) => {
    try {
      const config = {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      };
      await axios.post(
        `http://localhost:9090/api/auth/users/${username}/layout`,
        JSON.stringify(layoutToSave),
        config
      );
      setSaveMessage("Layout saved!");
    } catch (err) {
      console.error("Failed to save layout:", err);
      setSaveMessage("Failed to save layout.");
    }
  };

  const renderLineChart = (data, title, color) => (
    <div className="chart-card" key={title.toLowerCase()}>
      <h3>{title}</h3>
      <LineChart width={400} height={250} data={data}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="timestamp" tickFormatter={(ts) => ts.slice(11, 16)} />
        <YAxis />
        <Tooltip />
        <Legend />
        <Line type="monotone" dataKey="value" stroke={color} />
      </LineChart>
    </div>
  );

  const renderPieChart = () => {
    const latestCpu = cpu.at(-1)?.value || 0;
    const latestMemory = memory.at(-1)?.value || 0;
    const latestDisk = disk.at(-1)?.value || 0;
    const latestHeartbeat = heartbeat.at(-1)?.value || 0;

    const pieData = [
      { name: "CPU", value: latestCpu },
      { name: "Memory", value: latestMemory },
      { name: "Disk", value: latestDisk },
      { name: "Heartbeat", value: latestHeartbeat },
    ];
    const COLORS = ["#F8C8DC", "#DA70D6", "#770737", "#44948F"];

    return (
      <div className="chart-card" key="pie">
        <h3>Latest Metrics Distribution</h3>
        <PieChart width={400} height={250}>
          <Pie data={pieData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
            {pieData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </div>
    );
  };

  const charts = [
    { id: "cpu", component: renderLineChart(cpu, "CPU Usage", "#674494") },
    { id: "memory", component: renderLineChart(memory, "Memory Usage", "#944449") },
    { id: "disk", component: renderLineChart(disk, "Disk Usage", "#719444") },
    { id: "heartbeat", component: renderLineChart(heartbeat, "Heartbeat", "#44948F") },
    { id: "pie", component: renderPieChart() },
  ];

  return (
    <div className="dashboard-container">
      <h2>Dashboard</h2>
      {error && <div className="error-message">{error}</div>}
      {saveMessage && <div className="save-message">{saveMessage}</div>}

      <GridLayout
        className="layout"
        layout={layout.length ? layout : defaultLayout}
        cols={12}
        rowHeight={150}
        width={1200}
        isResizable={true} 
        isDraggable={true}
        onLayoutChange={handleLayoutChange} 
      >
        {charts.map((chart) => (
          <div key={chart.id}>{chart.component}</div>
        ))}
      </GridLayout>
    </div>
  );
};

export default Dashboard;

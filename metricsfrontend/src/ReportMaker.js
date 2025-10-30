// ReportMaker.js
import React, { useState } from "react";
import axios from "axios";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from "recharts";
import * as XLSX from "xlsx";
import { saveAs } from "file-saver";
import "./ReportMaker.css";

const ReportMaker = ({ token }) => {
  const [selectedMetrics, setSelectedMetrics] = useState([]);
  const [days, setDays] = useState(1);
  const [reportData, setReportData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [viewMode, setViewMode] = useState("chart"); 

  const metricsOptions = ["cpu", "memory", "disk", "heartbeat"];
  const COLORS = ["#674494", "#944449", "#719444", "#44948F"];

  const handleMetricToggle = (metric) => {
    setSelectedMetrics((prev) =>
      prev.includes(metric)
        ? prev.filter((m) => m !== metric)
        : [...prev, metric]
    );
  };

  const fetchReport = async () => {
    if (selectedMetrics.length === 0) {
      setError("Please select at least one metric.");
      return;
    }

    setError("");
    setLoading(true);
    setReportData([]);

    try {
      const config = { headers: { Authorization: `Bearer ${token}` } };
      const responses = await Promise.all(
        selectedMetrics.map((metric) =>
          axios.get(
            `http://localhost:9090/api/metrics/${metric}?days=${days}`,
            config
          )
        )
      );

      const combinedData = responses.map((res, idx) => ({
        metric: selectedMetrics[idx],
        data: res.data,
      }));

      setReportData(combinedData);
    } catch (err) {
      console.error("Error fetching report:", err);
      setError("Failed to load report data.");
    } finally {
      setLoading(false);
    }
  };

  const exportToExcel = () => {
    if (reportData.length === 0) return;

    const wb = XLSX.utils.book_new();

    reportData.forEach((report) => {
      const sheetData = report.data.map((item) => ({
        Timestamp: item.timestamp,
        Value: item.value,
        Client_IP: item.clientIp,
        Type: report.metric.toUpperCase(),
      }));

      const ws = XLSX.utils.json_to_sheet(sheetData);
      XLSX.utils.book_append_sheet(wb, ws, report.metric.toUpperCase());
    });

    const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
    const blob = new Blob([wbout], { type: "application/octet-stream" });
    saveAs(blob, `SystemMetrics_Report_${days}days.xlsx`);
  };

  return (
    <div className="reportmaker-container">
      <h2>Report</h2>

      <div className="report-controls">
        <div className="metric-selection">
          <h4>Select Metrics:</h4>
          <div className="checkbox-group">
            {metricsOptions.map((metric) => (
              <label key={metric}>
                <input
                  type="checkbox"
                  checked={selectedMetrics.includes(metric)}
                  onChange={() => handleMetricToggle(metric)}
                />
                {metric.toUpperCase()}
              </label>
            ))}
          </div>
        </div>

        <div className="days-selection">
          <h4>Select Time Range:</h4>
          <select
            value={days}
            onChange={(e) => setDays(Number(e.target.value))}
          >
            <option value={1}>Today</option>
            <option value={7}>Last 7 Days</option>
            <option value={30}>Last 30 Days</option>
          </select>
        </div>

        <div className="view-toggle">
          <h4>View Mode:</h4>
          <select
            value={viewMode}
            onChange={(e) => setViewMode(e.target.value)}
          >
            <option value="chart">📈 Chart</option>
            <option value="list">📋 List</option>
          </select>
        </div>

        <button onClick={fetchReport} className="generate-button">
          Generate Report
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}
      {loading && <div className="loading">Loading report...</div>}

      {!loading && reportData.length > 0 && (
        <>
          <div className="charts-container">
            {viewMode === "chart" &&
              reportData.map((report, index) => (
                <div className="chart-card" key={report.metric}>
                  <h3>
                    {report.metric.toUpperCase()} Report (Last {days} Days)
                  </h3>
                  <LineChart width={500} height={300} data={report.data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="timestamp"
                      tickFormatter={(ts) => ts.slice(5, 16)}
                    />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="value"
                      stroke={COLORS[index % COLORS.length]}
                    />
                  </LineChart>
                </div>
              ))}

            {viewMode === "list" &&
              reportData.map((report) => (
                <div className="list-card" key={report.metric}>
                  <h3>
                    {report.metric.toUpperCase()} Report (Last {days} Days)
                  </h3>
                  <table className="metrics-table">
                    <thead>
                      <tr>
                        <th>Timestamp</th>
                        <th>Client IP</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      {report.data.map((item, idx) => (
                        <tr key={idx}>
                          <td>{item.timestamp}</td>
                          <td>{item.clientIp}</td>
                          <td>{item.value}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ))}
          </div>

          <div className="export-section">
            <button onClick={exportToExcel} className="export-button">
              📤 Export to Excel
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default ReportMaker;

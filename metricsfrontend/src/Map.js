import React, { useEffect, useState } from "react";
import "ol/ol.css";
import { Map, View } from "ol";
import TileLayer from "ol/layer/Tile";
import { OSM } from "ol/source";
import VectorLayer from "ol/layer/Vector";
import VectorSource from "ol/source/Vector";
import { fromLonLat } from "ol/proj";
import Feature from "ol/Feature";
import Point from "ol/geom/Point";
import HeatmapLayer from "ol/layer/Heatmap";
import GeoJSON from "ol/format/GeoJSON";
import { Stroke, Fill, Style } from "ol/style";
import axios from "axios";

const MapComponent = ({ token }) => {
  const [status, setStatus] = useState("Loading map...");
  const apiKey = "2VJDYM3xXrNmLnDBtoFxNMo9VkPGKDdv";

  useEffect(() => {
    if (!token) {
      console.error("No token found.");
      setStatus("No token found. log in again.");
      return;
    }

    const map = new Map({
      target: "map",
      layers: [
        new TileLayer({
          source: new OSM(),
        }),
      ],
      view: new View({
        center: fromLonLat([51.5310, 25.2854]), // doha
        zoom: 13,
      }),
    });


    const loadGeoJSON = async () => {
      try {
        const response = await fetch("/albidda.geojson");

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const geojson = await response.json();
        const format = new GeoJSON();
        const features = format.readFeatures(geojson, {
          dataProjection: "EPSG:4326",
          featureProjection: "EPSG:3857",
        });

        const vectorSource = new VectorSource({ features });
        const alBiddaLayer = new VectorLayer({
          source: vectorSource,
          style: new Style({
            stroke: new Stroke({
              color: "#FF0000",
              width: 3,
            }),
            fill: new Fill({
              color: "rgba(255, 0, 0, 0.1)",
            }),
          }),
        });

        map.addLayer(alBiddaLayer);

        const extent = vectorSource.getExtent();
        map.getView().fit(extent, { padding: [50, 50, 50, 50], maxZoom: 16 });

        setStatus("Al Bidda outline loaded");
      } catch (error) {
        setStatus("failed to load Al Bidda geojson");
      }
    };

   
    const loadTrafficHeatmap = async () => {
      try {
        const points = [
          [25.2854, 51.5310], 
          [25.3200, 51.5200],
          [25.3600, 51.5500],
        ];

        const features = [];

        for (const [lat, lon] of points) {
          const url = `https://api.tomtom.com/traffic/services/4/flowSegmentData/relative0/10/json?point=${lat},${lon}&key=${apiKey}`;
          const res = await axios.get(url);
          const data = res.data.flowSegmentData;

          if (!data || !data.coordinates) {
            console.warn("Missing coordinate data at:", lat, lon);
            continue;
          }

          let coords = data.coordinates.coordinate;
          if (!Array.isArray(coords)) coords = [coords];

          const congestionRatio = data.currentSpeed / data.freeFlowSpeed;
          const weight = Math.max(0, Math.min(1, 1 - congestionRatio));

          coords.forEach((coord) => {
            const { latitude, longitude } = coord;
            if (latitude && longitude) {
              const feature = new Feature({
                geometry: new Point(fromLonLat([longitude, latitude])),
                weight,
              });
              features.push(feature);
            }
          });
        }

        const vectorSource = new VectorSource({ features });
        const heatmapLayer = new HeatmapLayer({
          source: vectorSource,
          blur: 20,
          radius: 15,
          weight: (feature) => feature.get("weight"),
        });

        map.addLayer(heatmapLayer);
      
      } catch (err) {
        setStatus("failed to load traffic data");
      }
    };

    loadGeoJSON();
    loadTrafficHeatmap();

    return () => map.setTarget(null);
  }, [token]);

  return (
    <div style={{ padding: "16px" }}>
      <h2>Qatar Map – Al Bidda Outline</h2>
      <p>{status}</p>
      <div
        id="map"
        style={{
          width: "100%",
          height: "600px",
          border: "1px solid #ccc",
        }}
      ></div>
    </div>
  );
};

export default MapComponent;

import React from "react";
import "../css/ResultPreview.css";

const ResultPreview = ({ result }) => {
  return (
    <div className="analysis-result-container">
      <h3 className="analysis-result-title">Analysis Result</h3>
      <pre className="analysis-result-text">{result}</pre>
    </div>
  );
};

export default ResultPreview;

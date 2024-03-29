import axios from "axios";
import React, { useState } from "react";
import "./App.css";
import FilePreview from "./component/FilePreview";
import ResultPreview from "./component/ResultPreview";

const App = () => {
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [selectedPattern, setSelectedPattern] = useState("visitor");
  const [uploadType, setUploadType] = useState("directory");
  const [uploadProgress, setUploadProgress] = useState(0);
  const [analysisResult, setAnalysisResult] = useState("");

  const handleFileChange = (event) => {
    const files = Array.from(event.target.files)
      .filter((file) => file.name.endsWith(".java"))
      .map((file) => ({
        name: file.name,
        fileData: file,
        preview: URL.createObjectURL(file),
      }));
    setAnalysisResult("");
    setSelectedFiles(files);
  };

  const handleRemoveFile = (index) => {
    const newFiles = selectedFiles.filter((_, i) => i !== index);
    setSelectedFiles(newFiles);
  };

  const handleAnalyze = async () => {
    if (selectedFiles.length === 0) {
      alert("No .java files selected!");
      return;
    }

    const formData = new FormData();
    formData.append("pattern", selectedPattern);
    selectedFiles.forEach((file) => {
      formData.append("files", file.fileData);
    });

    try {
      const response = await axios.post(
        "http://localhost:8080/upload",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
          onUploadProgress: (progressEvent) => {
            const percentCompleted = Math.round(
              (progressEvent.loaded * 100) / progressEvent.total
            );
            setUploadProgress(percentCompleted);
          },
        }
      );
      setAnalysisResult(response.data);
      setTimeout(() => setUploadProgress(0), 1500);
    } catch (error) {
      alert("An error occurred while uploading.");
      console.error("Upload error:", error);
      setTimeout(() => setUploadProgress(0), 1500);
    }
  };

  return (
    <div className="App">
      <h1>Design Pattern Verifier Application</h1>
      <h3>Select Source Code & Pattern to Analyze</h3>
      <div className="upload-section">
        <select
          value={uploadType}
          onChange={(e) => setUploadType(e.target.value)}
        >
          <option value="directory">Directory</option>
          <option value="files">Files</option>
        </select>

        <select
          className="margin-horizontals"
          value={selectedPattern}
          onChange={(e) => setSelectedPattern(e.target.value)}
        >
          <option value="visitor">Visitor</option>
          <option value="chain">Chain of Responsibility</option>
          <option value="observer">Observer</option>
        </select>

        <input
          className="margin-horizontals"
          type="file"
          onChange={handleFileChange}
          multiple={uploadType === "files"}
          webkitdirectory={uploadType === "directory" ? "true" : undefined}
          directory={uploadType === "directory" ? "true" : undefined}
          onClick={(event) => (event.target.value = "")}
        />

        <button className="margin-horizontals" onClick={handleAnalyze}>
          Analyze
        </button>
      </div>

      {uploadProgress !== 0 && (
        <div className="progress-bar">
          <div className="progress" style={{ width: `${uploadProgress}%` }}>
            {uploadProgress}%
          </div>
        </div>
      )}

      {analysisResult ? (
        <ResultPreview result={analysisResult} />
      ) : (
        <FilePreview files={selectedFiles} onRemove={handleRemoveFile} />
      )}
    </div>
  );
};

export default App;

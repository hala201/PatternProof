import axios from "axios";
import React, { useState } from "react";

const App = () => {
  const [selectedFiles, setSelectedFiles] = useState([]);

  const handleFileChange = (event) => {
    setSelectedFiles(event.target.files);
  };

  const handleUpload = async () => {
    if (selectedFiles.length === 0) {
      alert("No files selected!");
      return;
    }

    const formData = new FormData();
    for (let i = 0; i < selectedFiles.length; i++) {
      formData.append("files", selectedFiles[i]);
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/upload",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );
      alert(response.data);
    } catch (error) {
      alert("An error occurred while uploading the directory.");
      console.error("Upload error:", error);
    }
  };

  return (
    <div>
      <h2>Select Directory to Upload</h2>
      <input
        type="file"
        webkitdirectory="true"
        directory="true"
        onChange={handleFileChange}
        multiple
      />
      <button onClick={handleUpload}>Upload Directory</button>
    </div>
  );
};

export default App;

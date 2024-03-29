import React from "react";
import "../css/FilePreview.css";

const FilePreview = ({ files, onRemove }) => {
  return (
    <div className="file-preview-container">
      <h3 className="file-preview-title">Selected Files</h3>
      <ul className="file-list">
        {files.map((file, index) => (
          <li key={index} className="file-item">
            <span className="file-name">{file.name}</span>
            <span className="file-remove" onClick={() => onRemove(index)}>
              X
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FilePreview;

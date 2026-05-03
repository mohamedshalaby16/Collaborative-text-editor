package model;

public class SessionInfo {
    private String documentId;
    private String editorCode;
    private String viewerCode;

    public SessionInfo(String documentId, String editorCode, String viewerCode) {
        this.documentId = documentId;
        this.editorCode = editorCode;
        this.viewerCode = viewerCode;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public String getViewerCode() {
        return viewerCode;
    }

    @Override
    public String toString() {
        return documentId + " (E:" + editorCode + " V:" + viewerCode + ")";
    }
}

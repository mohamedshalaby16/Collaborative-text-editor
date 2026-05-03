package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;
import model.CursorInfo;
import model.Document;
import model.UserRole;
import network.MessageHandler;
import network.WebSocketClient;
import operations.DeleteBlockOperation;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.InsertCharacterOperation;

public class EditorUI extends JFrame {

    // Connection configuration fields
    private JTextField hostField;
    private JTextField portField;

    // Document management fields
    private JTextField documentCodeField;
    private JButton createDocButton;
    private JButton joinDocButton;
    private JPanel shareCodesPanel;
    private JTextField editorCodeField;
    private JTextField viewerCodeField;
    private JLabel roleLabel;
    private String currentDocumentId;
    private UserRole currentUserRole;
    private String currentShareCode;

    private JTextPane textPane;
    private JTextField usernameField;
    private JButton connectButton;
    private JLabel statusLabel;

    private JPanel usersPanel;
    private JLabel usersTitle;
    private Map<Integer, JLabel> userLabels;

    private boolean isRemoteUpdate = false;

    private Document document;
    private final String blockId = "block-1";
    private int localUserId;
    private int localClock = 0;

    private Map<Integer, CursorInfo> remoteCursors;
    private java.util.List<Object> remoteCursorHighlightTags;

    private WebSocketClient wsClient;
    private boolean isConnected = false;
    private long lastViewerEditWarningAt = 0;

    public EditorUI() {
        localUserId = (int) (Math.random() * 100000);
        currentUserRole = UserRole.VIEWER; // default until joined

        setTitle("Collaborative Text Editor - Phase 3");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initPhase1Core();
        initComponents();
        layoutComponents();
        addListeners();

        setVisible(true);
    }

    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            sendLeaveMessage();
        }
        super.processWindowEvent(e);
    }

    private void initPhase1Core() {
        document = new Document();
        remoteCursors = new HashMap<>();
        remoteCursorHighlightTags = new ArrayList<>();
        userLabels = new HashMap<>();
        document.apply(new InsertBlockOperation(blockId, null, localUserId, localClock));
        localClock++;
    }

    private void initComponents() {
        textPane = new JTextPane();
        textPane.setEditable(false);

        usernameField = new JTextField("user" + localUserId, 8);
        hostField = new JTextField("localhost", 10);
        portField = new JTextField("9091", 5);

        documentCodeField = new JTextField(15);
        createDocButton = new JButton("Create New Doc");
        joinDocButton = new JButton("Join with Code");
        editorCodeField = new JTextField(10);
        editorCodeField.setEditable(false);
        viewerCodeField = new JTextField(10);
        viewerCodeField.setEditable(false);
        roleLabel = new JLabel("Role: none");

        shareCodesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        shareCodesPanel.add(new JLabel("Editor Code:"));
        shareCodesPanel.add(editorCodeField);
        shareCodesPanel.add(new JLabel("Viewer Code:"));
        shareCodesPanel.add(viewerCodeField);
        shareCodesPanel.setVisible(false);

        connectButton = new JButton("Connect");
        statusLabel = new JLabel("Not connected");

        usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        usersPanel.setPreferredSize(new Dimension(150, 0));

        usersTitle = new JLabel("Active Users");
        usersTitle.setFont(usersTitle.getFont().deriveFont(Font.BOLD));
        usersPanel.add(usersTitle);
        usersPanel.add(Box.createVerticalStrut(8));

        // Initially disable join buttons until connected
        createDocButton.setEnabled(false);
        joinDocButton.setEnabled(false);
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Connection row
        JPanel connectionRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionRow.add(new JLabel("Host:"));
        connectionRow.add(hostField);
        connectionRow.add(new JLabel("Port:"));
        connectionRow.add(portField);
        connectionRow.add(new JLabel("Username:"));
        connectionRow.add(usernameField);
        connectionRow.add(connectButton);
        connectionRow.add(statusLabel);

        // Document management row
        JPanel documentRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        documentRow.add(createDocButton);
        documentRow.add(new JLabel("Join Code:"));
        documentRow.add(documentCodeField);
        documentRow.add(joinDocButton);
        documentRow.add(roleLabel);
        documentRow.add(shareCodesPanel);

        topPanel.add(connectionRow);
        topPanel.add(documentRow);

        JScrollPane scrollPane = new JScrollPane(textPane);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(usersPanel, BorderLayout.EAST);
    }

    private void addListeners() {
        connectButton.addActionListener(e -> {
            if (!isConnected) {
                connectToServer();
            } else {
                disconnectFromServer();
            }
        });

        createDocButton.addActionListener(e -> createNewDocument());
        joinDocButton.addActionListener(e -> joinDocumentWithCode());

        textPane.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (currentUserRole == UserRole.VIEWER && currentDocumentId != null && isEditKey(e)) {
                    showViewerEditWarning();
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (currentUserRole == UserRole.VIEWER && currentDocumentId != null && !Character.isISOControl(e.getKeyChar())) {
                    showViewerEditWarning();
                }
            }
        });

        textPane.addCaretListener(e -> {
            if (!isRemoteUpdate && isConnected && currentUserRole == UserRole.EDITOR) {
                int position = e.getDot();
                String anchorCharId = document.getCharIdBeforeOffset(blockId, position);
                String message = MessageHandler.cursorToMessage(localUserId, getUsername(), position, anchorCharId,
                        currentDocumentId);
                wsClient.sendMessage(message);
            }
        });

        AbstractDocument doc = (AbstractDocument) textPane.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (isRemoteUpdate) {
                    super.insertString(fb, offset, string, attr);
                } else if (currentUserRole == UserRole.EDITOR) {
                    handleInsert(offset, string);
                } else if (currentUserRole == UserRole.VIEWER && string != null && !string.isEmpty()) {
                    showViewerEditWarning();
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length)
                    throws BadLocationException {
                if (isRemoteUpdate) {
                    super.remove(fb, offset, length);
                } else if (currentUserRole == UserRole.EDITOR) {
                    handleRemove(offset, length);
                } else if (currentUserRole == UserRole.VIEWER && length > 0) {
                    showViewerEditWarning();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (isRemoteUpdate) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }
                if (currentUserRole != UserRole.EDITOR) {
                    if (currentUserRole == UserRole.VIEWER) {
                        showViewerEditWarning();
                    }
                    return;
                }

                if (length == 0 && text != null && !text.isEmpty()) {
                    handleInsert(offset, text);
                    return;
                }
                if (length > 0 && (text == null || text.isEmpty())) {
                    handleDeleteRange(offset, length);
                    return;
                }
                if (length > 0) {
                    handleDeleteRange(offset, length);
                }
                if (text != null && !text.isEmpty()) {
                    handleInsert(offset, text);
                }
            }
        });
    }

    private void createNewDocument() {
        if (!isConnected || wsClient == null) {
            setStatus("Not connected to server!");
            return;
        }

        resetLocalDocumentState();
        String message = MessageHandler.createSessionMessage(localUserId, getUsername());
        wsClient.sendMessage(message);
        setStatus("Creating new document...");
    }

    private void joinDocumentWithCode() {
        if (!isConnected || wsClient == null) {
            setStatus("Not connected to server!");
            return;
        }

        String code = documentCodeField.getText().trim();
        if (code.isEmpty()) {
            setStatus("Please enter a share code!");
            JOptionPane.showMessageDialog(this, "Please enter the code.", "Missing Join Code",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        resetLocalDocumentState();
        String message = MessageHandler.joinSessionMessage(localUserId, getUsername(), code);
        wsClient.sendMessage(message);
        setStatus("Joining document with code: " + code);
    }

    private boolean isEditKey(java.awt.event.KeyEvent e) {
        if (e.isControlDown() && (e.getKeyCode() == java.awt.event.KeyEvent.VK_V
                || e.getKeyCode() == java.awt.event.KeyEvent.VK_X)) {
            return true;
        }
        return e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE
                || e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE
                || e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER;
    }

    private void showViewerEditWarning() {
        setStatus("Viewers cannot edit this document.");
        Toolkit.getDefaultToolkit().beep();
        long now = System.currentTimeMillis();
        if (now - lastViewerEditWarningAt < 2000) {
            return;
        }
        lastViewerEditWarningAt = now;
        JOptionPane.showMessageDialog(this, "You cannot type as a viewer.", "Read-only Viewer",
                JOptionPane.WARNING_MESSAGE);
    }

    private void resetLocalDocumentState() {
        isRemoteUpdate = true;
        document = new Document();
        localClock = 0;
        currentDocumentId = null;
        currentShareCode = null;
        currentUserRole = UserRole.VIEWER;
        remoteCursors.clear();
        remoteCursorHighlightTags.clear();
        userLabels.clear();
        editorCodeField.setText("");
        viewerCodeField.setText("");
        shareCodesPanel.setVisible(false);
        roleLabel.setText("Role: none");
        textPane.setText("");
        textPane.setEditable(false);
        usersPanel.removeAll();
        usersPanel.add(usersTitle);
        usersPanel.add(Box.createVerticalStrut(8));
        document.apply(new InsertBlockOperation(blockId, null, localUserId, localClock));
        localClock++;
        usersPanel.revalidate();
        usersPanel.repaint();
        isRemoteUpdate = false;
    }

    private void handleInsert(int offset, String string) {
        if (isRemoteUpdate || string == null || string.isEmpty())
            return;
        handleInsertAtOffset(offset, string);
    }

    private void handleRemove(int offset, int length) {
        if (isRemoteUpdate || length <= 0)
            return;
        handleDeleteRange(offset, length);
    }

    private void handleInsertAtOffset(int offset, String text) {
        if (currentUserRole != UserRole.EDITOR) {
            return;
        }
        String parentId = document.getCharIdBeforeOffset(blockId, offset);
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);
            InsertCharacterOperation op = new InsertCharacterOperation(localUserId, localClock, value, parentId,
                    blockId);
            document.apply(op);
            parentId = op.getCharId();
            localClock++;
            if (wsClient != null && wsClient.isConnected()) {
                String message = MessageHandler.operationToMessage(op, currentDocumentId);
                wsClient.sendMessage(message);
            }
        }
        refreshTextFromDocument(offset + text.length());
    }

    private void handleDeleteRange(int offset, int length) {
        if (currentUserRole != UserRole.EDITOR) {
            return;
        }
        java.util.List<String> visibleIds = document.getVisibleCharacterIds(blockId);
        java.util.List<String> idsToDelete = new ArrayList<>();
        int end = Math.min(offset + length, visibleIds.size());
        for (int i = offset; i < end; i++) {
            idsToDelete.add(visibleIds.get(i));
        }
        for (String charId : idsToDelete) {
            String[] parts = charId.split("-");
            int userId = Integer.parseInt(parts[0]);
            int clock = Integer.parseInt(parts[1]);
            DeleteCharacterOperation op = new DeleteCharacterOperation(userId, clock, blockId);
            document.apply(op);
            if (wsClient != null && wsClient.isConnected()) {
                String message = MessageHandler.operationToMessage(op, currentDocumentId);
                wsClient.sendMessage(message);
            }
        }
        refreshTextFromDocument(offset);
    }

    private void refreshTextFromDocument() {
        refreshTextFromDocument(textPane.getCaretPosition());
    }

    private void refreshTextFromDocument(int caretPosition) {
        isRemoteUpdate = true;
        String text = document.getText();
        textPane.setText(text);
        textPane.setCaretPosition(Math.max(0, Math.min(caretPosition, text.length())));
        isRemoteUpdate = false;

        SwingUtilities.invokeLater(() -> {
            Highlighter highlighter = textPane.getHighlighter();
            for (Object tag : remoteCursorHighlightTags)
                highlighter.removeHighlight(tag);
            remoteCursorHighlightTags.clear();
            java.util.List<String> visibleIds = document.getVisibleCharacterIds(blockId);
            for (CursorInfo cursor : remoteCursors.values()) {
                int cursorPosition = getRemoteCursorPosition(cursor, visibleIds, text.length());
                try {
                    Object tag = highlighter.addHighlight(cursorPosition, cursorPosition,
                            new RemoteCursorPainter(cursor.color));
                    remoteCursorHighlightTags.add(tag);
                } catch (BadLocationException e) {
                    System.out.println("Failed to draw remote cursor: " + e.getMessage());
                }
            }
        });
    }

    private int getRemoteCursorPosition(CursorInfo cursor, java.util.List<String> visibleIds, int textLength) {
        if (cursor.anchorCharId != null) {
            int index = visibleIds.indexOf(cursor.anchorCharId);
            if (index >= 0)
                return Math.min(index + 1, textLength);
        }
        return Math.max(0, Math.min(cursor.position, textLength));
    }

    private static class RemoteCursorPainter extends LayeredHighlighter.LayerPainter {
        private final Color color;

        RemoteCursorPainter(Color color) {
            this.color = color;
        }

        @Override
        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
            paintCaret(g, c, p0);
        }

        @Override
        public Shape paintLayer(Graphics g, int p0, int p1, Shape bounds, JTextComponent c, View view) {
            return paintCaret(g, c, p0);
        }

        private Shape paintCaret(Graphics g, JTextComponent c, int position) {
            try {
                Rectangle rect = c.modelToView2D(position).getBounds();
                g.setColor(color);
                g.fillRect(rect.x, rect.y, 2, rect.height);
                return rect;
            } catch (BadLocationException e) {
                return null;
            }
        }
    }

    private void addUserToPanel(CursorInfo cursor) {
        JLabel existingLabel = userLabels.get(cursor.userId);
        if (existingLabel != null) {
            existingLabel.setText("* " + cursor.username);
            existingLabel.setForeground(cursor.color);
            return;
        }
        JLabel label = new JLabel("* " + cursor.username);
        label.setForeground(cursor.color);
        userLabels.put(cursor.userId, label);
        usersPanel.add(label);
        usersPanel.revalidate();
        usersPanel.repaint();
    }

    private void removeUserFromPanel(int userId) {
        JLabel label = userLabels.remove(userId);
        if (label != null) {
            usersPanel.remove(label);
            usersPanel.revalidate();
            usersPanel.repaint();
        }
    }

    private void connectToServer() {
        String host = hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            setStatus("Invalid port number!");
            return;
        }

        hostField.setEnabled(false);
        portField.setEnabled(false);

        wsClient = new WebSocketClient(host, port, new WebSocketClient.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                SwingUtilities.invokeLater(() -> handleRemoteMessage(message));
            }

            @Override
            public void onConnected() {
                SwingUtilities.invokeLater(() -> {
                    isConnected = true;
                    setStatus("Connected to " + host + ":" + port);
                    connectButton.setText("Disconnect");
                    createDocButton.setEnabled(true);
                    joinDocButton.setEnabled(true);
                });
            }

            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    isConnected = false;
                    setStatus("Disconnected");
                    connectButton.setText("Connect");
                    textPane.setEditable(false);
                    currentUserRole = UserRole.VIEWER;
                    roleLabel.setText("Role: none");
                    editorCodeField.setText("");
                    viewerCodeField.setText("");
                    shareCodesPanel.setVisible(false);
                    createDocButton.setEnabled(false);
                    joinDocButton.setEnabled(false);
                    hostField.setEnabled(true);
                    portField.setEnabled(true);
                    remoteCursors.clear();
                    for (int uid : new ArrayList<>(userLabels.keySet())) {
                        removeUserFromPanel(uid);
                    }
                    refreshTextFromDocument();
                });
            }
        });
        wsClient.connect();
    }

    private void disconnectFromServer() {
        sendLeaveMessage();
        if (wsClient != null)
            wsClient.disconnect();
    }

    private void sendLeaveMessage() {
        if (wsClient != null && wsClient.isConnected() && currentDocumentId != null) {
            wsClient.sendMessage(MessageHandler.leaveToMessage(localUserId, currentDocumentId));
        }
    }

    private void handleRemoteMessage(String message) {
        // Handle session creation response
        if (MessageHandler.isSessionCreatedMessage(message)) {
            currentDocumentId = MessageHandler.getDocumentId(message);
            String editorCode = MessageHandler.getEditorCode(message);
            String viewerCode = MessageHandler.getViewerCode(message);
            currentShareCode = editorCode;
            currentUserRole = UserRole.EDITOR;
            textPane.setEditable(true);
            roleLabel.setText("Role: EDITOR");
            editorCodeField.setText(editorCode != null ? editorCode : "");
            viewerCodeField.setText(viewerCode != null ? viewerCode : "");
            shareCodesPanel.setVisible(true);
            setStatus("Document created! Editor and viewer codes are ready.");
            JOptionPane.showMessageDialog(this,
                    "Document Created!\n\nEditor code:\n" + editorCode + "\n\nViewer code:\n" + viewerCode,
                    "Document Created", JOptionPane.INFORMATION_MESSAGE);

            // Send join message for this session
            wsClient.sendMessage(MessageHandler.joinToMessage(localUserId, getUsername(), currentDocumentId));
            return;
        }

        // Handle join acceptance
        if (MessageHandler.isJoinAcceptedMessage(message)) {
            currentDocumentId = MessageHandler.getDocumentId(message);
            String role = MessageHandler.getRole(message);
            currentUserRole = "EDITOR".equals(role) ? UserRole.EDITOR : UserRole.VIEWER;

            if (currentUserRole == UserRole.EDITOR) {
                textPane.setEditable(true);
                roleLabel.setText("Role: EDITOR");
                String editorCode = MessageHandler.getEditorCode(message);
                String viewerCode = MessageHandler.getViewerCode(message);
                editorCodeField.setText(editorCode != null ? editorCode : "");
                viewerCodeField.setText(viewerCode != null ? viewerCode : "");
                shareCodesPanel.setVisible(editorCode != null || viewerCode != null);
                setStatus("Joined as EDITOR");
            } else {
                textPane.setEditable(false);
                roleLabel.setText("Role: VIEWER");
                editorCodeField.setText("");
                viewerCodeField.setText("");
                shareCodesPanel.setVisible(false);
                setStatus("Joined as VIEWER (read-only)");
            }

            for (String oldOperationMessage : MessageHandler.getOperationHistory(message)) {
                applyRemoteOperation(oldOperationMessage);
            }
            refreshTextFromDocument();

            // Send presence join
            wsClient.sendMessage(MessageHandler.joinToMessage(localUserId, getUsername(), currentDocumentId));
            return;
        }

        // Handle join rejection
        if (MessageHandler.isJoinRejectedMessage(message)) {
            String reason = MessageHandler.getRejectionReason(message);
            setStatus("Join failed: " + reason);
            JOptionPane.showMessageDialog(this, "Failed to join: " + reason, "Join Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (MessageHandler.isPermissionDeniedMessage(message)) {
            String reason = MessageHandler.getRejectionReason(message);
            setStatus("Permission denied: " + reason);
            JOptionPane.showMessageDialog(this, reason, "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (MessageHandler.isJoinMessage(message)) {
            int userId = MessageHandler.getPresenceUserId(message);
            if (userId == localUserId)
                return;
            String username = MessageHandler.getPresenceUsername(message);
            CursorInfo cursor = remoteCursors.get(userId);
            if (cursor == null) {
                cursor = new CursorInfo(userId, username, 0);
                remoteCursors.put(userId, cursor);
            }
            addUserToPanel(cursor);
            refreshTextFromDocument();
            return;
        }

        if (MessageHandler.isLeaveMessage(message)) {
            int userId = MessageHandler.getPresenceUserId(message);
            remoteCursors.remove(userId);
            removeUserFromPanel(userId);
            refreshTextFromDocument();
            return;
        }

        if (MessageHandler.isCursorMessage(message)) {
            int userId = MessageHandler.getCursorUserId(message);
            if (userId == localUserId)
                return;
            String username = MessageHandler.getCursorUsername(message);
            int position = MessageHandler.getCursorPosition(message);
            String anchorCharId = MessageHandler.getCursorAnchorCharId(message);
            if (!remoteCursors.containsKey(userId)) {
                CursorInfo cursor = new CursorInfo(userId, username, position, anchorCharId);
                remoteCursors.put(userId, cursor);
                addUserToPanel(cursor);
            } else {
                CursorInfo cursor = remoteCursors.get(userId);
                cursor.position = position;
                cursor.anchorCharId = anchorCharId;
            }
            refreshTextFromDocument();
            return;
        }

        Object operation = MessageHandler.messageToOperation(message);
        if (operation != null) {
            applyRemoteOperation(operation);
            refreshTextFromDocument();
        }
    }

    private void applyRemoteOperation(String operationMessage) {
        Object operation = MessageHandler.messageToOperation(operationMessage);
        if (operation != null) {
            applyRemoteOperation(operation);
        }
    }

    private void applyRemoteOperation(Object operation) {
        if (operation instanceof InsertCharacterOperation) {
            document.apply((InsertCharacterOperation) operation);
        } else if (operation instanceof DeleteCharacterOperation) {
            document.apply((DeleteCharacterOperation) operation);
        } else if (operation instanceof InsertBlockOperation) {
            document.apply((InsertBlockOperation) operation);
        } else if (operation instanceof DeleteBlockOperation) {
            document.apply((DeleteBlockOperation) operation);
        }
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getEditorText() {
        return textPane.getText();
    }

    public void setEditorText(String text) {
        isRemoteUpdate = true;
        textPane.setText(text);
        isRemoteUpdate = false;
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EditorUI::new);
    }
}

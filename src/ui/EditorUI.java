package ui;

import model.Document;
import operations.DeleteCharacterOperation;
import operations.InsertBlockOperation;
import operations.InsertCharacterOperation;
import network.WebSocketClient; /// Integration: Added for real network communication
import network.MessageHandler; /// Integration: Added for converting operations to/from messages

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditorUI extends JFrame {

    private final String HOST = "localhost";
    private final int PORT = 9091;
    /// Integration: Make sure server uses same port

    private JTextPane textPane;
    private JTextField usernameField;
    private JButton connectButton;
    private JLabel statusLabel;

    private boolean isRemoteUpdate = false;

    // Phase 1 integration
    private Document document;
    private final String blockId = "block-1";
    private final int localUserId = 1;
    private int localClock = 0;

    // Keeps inserted character IDs in order for simple delete-from-end
    private List<String> insertedCharIds;

    /// Integration: Network client and connection state
    private WebSocketClient wsClient;
    private boolean isConnected = false;

    public EditorUI() {
        setTitle("Collaborative Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initPhase1Core();
        initComponents();
        layoutComponents();
        addListeners();

        setVisible(true);
    }

    private void initPhase1Core() {
        document = new Document();
        insertedCharIds = new ArrayList<>();

        // Create one default block
        document.apply(new InsertBlockOperation(blockId, null, localUserId, localClock));
        localClock++;
    }

    private void initComponents() {
        textPane = new JTextPane();

        usernameField = new JTextField("user1", 10);
        connectButton = new JButton("Connect");
        statusLabel = new JLabel("Not connected");
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(connectButton);
        topPanel.add(statusLabel);

        JScrollPane scrollPane = new JScrollPane(textPane);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addListeners() {
        /// Integration: REPLACED - Now connects to real server instead of simulating
        connectButton.addActionListener(e -> {
            if (!isConnected) {
                connectToServer();
            } else {
                disconnectFromServer();
            }
        });

        AbstractDocument doc = (AbstractDocument) textPane.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (isRemoteUpdate) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    handleInsert(offset, string);
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length)
                    throws BadLocationException {
                if (isRemoteUpdate) {
                    super.remove(fb, offset, length);
                } else {
                    handleRemove(offset, length);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {

                if (isRemoteUpdate) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }

                String currentText = document.getText();

                // Case 1: pure typing at end
                if (length == 0 && text != null && !text.isEmpty()) {
                    if (offset == currentText.length()) {
                        handleSimpleInsert(text);
                    } else {
                        JOptionPane.showMessageDialog(
                                EditorUI.this,
                                "For now, only typing at the end is supported.",
                                "Insert Not Supported Yet",
                                JOptionPane.WARNING_MESSAGE);
                        refreshTextFromDocument();
                    }
                    return;
                }

                // Case 2: pure delete from end
                if (length > 0 && (text == null || text.isEmpty())) {
                    if (offset + length == currentText.length()) {
                        handleSimpleDelete(length);
                    } else {
                        JOptionPane.showMessageDialog(
                                EditorUI.this,
                                "For now, only deleting from the end is supported.",
                                "Delete Not Supported Yet",
                                JOptionPane.WARNING_MESSAGE);
                        refreshTextFromDocument();
                    }
                    return;
                }

                // Case 3: replace selected text
                JOptionPane.showMessageDialog(
                        EditorUI.this,
                        "For now, replace/edit-in-middle is not supported.",
                        "Edit Not Supported Yet",
                        JOptionPane.WARNING_MESSAGE);
                refreshTextFromDocument();
            }
        });
    }

    private void handleInsert(int offset, String string) {
        if (isRemoteUpdate || string == null || string.isEmpty()) {
            return;
        }

        String currentText = document.getText();

        // Temporary limitation: only append at end
        if (offset != currentText.length()) {
            JOptionPane.showMessageDialog(
                    this,
                    "For now, only typing at the end is supported.",
                    "Insert Not Supported Yet",
                    JOptionPane.WARNING_MESSAGE);
            refreshTextFromDocument();
            return;
        }

        handleSimpleInsert(string);
    }

    private void handleRemove(int offset, int length) {
        if (isRemoteUpdate || length <= 0) {
            return;
        }

        String currentText = document.getText();

        // Temporary limitation: only delete from end
        if (offset + length != currentText.length()) {
            JOptionPane.showMessageDialog(
                    this,
                    "For now, only deleting from the end is supported.",
                    "Delete Not Supported Yet",
                    JOptionPane.WARNING_MESSAGE);
            refreshTextFromDocument();
            return;
        }

        handleSimpleDelete(length);
    }

    private void handleSimpleInsert(String text) {
        for (int i = 0; i < text.length(); i++) {
            char value = text.charAt(i);

            String parentId = insertedCharIds.isEmpty() ? null : insertedCharIds.get(insertedCharIds.size() - 1);

            InsertCharacterOperation op = new InsertCharacterOperation(localUserId, localClock, value, parentId,
                    blockId);

            document.apply(op);

            insertedCharIds.add(op.getCharId());
            localClock++;

            /// Integration: REPLACED - Now sends real message instead of simulateSend
            if (wsClient != null && wsClient.isConnected()) {
                String message = MessageHandler.operationToMessage(op);
                wsClient.sendMessage(message);
                System.out.println("Sent: " + message);
            }
        }
        System.out.println("Document text after insert = [" + document.getText() + "]");
        refreshTextFromDocument();
    }

    private void handleSimpleDelete(int length) {
        for (int i = 0; i < length; i++) {
            if (insertedCharIds.isEmpty()) {
                break;
            }

            String lastCharId = insertedCharIds.remove(insertedCharIds.size() - 1);

            String[] parts = lastCharId.split("-");
            int userId = Integer.parseInt(parts[0]);
            int clock = Integer.parseInt(parts[1]);

            DeleteCharacterOperation op = new DeleteCharacterOperation(userId, clock, blockId);

            document.apply(op);

            /// Integration: REPLACED - Now sends real message instead of simulateSend
            if (wsClient != null && wsClient.isConnected()) {
                String message = MessageHandler.operationToMessage(op);
                wsClient.sendMessage(message);
                System.out.println("Sent: " + message);
            }
        }
        System.out.println("Document text after delete = [" + document.getText() + "]");
        refreshTextFromDocument();
    }

    private void refreshTextFromDocument() {
        isRemoteUpdate = true;
        textPane.setText(document.getText());
        isRemoteUpdate = false;
    }

    /// Integration: REMOVED simulateSend method - no longer needed

    /// Integration: ADDED - Connect to real server
    private void connectToServer() {
        wsClient = new WebSocketClient(HOST, PORT, new WebSocketClient.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                SwingUtilities.invokeLater(() -> {
                    handleRemoteMessage(message);
                });
            }

            @Override
            public void onConnected() {
                SwingUtilities.invokeLater(() -> {
                    isConnected = true;
                    setStatus("Connected to " + HOST + ":" + PORT);
                    connectButton.setText("Disconnect");
                    textPane.setEditable(true);
                });
            }

            @Override
            public void onDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    isConnected = false;
                    setStatus("Disconnected");
                    connectButton.setText("Connect");
                    textPane.setEditable(false);
                });
            }
        });
        wsClient.connect();
    }

    /// Integration: ADDED - Disconnect from server
    private void disconnectFromServer() {
        if (wsClient != null) {
            wsClient.disconnect();
        }
    }

    /// Integration: ADDED - Handle incoming remote messages
    private void handleRemoteMessage(String message) {
        System.out.println(">>> Remote message: " + message);
        Object operation = MessageHandler.messageToOperation(message);
        if (operation != null) {
            document.applyRemote(operation);
            refreshTextFromDocument();
        }
    }

    public void updateTextFromRemote(String text) {
        isRemoteUpdate = true;
        textPane.setText(text);
        isRemoteUpdate = false;
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

    public JTextPane getTextPane() {
        return textPane;
    }

    public JButton getConnectButton() {
        return connectButton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EditorUI::new);
    }
}
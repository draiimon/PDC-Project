import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MoodSyncApp {
    private JFrame frame;
    private JPanel chatPanel;
    private JTextField userInputField;
    private DefaultListModel<String> historyListModel;
    private JList<String> historyList;
    private JTextArea conversationContext;
    private String currentTitle;
    private ArrayList<String> chatHistory;
    private Map<String, ArrayList<String>> historyMap;
    private ImageIcon userIcon;
    private ImageIcon botIcon;
    private final String WELCOME_MESSAGE = "Hello! I'm MoodSync, your emotional assistant. How are you feeling today?";

    public MoodSyncApp() {
        FlatDarkLaf.install();
        chatHistory = new ArrayList<>();
        historyMap = new HashMap<>();
        historyListModel = new DefaultListModel<>();
        conversationContext = new JTextArea();
        userIcon = new ImageIcon("C:\\Users\\andre\\MoodSyncApp\\andrei.jpg");
        botIcon = new ImageIcon("path/to/bot_icon.jpg");
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("MoodSync App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mood Tracker", new JPanel());
        tabbedPane.addTab("Chatbot", createChatbotPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);

        startNewConversation(); // Start a new conversation initially when the app starts
    }

    private JPanel createChatbotPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.DARK_GRAY);

        JScrollPane chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.DARK_GRAY);
        userInputField = new JTextField();
        userInputField.setFont(new Font("Arial", Font.PLAIN, 16));
        userInputField.setForeground(Color.WHITE);
        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.PLAIN, 16));
        sendButton.addActionListener(new SendButtonListener());

        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        historyList = new JList<>(historyListModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                saveCurrentConversation();
                loadSelectedConversation();
            }
        });
        historyList.setCellRenderer(new CustomHistoryListRenderer());
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("History"));
        historyScrollPane.setPreferredSize(new Dimension(250, 0));
        mainPanel.add(historyScrollPane, BorderLayout.WEST);

        JButton newChatButton = new JButton("New Chat");
        newChatButton.setFont(new Font("Arial", Font.PLAIN, 16));
        newChatButton.addActionListener(e -> {
            saveCurrentConversation();
            startNewConversation();
        });

        JPanel newChatPanel = new JPanel(new BorderLayout());
        newChatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        newChatPanel.add(newChatButton, BorderLayout.CENTER);

        mainPanel.add(newChatPanel, BorderLayout.NORTH);

        addContextMenuToHistoryList();

        return mainPanel;
    }

    private void startNewConversation() {
        chatHistory.clear();
        currentTitle = null;
        chatPanel.removeAll();
        conversationContext.setText("");
        displayMessage("MoodSync Bot", WELCOME_MESSAGE, false);
        chatHistory.add("MoodSync Bot: " + WELCOME_MESSAGE);
    }

    private void loadSelectedConversation() {
        if (!historyList.isSelectionEmpty()) {
            String selectedTitle = historyList.getSelectedValue();
            ArrayList<String> selectedHistory = historyMap.get(selectedTitle);
            chatPanel.removeAll();
            chatHistory.clear();
            conversationContext.setText("");
            if (selectedHistory != null) {
                for (String message : selectedHistory) {
                    boolean isUser = message.startsWith("You: ");
                    displayMessage(isUser ? "You" : "MoodSync Bot", message.substring(isUser ? 4 : 13), isUser);
                    conversationContext.append(message + "\n");
                }
            }
            chatPanel.revalidate();
            chatPanel.repaint();
        }
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userMessage = userInputField.getText().trim();
            if (!userMessage.isEmpty()) {
                if (currentTitle == null) {
                    currentTitle = generateTitleFromMessage(userMessage);
                    historyListModel.addElement(currentTitle);
                }
                displayMessage("You", userMessage, true);
                chatHistory.add("You: " + userMessage);
                userInputField.setText("");
                String conversationContextText = conversationContext.getText() + " " + userMessage;
                // Get response from AI API
                String botResponse = APIClient.getChatbotResponse(conversationContextText);
                displayMessage("MoodSync Bot", botResponse, false);
                chatHistory.add("MoodSync Bot: " + botResponse);
                conversationContext.append("You: " + userMessage + "\nMoodSync Bot: " + botResponse + "\n");
                JScrollBar vertical = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        }
    }

    private void displayMessage(String sender, String message, boolean isUser) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        JLabel messageLabel = new JLabel("<html><div style='width: auto; max-width: 250px;'><b style='color:darkblue;'>"
                + sender + "</b><br><small>" + timestamp + "</small><br>"
                + message.replaceAll("(\r\n|\n)", "<br>") + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setForeground(Color.WHITE);

        JPanel bubblePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int width = getWidth();
                int height = getHeight();
                Color color1 = isUser ? new Color(0, 153, 255) : new Color(204, 204, 204);
                Color color2 = Color.DARK_GRAY;
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, width - 1, height - 1, 30, 30);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                dim.width = Math.min(dim.width, 250);
                return dim;
            }
        };
        bubblePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bubblePanel.add(messageLabel, BorderLayout.CENTER);

        JPanel alignedMessagePanel = new JPanel(new BorderLayout());
        alignedMessagePanel.setOpaque(false);

        JPanel iconAndBubblePanel = new JPanel(new BorderLayout());

        JLabel userPicture = new JLabel("", JLabel.CENTER);
        userPicture.setIcon(new ImageIcon(getScaledRoundImage(isUser ? userIcon.getImage() : botIcon.getImage(), 40)));

        if (isUser) {
            iconAndBubblePanel.add(userPicture, BorderLayout.WEST);
            iconAndBubblePanel.add(bubblePanel, BorderLayout.CENTER);
            alignedMessagePanel.add(iconAndBubblePanel, BorderLayout.WEST);
        } else {
            iconAndBubblePanel.add(bubblePanel, BorderLayout.CENTER);
            iconAndBubblePanel.add(userPicture, BorderLayout.EAST);
            alignedMessagePanel.add(iconAndBubblePanel, BorderLayout.EAST);
        }

        chatPanel.add(alignedMessagePanel);
        chatPanel.add(Box.createVerticalStrut(5));
        chatPanel.revalidate();
        chatPanel.repaint();

        JScrollBar vertical = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private Image getScaledRoundImage(Image srcImg, int size) {
        BufferedImage resizedImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Double(0, 0, size, size, size, size));
        g2.drawImage(srcImg, 0, 0, size, size, null);
        g2.dispose();
        return resizedImg;
    }

    private void saveCurrentConversation() {
        if (!chatHistory.isEmpty()) {
            if (currentTitle == null) {
                currentTitle = generateTitleFromMessage(chatHistory.get(1)); // Use the first user message for title
            }
            historyMap.put(currentTitle, new ArrayList<>(chatHistory));
            if (!historyListModel.contains(currentTitle)) {
                historyListModel.addElement(currentTitle);
            }
        }
    }

    private String generateTitleFromMessage(String message) {
        StringTokenizer tokenizer = new StringTokenizer(message, " ");
        StringBuilder titleBuilder = new StringBuilder();
        int wordCount = 0;

        while (tokenizer.hasMoreTokens() && wordCount < 5) {
            titleBuilder.append(tokenizer.nextToken()).append(" ");
            wordCount++;
        }

        return titleBuilder.toString().trim();
    }

    private class CustomHistoryListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(10, 10, 10, 10));
            label.setFont(new Font("Arial", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            label.setBackground(isSelected ? Color.GRAY : Color.DARK_GRAY);

            return label;
        }
    }

    private void addContextMenuToHistoryList() {
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem renameItem = new JMenuItem("Rename");
        JMenuItem deleteItem = new JMenuItem("Delete");

        renameItem.addActionListener(e -> renameSelectedConversation());
        deleteItem.addActionListener(e -> deleteSelectedConversation());

        contextMenu.add(renameItem);
        contextMenu.add(deleteItem);

        historyList.setComponentPopupMenu(contextMenu);
    }

    private void renameSelectedConversation() {
        String selectedTitle = historyList.getSelectedValue();
        if (selectedTitle != null) {
            String newTitle = JOptionPane.showInputDialog(frame, "Enter new title:", selectedTitle);
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                ArrayList<String> conversation = historyMap.remove(selectedTitle);
                historyMap.put(newTitle, conversation);
                historyListModel.setElementAt(newTitle, historyList.getSelectedIndex());
            }
        }
    }

    private void deleteSelectedConversation() {
        String selectedTitle = historyList.getSelectedValue();
        if (selectedTitle != null) {
            int response = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this conversation?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                historyMap.remove(selectedTitle);
                historyListModel.removeElement(selectedTitle);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MoodSyncApp::new);
    }
}

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executorService;

    public MoodSyncApp() {
        FlatDarkLaf.install();
        chatHistory = new ArrayList<>();
        historyMap = new HashMap<>();
        historyListModel = new DefaultListModel<>();
        conversationContext = new JTextArea();
        userIcon = new ImageIcon("C:\\Users\\andre\\MoodSyncApp\\andrei.jpg");
        botIcon = new ImageIcon("C:\\Users\\andre\\MoodSyncApp\\andrei.jpg");
        executorService = Executors.newFixedThreadPool(2); // Using 2 threads for simplicity
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("MoodSync App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800); // Increased the size of the GUI
        frame.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Mood Tracker", new JPanel());
        tabbedPane.addTab("Chatbot", createChatbotPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setVisible(true);
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
        historyList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedConversation();
                }
            }
        });
        JScrollPane historyScrollPane = new JScrollPane(historyList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("History"));
        historyScrollPane.setPreferredSize(new Dimension(250, 0));
        mainPanel.add(historyScrollPane, BorderLayout.WEST);

        JButton newChatButton = new JButton("New Chat");
        newChatButton.setFont(new Font("Arial", Font.PLAIN, 16));
        newChatButton.addActionListener(e -> autoSaveCurrentConversation());

        JPanel newChatPanel = new JPanel(new BorderLayout());
        newChatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        newChatPanel.add(newChatButton, BorderLayout.CENTER);

        mainPanel.add(newChatPanel, BorderLayout.NORTH);

        return mainPanel;
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
                displayMessage("You", userMessage, true);
                chatHistory.add("You: " + userMessage);
                userInputField.setText("");
                String conversationContextText = conversationContext.getText() + " " + userMessage;
                executorService.submit(() -> {
                    String botResponse = APIClient.getChatbotResponse(conversationContextText);
                    SwingUtilities.invokeLater(() -> {
                        displayMessage("MoodSync Bot", botResponse, false);
                        chatHistory.add("MoodSync Bot: " + botResponse);
                        conversationContext.append("You: " + userMessage + "\nMoodSync Bot: " + botResponse + "\n");
                        JScrollBar vertical = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
                        vertical.setValue(vertical.getMaximum());
                    });
                });
            }
        }
    }

    private void displayMessage(String sender, String message, boolean isUser) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
    
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        JLabel messageLabel = new JLabel("<html><div style='width: auto; max-width: 250px;'><b>" + sender + "</b><br><small>" + timestamp + "</small><br>" + message + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
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
    
        // Create a panel for the icon and bubble
        JPanel iconAndBubblePanel = new JPanel(new BorderLayout());
    
        JLabel userPicture = new JLabel("", JLabel.CENTER);
        userPicture.setIcon(new ImageIcon(getScaledRoundImage(isUser ? userIcon.getImage() : botIcon.getImage(), 40)));
    
        if (isUser) {
            // User message alignment: icon on the left, bubble on the right
            iconAndBubblePanel.add(userPicture, BorderLayout.WEST);
            iconAndBubblePanel.add(bubblePanel, BorderLayout.CENTER);
            alignedMessagePanel.add(iconAndBubblePanel, BorderLayout.WEST); // Align to the left edge of chat panel
        } else {
            // Bot message alignment: bubble on the left, icon on the right
            iconAndBubblePanel.add(bubblePanel, BorderLayout.CENTER);
            iconAndBubblePanel.add(userPicture, BorderLayout.EAST);
            alignedMessagePanel.add(iconAndBubblePanel, BorderLayout.EAST); // Align to the right edge of chat panel
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

    private void autoSaveCurrentConversation() {
        if (!chatHistory.isEmpty()) {
            String title = currentTitle != null ? currentTitle : generateTitle(chatHistory.get(0));
            historyMap.put(title, new ArrayList<>(chatHistory));
            if (!historyListModel.contains(title)) {
                historyListModel.addElement(title);
            }
            chatHistory.clear();
            chatPanel.removeAll();
            chatPanel.revalidate();
            chatPanel.repaint();
            conversationContext.setText("");
            currentTitle = null;
        }
    }

    private String generateTitle(String initialMessage) {
        // Improved title generation using keyword extraction
        String[] words = initialMessage.split(" ");
        StringBuilder titleBuilder = new StringBuilder("Chat: ");
        for (int i = 0; i < Math.min(words.length, 5); i++) {
            titleBuilder.append(words[i]).append(" ");
        }
        return titleBuilder.toString().trim() + "...";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MoodSyncApp::new);
    }
}

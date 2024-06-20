import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class LoginRegistrationFrame {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginRegistrationFrame() {
        initializeUI();
    }

    private void initializeUI() {
        // Use FlatLaf for modern look and feel
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame("MoodSync Login/Register");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400); // Adjusted width
        frame.setLocationRelativeTo(null); // Center the frame on screen
        frame.setLayout(new BorderLayout());
        frame.setResizable(false); // Fixed size

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.decode("#191B2C"));

        JLabel titleLabel = new JLabel("MoodSync Bot", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.decode("#191B2C"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel usernameLabel = new JLabel("Username:", SwingConstants.LEFT);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        usernameLabel.setForeground(Color.WHITE);
        usernameField = new RoundedTextField("Enter Username");
        addPlaceholderStyle(usernameField);
        centerPanel.add(usernameLabel, gbc);
        centerPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:", SwingConstants.LEFT);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(Color.WHITE);
        passwordField = new RoundedPasswordField("Enter Password");
        addPlaceholderStyle(passwordField);
        centerPanel.add(passwordLabel, gbc);
        centerPanel.add(passwordField, gbc);

        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setForeground(Color.WHITE);
        showPassword.setBackground(Color.decode("#191B2C"));
        showPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPassword.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('•');
                }
            }
        });

        centerPanel.add(showPassword, gbc);

        JButton loginButton = new RoundedButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add your login logic here
                String username = usernameField.getText().trim();
                char[] password = passwordField.getPassword();
                // Example validation (replace with your actual validation)
                if (username.equals("admin") && new String(password).equals("password")) {
                    JOptionPane.showMessageDialog(frame, "Login Successful");
                    // Proceed with your application logic after login
                    frame.dispose(); // Close the login frame
                    // Example: new MoodSyncApp();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password");
                }
            }
        });

        JButton registerButton = new RoundedButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add your registration logic here
                String username = usernameField.getText().trim();
                char[] password = passwordField.getPassword();
                // Example registration (replace with your actual registration)
                JOptionPane.showMessageDialog(frame, "Registration functionality not implemented yet");
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.decode("#191B2C"));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        centerPanel.add(buttonPanel, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Load and display the GIF image
        try {
            File imgFile = new File("C:\\Users\\andre\\MoodSyncApp\\moodsynclogo.gif"); // Change to your image path
            if (imgFile.exists()) {
                ImageIcon imageIcon = new ImageIcon(imgFile.getAbsolutePath());
                JLabel imageLabel = new JLabel(imageIcon);
                JPanel imagePanel = new JPanel(new BorderLayout());
                imagePanel.setBackground(Color.decode("#191B2C"));
                imagePanel.add(imageLabel, BorderLayout.CENTER);
                mainPanel.add(imagePanel, BorderLayout.WEST);
            } else {
                System.err.println("Image file not found: " + imgFile.getPath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void addPlaceholderStyle(JTextField textField) {
        textField.setForeground(Color.WHITE); // White text color for placeholders
        textField.setFont(textField.getFont().deriveFont(Font.BOLD)); // Bold font
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals("Enter Username") || textField.getText().equals("Enter Password")) {
                    textField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    if (textField instanceof JPasswordField) {
                        textField.setText("Enter Password");
                    } else {
                        textField.setText("Enter Username");
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginRegistrationFrame::new);
    }
}

class RoundedTextField extends JTextField {
    private Shape shape;

    public RoundedTextField(String placeholder) {
        super(placeholder);
        setOpaque(false);
        setForeground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        setFont(new Font("Arial", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, Color.DARK_GRAY, getWidth(), getHeight(), Color.LIGHT_GRAY);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        }
        return shape.contains(x, y);
    }
}

class RoundedPasswordField extends JPasswordField {
    private Shape shape;

    public RoundedPasswordField(String placeholder) {
        super(placeholder);
        setOpaque(false);
        setForeground(Color.GRAY);
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        setFont(new Font("Arial", Font.PLAIN, 14));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, Color.DARK_GRAY, getWidth(), getHeight(), Color.LIGHT_GRAY);
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        super.paintComponent(g2);
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.GRAY);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
        }
        return shape.contains(x, y);
    }
}

class RoundedButton extends JButton {
    public RoundedButton(String text) {
        super(text);
        setUI(new BasicButtonUI()); // Use basic button UI to customize painting
        setContentAreaFilled(false); // No default background
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setFont(new Font("Arial", Font.BOLD, 14));

        // Add hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setForeground(Color.WHITE); // Text color on hover
                setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Reset border
                setBackground(Color.decode("#31364D")); // Darker color on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(Color.WHITE); // Text color on exit
                setBackground(null); // Reset to default color
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isPressed()) {
            g.setColor(Color.decode("#262A3F")); // Darker color when pressed
        } else {
            g.setColor(getBackground());
        }
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(Color.GRAY);
        g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
    }
}

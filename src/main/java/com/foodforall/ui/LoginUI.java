package com.foodforall.ui;

import com.foodforall.service.AuthenticationService;
import com.foodforall.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginUI extends JFrame implements ActionListener {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;
    
    public LoginUI() {
        setTitle("Login - " + Constants.APP_NAME);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initComponents();
        layoutComponents();
        
        setVisible(true);
    }
    
    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Sign In");
        loginButton.setActionCommand(Constants.CMD_LOGIN);
        loginButton.addActionListener(this);
        
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        
        // Add key listeners to fields
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        };
        
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
    }
    
    private void layoutComponents() {
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Logo panel
        JPanel logoPanel = new JPanel();
        JLabel logoLabel = new JLabel(Constants.APP_NAME);
        logoLabel.setFont(Constants.TITLE_FONT);
        logoPanel.add(logoLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Email label
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Email:");
        formPanel.add(usernameLabel, gbc);
        
        // Email field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);
        
        // Password label
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);
        
        // Password field
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        
        // Forgot password panel
        JPanel forgotPanel = new JPanel();
        JLabel forgotLabel = new JLabel("Forgot password?");
        forgotLabel.setForeground(Constants.PRIMARY_COLOR);
        forgotPanel.add(forgotLabel);
        
        // Message panel
        JPanel messagePanel = new JPanel();
        messagePanel.add(messageLabel);
        
        // Add components to main panel
        mainPanel.add(logoPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.add(buttonPanel, BorderLayout.NORTH);
        footerPanel.add(forgotPanel, BorderLayout.CENTER);
        footerPanel.add(messagePanel, BorderLayout.SOUTH);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
    }
    
    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }
        
        boolean success = AuthenticationService.getInstance().login(username, password);
        
        if (success) {
            dispose();
            new DashboardUI();
        } else {
            messageLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (Constants.CMD_LOGIN.equals(e.getActionCommand())) {
            attemptLogin();
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}

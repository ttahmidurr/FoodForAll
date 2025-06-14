package com.foodforall.util;

import java.awt.*;

public class Constants {
    
    // Application Info
    public static final String APP_NAME = "FoodForAll Till System";
    public static final String APP_VERSION = "1.0.0";
    public static final String COPYRIGHT = "© 2025 FoodForAll Ltd";
    
    // UI Constants
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    public static final int DASHBOARD_WIDTH = 950;
    public static final int DASHBOARD_HEIGHT = 700;
    public static final int FORM_WIDTH = 600;
    public static final int FORM_HEIGHT = 500;
    public static final int DIALOG_WIDTH = 400;
    public static final int DIALOG_HEIGHT = 300;
    public static final int BUTTON_WIDTH = 100;
    public static final int BUTTON_HEIGHT = 30;
    public static final int MARGIN = 10;
    public static final int PADDING = 5;
    
    // Colors
    public static final Color PRIMARY_COLOR = new Color(0, 123, 255);
    public static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color INFO_COLOR = new Color(23, 162, 184);
    public static final Color LIGHT_COLOR = new Color(248, 249, 250);
    public static final Color DARK_COLOR = new Color(52, 58, 64);
    
    // Fonts
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    public static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font REGULAR_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 12);
    
    // Action Commands
    public static final String CMD_LOGIN = "login";
    public static final String CMD_LOGOUT = "logout";
    public static final String CMD_ADD = "add";
    public static final String CMD_EDIT = "edit";
    public static final String CMD_DELETE = "delete";
    public static final String CMD_SAVE = "save";
    public static final String CMD_CANCEL = "cancel";
    public static final String CMD_SEARCH = "search";
    public static final String CMD_REFRESH = "refresh";
    public static final String CMD_GENERATE = "generate";
    public static final String CMD_CHECKOUT = "checkout";
    
    // Entities
    public static final String ENTITY_USER = "USER";
    public static final String ENTITY_CUSTOMER = "CUSTOMER";
    public static final String ENTITY_PRODUCT = "PRODUCT";
    public static final String ENTITY_STOCK = "STOCK";
    public static final String ENTITY_SALE = "SALE";
    public static final String ENTITY_REPORT = "REPORT";
    
    // Actions
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_READ = "READ";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_PROCESS = "PROCESS";
    public static final String ACTION_GENERATE = "GENERATE";
    
    // Error Messages
    public static final String ERR_DB_CONNECTION = "Failed to connect to the database";
    public static final String ERR_AUTHENTICATION = "Invalid username or password";
    public static final String ERR_ACCESS_DENIED = "Access denied. You do not have permission to perform this action";
    public static final String ERR_INVALID_INPUT = "Invalid input. Please check your entries";
    public static final String ERR_OPERATION_FAILED = "Operation failed. Please try again";
    
    // Success Messages
    public static final String MSG_LOGIN_SUCCESS = "Login successful";
    public static final String MSG_LOGOUT_SUCCESS = "Logout successful";
    public static final String MSG_SAVE_SUCCESS = "Data saved successfully";
    public static final String MSG_DELETE_SUCCESS = "Data deleted successfully";
    public static final String MSG_OPERATION_SUCCESS = "Operation completed successfully";
}
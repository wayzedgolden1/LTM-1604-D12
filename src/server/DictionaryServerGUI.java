package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Date;
import javax.swing.Timer;

public class DictionaryServerGUI extends JFrame {
    private static final int PORT = 5000;

    // Models
    private DefaultTableModel dictModel;
    private DefaultTableModel historyModel;

    // UI Components
    private JTextField txtSearch;
    private JLabel statusLabel;

    // Màu sắc hiện đại
    private final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);

    public DictionaryServerGUI() {
        setTitle("Dictionary Server - Management Console");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
        
        // Start socket server
        new Thread(this::startSocketServer).start();
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        setContentPane(mainPanel);

        // ====== HEADER ======
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ====== CENTER - TABS ======
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Tab Dictionary
        JPanel dictPanel = createDictionaryPanel();
        tabs.addTab("Quản Lý Từ Điển", dictPanel);
        
        // Tab History
        JPanel histPanel = createHistoryPanel();
        tabs.addTab("Lịch Sử Tra Cứu", histPanel);
        
        mainPanel.add(tabs, BorderLayout.CENTER);

        // ====== STATUS BAR ======
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusPanel.setBackground(Color.WHITE);
        
        statusLabel = new JLabel("Server đang chạy trên port " + PORT);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel(new Date().toString());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(timeLabel, BorderLayout.EAST);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Load initial data
        loadDictionary();
        loadHistory();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Title
        JLabel titleLabel = new JLabel("DICTIONARY SERVER MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);

        JLabel dictCountLabel = new JLabel("0 từ");
        JLabel historyCountLabel = new JLabel("0 lượt tra");
        
        for (JLabel label : new JLabel[]{dictCountLabel, historyCountLabel}) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            statsPanel.add(label);
        }

        header.add(titleLabel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        // Update stats periodically
        new Timer(5000, e -> updateStats(dictCountLabel, historyCountLabel)).start();

        return header;
    }

    private JPanel createDictionaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JButton btnSearch = createStyledButton("Tìm", SECONDARY_COLOR);
        JButton btnClearSearch = createStyledButton("Xóa", new Color(149, 165, 166));

        JPanel searchButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchButtons.setBackground(BACKGROUND_COLOR);
        searchButtons.add(btnSearch);
        searchButtons.add(btnClearSearch);

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(searchButtons, BorderLayout.EAST);

        // Table
        dictModel = new DefaultTableModel(new String[]{"English", "Vietnamese", "Example"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable dictTable = new JTable(dictModel);
        styleTable(dictTable);
        
        JScrollPane tableScroll = new JScrollPane(dictTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        controlPanel.setBackground(BACKGROUND_COLOR);
        
        JButton btnRefresh = createStyledButton("Làm Mới", SECONDARY_COLOR);
        JButton btnAdd = createStyledButton("Thêm Từ", SUCCESS_COLOR);
        JButton btnEdit = createStyledButton("Sửa Từ", new Color(230, 126, 34));
        JButton btnDelete = createStyledButton("Xóa Từ", WARNING_COLOR);
        JButton btnExport = createStyledButton("Xuất File", new Color(155, 89, 182));

        controlPanel.add(btnRefresh);
        controlPanel.add(btnAdd);
        controlPanel.add(btnEdit);
        controlPanel.add(btnDelete);
        controlPanel.add(btnExport);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        // Event handlers
        btnRefresh.addActionListener(e -> loadDictionary());
        btnAdd.addActionListener(e -> addWordDialog());
        btnEdit.addActionListener(e -> editWordDialog(dictTable));
        btnDelete.addActionListener(e -> deleteWordDialog(dictTable));
        btnSearch.addActionListener(e -> searchDictionary(txtSearch.getText().trim()));
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadDictionary();
        });
        btnExport.addActionListener(e -> exportDictionary());

        // Double click to edit
        dictTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editWordDialog(dictTable);
                }
            }
        });

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);

        // History table
        historyModel = new DefaultTableModel(new String[]{"ID", "Từ Tra Cứu", "Địa Chỉ Client", "Thời Gian"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable histTable = new JTable(historyModel);
        styleTable(histTable);
        
        JScrollPane tableScroll = new JScrollPane(histTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        controlPanel.setBackground(BACKGROUND_COLOR);
        
        JButton btnRefresh = createStyledButton("Làm Mới", SECONDARY_COLOR);
        JButton btnClear = createStyledButton("Xóa Lịch Sử", WARNING_COLOR);
        JButton btnExport = createStyledButton("Xuất CSV", new Color(155, 89, 182));

        controlPanel.add(btnRefresh);
        controlPanel.add(btnClear);
        controlPanel.add(btnExport);

        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        // Event handlers
        btnRefresh.addActionListener(e -> loadHistory());
        btnClear.addActionListener(e -> clearHistoryDialog());
        btnExport.addActionListener(e -> exportHistory());

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void styleTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setIntercellSpacing(new Dimension(1, 1));
    }

    // ====== DATABASE OPERATIONS ======

    private void loadDictionary() {
        SwingUtilities.invokeLater(() -> {
            dictModel.setRowCount(0);
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement("SELECT English, Vietnamese, Example FROM EV_Dictionary ORDER BY English");
                 ResultSet rs = st.executeQuery()) {
                
                while (rs.next()) {
                    dictModel.addRow(new Object[]{
                        rs.getString("English"),
                        rs.getString("Vietnamese"), 
                        rs.getString("Example")
                    });
                }
            } catch (SQLException e) {
                showError("Lỗi tải từ điển: " + e.getMessage());
            }
        });
    }

    private void searchDictionary(String keyword) {
        if (keyword.isEmpty()) {
            loadDictionary();
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            dictModel.setRowCount(0);
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "SELECT English, Vietnamese, Example FROM EV_Dictionary " +
                     "WHERE English LIKE ? OR Vietnamese LIKE ? ORDER BY English")) {
                
                st.setString(1, "%" + keyword + "%");
                st.setString(2, "%" + keyword + "%");
                
                try (ResultSet rs = st.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        dictModel.addRow(new Object[]{
                            rs.getString("English"),
                            rs.getString("Vietnamese"),
                            rs.getString("Example")
                        });
                    }
                    if (!found) {
                        showInfo("Không tìm thấy từ khóa: " + keyword);
                    }
                }
            } catch (SQLException e) {
                showError("Lỗi tìm kiếm: " + e.getMessage());
            }
        });
    }

    private void loadHistory() {
        SwingUtilities.invokeLater(() -> {
            historyModel.setRowCount(0);
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "SELECT Id, QueryWord, ClientAddr, QueryTime FROM History ORDER BY QueryTime DESC");
                 ResultSet rs = st.executeQuery()) {
                
                while (rs.next()) {
                    historyModel.addRow(new Object[]{
                        rs.getInt("Id"),
                        rs.getString("QueryWord"),
                        rs.getString("ClientAddr"),
                        rs.getTimestamp("QueryTime")
                    });
                }
            } catch (SQLException e) {
                showError("Lỗi tải lịch sử: " + e.getMessage());
            }
        });
    }

    // ====== CRUD OPERATIONS ======

    private void addWordDialog() {
        JTextField txtEnglish = new JTextField(25);
        JTextField txtVietnamese = new JTextField(25);
        JTextField txtExample = new JTextField(25);
        
        JPanel panel = createInputPanel("Thêm Từ Mới", 
            new String[]{"Từ tiếng Anh:", "Nghĩa tiếng Việt:", "Ví dụ:"},
            new JTextField[]{txtEnglish, txtVietnamese, txtExample});

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm Từ Mới", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String english = txtEnglish.getText().trim();
            String vietnamese = txtVietnamese.getText().trim();
            String example = txtExample.getText().trim();

            if (english.isEmpty() || vietnamese.isEmpty()) {
                showError("Từ tiếng Anh và nghĩa tiếng Việt không được để trống!");
                return;
            }

            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "INSERT INTO EV_Dictionary (English, Vietnamese, Example) VALUES (?, ?, ?)")) {
                
                st.setString(1, english);
                st.setString(2, vietnamese);
                st.setString(3, example);
                st.executeUpdate();
                
                showSuccess("Thêm từ thành công!");
                loadDictionary();
                
            } catch (SQLException e) {
                showError("Lỗi thêm từ: " + e.getMessage());
            }
        }
    }

    private void editWordDialog(JTable dictTable) {
        int row = dictTable.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một từ để sửa!");
            return;
        }

        String oldEnglish = (String) dictModel.getValueAt(row, 0);
        String oldVietnamese = (String) dictModel.getValueAt(row, 1);
        String oldExample = (String) dictModel.getValueAt(row, 2);

        JTextField txtEnglish = new JTextField(oldEnglish, 25);
        JTextField txtVietnamese = new JTextField(oldVietnamese, 25);
        JTextField txtExample = new JTextField(oldExample, 25);
        
        JPanel panel = createInputPanel("Sửa Từ",
            new String[]{"Từ tiếng Anh:", "Nghĩa tiếng Việt:", "Ví dụ:"},
            new JTextField[]{txtEnglish, txtVietnamese, txtExample});

        int result = JOptionPane.showConfirmDialog(this, panel, "Sửa Từ", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String newEnglish = txtEnglish.getText().trim();
            String newVietnamese = txtVietnamese.getText().trim();
            String newExample = txtExample.getText().trim();

            if (newEnglish.isEmpty() || newVietnamese.isEmpty()) {
                showError("Từ tiếng Anh và nghĩa tiếng Việt không được để trống!");
                return;
            }

            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "UPDATE EV_Dictionary SET English = ?, Vietnamese = ?, Example = ? WHERE English = ?")) {
                
                st.setString(1, newEnglish);
                st.setString(2, newVietnamese);
                st.setString(3, newExample);
                st.setString(4, oldEnglish);
                
                int affected = st.executeUpdate();
                if (affected > 0) {
                    showSuccess("Sửa từ thành công!");
                    loadDictionary();
                } else {
                    showError("Không tìm thấy từ để cập nhật!");
                }
                
            } catch (SQLException e) {
                showError("Lỗi cập nhật: " + e.getMessage());
            }
        }
    }

    private void deleteWordDialog(JTable dictTable) {
        int row = dictTable.getSelectedRow();
        if (row < 0) {
            showError("Vui lòng chọn một từ để xóa!");
            return;
        }

        String english = (String) dictModel.getValueAt(row, 0);
        
        int result = JOptionPane.showConfirmDialog(this,
            "<html><b>Bạn có chắc muốn xóa từ '" + english + "'?</b><br>Hành động này không thể hoàn tác.</html>",
            "Xác Nhận Xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "DELETE FROM EV_Dictionary WHERE English = ?")) {
                
                st.setString(1, english);
                st.executeUpdate();
                
                showSuccess("Xóa từ thành công!");
                loadDictionary();
                
            } catch (SQLException e) {
                showError("Lỗi xóa từ: " + e.getMessage());
            }
        }
    }

    private void clearHistoryDialog() {
        int result = JOptionPane.showConfirmDialog(this,
            "<html><b>Bạn có chắc muốn xóa toàn bộ lịch sử?</b><br>Tất cả dữ liệu lịch sử sẽ bị mất.</html>",
            "Xác Nhận Xóa Lịch Sử",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement("DELETE FROM History")) {
                
                st.executeUpdate();
                showSuccess("Xóa lịch sử thành công!");
                loadHistory();
                
            } catch (SQLException e) {
                showError("Lỗi xóa lịch sử: " + e.getMessage());
            }
        }
    }

    // ====== EXPORT FUNCTIONS ======

    private void exportDictionary() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Xuất từ điển");
        fileChooser.setSelectedFile(new File("dictionary_export.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("English,Vietnamese,Example");
                
                for (int i = 0; i < dictModel.getRowCount(); i++) {
                    String english = (String) dictModel.getValueAt(i, 0);
                    String vietnamese = (String) dictModel.getValueAt(i, 1);
                    String example = (String) dictModel.getValueAt(i, 2);
                    
                    writer.printf("\"%s\",\"%s\",\"%s\"%n", 
                        english.replace("\"", "\"\""), 
                        vietnamese.replace("\"", "\"\""), 
                        example.replace("\"", "\"\""));
                }
                
                showSuccess("Xuất từ điển thành công: " + file.getName());
            } catch (IOException e) {
                showError("Lỗi xuất file: " + e.getMessage());
            }
        }
    }

    private void exportHistory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Xuất lịch sử");
        fileChooser.setSelectedFile(new File("history_export.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,QueryWord,ClientAddr,QueryTime");
                
                for (int i = 0; i < historyModel.getRowCount(); i++) {
                    String id = historyModel.getValueAt(i, 0).toString();
                    String queryWord = (String) historyModel.getValueAt(i, 1);
                    String clientAddr = (String) historyModel.getValueAt(i, 2);
                    String queryTime = historyModel.getValueAt(i, 3).toString();
                    
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n", 
                        id, queryWord, clientAddr, queryTime);
                }
                
                showSuccess("Xuất lịch sử thành công: " + file.getName());
            } catch (IOException e) {
                showError("Lỗi xuất file: " + e.getMessage());
            }
        }
    }

    // ====== SOCKET SERVER ======

    private void startSocketServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            updateStatus("Server đang lắng nghe trên port " + PORT);
            System.out.println("Server listening on port " + PORT);
            
            while (true) {
                Socket client = server.accept();
                new Thread(() -> handleClientSocket(client)).start();
            }
        } catch (IOException e) {
            updateStatus("Lỗi server: " + e.getMessage());
            showError("Lỗi server socket: " + e.getMessage());
        }
    }

    private void handleClientSocket(Socket clientSocket) {
        try (Socket s = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            String line = in.readLine();
            if (line == null) return;

            System.out.println("Received: " + line);

            if (line.startsWith("TRANSLATE:")) {
                handleTranslateRequest(line, out, clientSocket);
            } else if (line.equals("LISTALL")) {
                handleListAllRequest(out);
            } else if (line.startsWith("ADD:")) {
                handleAddRequest(line, out);
            } else if (line.startsWith("EDIT:")) {
                handleEditRequest(line, out);
            } else if (line.startsWith("DELETE:")) {
                handleDeleteRequest(line, out);
            } else if (line.startsWith("GETWORD:")) {
                handleGetWordRequest(line, out);
            } else {
                out.println("Lệnh không hợp lệ!");
            }
        } catch (IOException e) {
            System.out.println("I/O error handling client: " + e.getMessage());
        } finally {
            SwingUtilities.invokeLater(this::loadHistory);
        }
    }

    private void handleTranslateRequest(String line, PrintWriter out, Socket clientSocket) {
        String[] parts = line.split(":", 3);
        if (parts.length < 3) {
            out.println("Sai cú pháp!");
            return;
        }
        
        String mode = parts[1];
        String word = parts[2].trim();

        String sql;
        if (mode.equals("EV")) {
            sql = "SELECT Vietnamese, Example FROM EV_Dictionary WHERE English = ?";
        } else {
            sql = "SELECT English, Example FROM EV_Dictionary WHERE Vietnamese = ?";
        }

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setString(1, word);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String result = mode.equals("EV") ? rs.getString("Vietnamese") : rs.getString("English");
                    String ex = rs.getString("Example");
                    out.println(result != null ? result : "");
                    if (ex != null && !ex.isEmpty()) {
                        out.println("EXAMPLE:" + ex);
                    }
                } else {
                    out.println("Không tìm thấy từ!");
                }
            }
        } catch (SQLException e) {
            out.println("Lỗi truy vấn: " + e.getMessage());
        }

        // Log history
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement st = conn.prepareStatement(
                 "INSERT INTO History (QueryWord, ClientAddr) VALUES (?, ?)")) {
            
            st.setString(1, word);
            st.setString(2, clientSocket.getInetAddress().getHostAddress());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Lỗi ghi history: " + e.getMessage());
        }
    }

    private void handleListAllRequest(PrintWriter out) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement st = conn.prepareStatement(
                 "SELECT English, Vietnamese, Example FROM EV_Dictionary ORDER BY English");
             ResultSet rs = st.executeQuery()) {

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                String eng = rs.getString("English");
                String vie = rs.getString("Vietnamese");
                String ex = rs.getString("Example");
                out.println(eng + "|" + vie + "|" + (ex != null ? ex : ""));
            }
            if (!hasData) {
                out.println("EMPTY");
            }
        } catch (SQLException e) {
            out.println("Lỗi tải LISTALL: " + e.getMessage());
        }
    }

    private void handleAddRequest(String line, PrintWriter out) {
        String[] parts = line.split(":", 4);
        if (parts.length >= 4) {
            String english = parts[1];
            String vietnamese = parts[2];
            String example = parts[3];
            
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "INSERT INTO EV_Dictionary (English, Vietnamese, Example) VALUES (?, ?, ?)")) {
                
                st.setString(1, english);
                st.setString(2, vietnamese);
                st.setString(3, example);
                st.executeUpdate();
                out.println("SUCCESS");
                SwingUtilities.invokeLater(this::loadDictionary);
            } catch (SQLException e) {
                out.println("Lỗi thêm từ: " + e.getMessage());
            }
        }
    }

    private void handleEditRequest(String line, PrintWriter out) {
        String[] parts = line.split(":", 5);
        if (parts.length >= 5) {
            String oldEnglish = parts[1];
            String newEnglish = parts[2];
            String newVietnamese = parts[3];
            String newExample = parts[4];
            
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "UPDATE EV_Dictionary SET English = ?, Vietnamese = ?, Example = ? WHERE English = ?")) {
                
                st.setString(1, newEnglish);
                st.setString(2, newVietnamese);
                st.setString(3, newExample);
                st.setString(4, oldEnglish);
                int affected = st.executeUpdate();
                if (affected > 0) {
                    out.println("SUCCESS");
                    SwingUtilities.invokeLater(this::loadDictionary);
                } else {
                    out.println("Không tìm thấy từ để sửa");
                }
            } catch (SQLException e) {
                out.println("Lỗi sửa từ: " + e.getMessage());
            }
        }
    }

    private void handleDeleteRequest(String line, PrintWriter out) {
        String[] parts = line.split(":", 2);
        if (parts.length >= 2) {
            String english = parts[1];
            
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "DELETE FROM EV_Dictionary WHERE English = ?")) {
                
                st.setString(1, english);
                int affected = st.executeUpdate();
                if (affected > 0) {
                    out.println("SUCCESS");
                    SwingUtilities.invokeLater(this::loadDictionary);
                } else {
                    out.println("Không tìm thấy từ để xóa");
                }
            } catch (SQLException e) {
                out.println("Lỗi xóa từ: " + e.getMessage());
            }
        }
    }

    private void handleGetWordRequest(String line, PrintWriter out) {
        String[] parts = line.split(":", 2);
        if (parts.length >= 2) {
            String word = parts[1];
            
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement(
                     "SELECT English, Vietnamese, Example FROM EV_Dictionary WHERE English = ?")) {
                
                st.setString(1, word);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        String eng = rs.getString("English");
                        String vie = rs.getString("Vietnamese");
                        String ex = rs.getString("Example");
                        out.println(eng + "|" + vie + "|" + (ex != null ? ex : ""));
                    } else {
                        out.println("Không tìm thấy từ");
                    }
                }
            } catch (SQLException e) {
                out.println("Lỗi truy vấn: " + e.getMessage());
            }
        }
    }

    // ====== UTILITY METHODS ======

    private JPanel createInputPanel(String title, String[] labels, JTextField[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            panel.add(label);
            panel.add(fields[i]);
        }
        
        return panel;
    }

    private void updateStats(JLabel dictCount, JLabel historyCount) {
        try (Connection conn = DBConnector.getConnection()) {
            // Dictionary count
            try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM EV_Dictionary");
                 ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    dictCount.setText(rs.getInt(1) + " từ");
                }
            }
            
            // History count
            try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM History");
                 ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    historyCount.setText(rs.getInt(1) + " lượt tra");
                }
            }
        } catch (SQLException e) {
            // Ignore stats errors
        }
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Thành Công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông Tin", JOptionPane.INFORMATION_MESSAGE);
    }

    // ====== MAIN ======
    public static void main(String[] args) {
        try {
            // Sử dụng Nimbus Look and Feel hoặc System
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName()) || "Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                // Fallback to cross-platform look and feel
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            new DictionaryServerGUI();
        });
    }
}
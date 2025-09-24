package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class DictionaryServerGUI extends JFrame {
    private static final int PORT = 5000;

    private DefaultTableModel dictModel;
    private DefaultTableModel historyModel;

    public DictionaryServerGUI() {
        setTitle("Dictionary Server - GUI");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // --- Dictionary tab ---
        JPanel dictPanel = new JPanel(new BorderLayout(8,8));
        dictPanel.setBorder(new EmptyBorder(8,8,8,8));
        dictModel = new DefaultTableModel(new String[]{"English", "Vietnamese", "Example"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable dictTable = new JTable(dictModel);
        dictTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dictTable.setRowHeight(26);
        dictPanel.add(new JScrollPane(dictTable), BorderLayout.CENTER);

        JPanel dictBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnRefreshDict = new JButton("Refresh");
        btnRefreshDict.setBackground(new Color(33,150,243)); btnRefreshDict.setForeground(Color.WHITE);
        JButton btnAdd = new JButton("Thêm");
        btnAdd.setBackground(new Color(76,175,80)); btnAdd.setForeground(Color.WHITE);
        JButton btnEdit = new JButton("Sửa");
        btnEdit.setBackground(new Color(255,152,0)); btnEdit.setForeground(Color.WHITE);
        JButton btnDelete = new JButton("Xóa");
        btnDelete.setBackground(new Color(244,67,54)); btnDelete.setForeground(Color.WHITE);

        dictBtns.add(btnRefreshDict);
        dictBtns.add(btnAdd);
        dictBtns.add(btnEdit);
        dictBtns.add(btnDelete);
        dictPanel.add(dictBtns, BorderLayout.SOUTH);

        // --- History tab ---
        JPanel histPanel = new JPanel(new BorderLayout(8,8));
        histPanel.setBorder(new EmptyBorder(8,8,8,8));
        historyModel = new DefaultTableModel(new String[]{"Id", "QueryWord", "ClientAddr", "QueryTime"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable histTable = new JTable(historyModel);
        histTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        histTable.setRowHeight(26);
        histPanel.add(new JScrollPane(histTable), BorderLayout.CENTER);

        JPanel histBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnRefreshHist = new JButton("Refresh");
        btnRefreshHist.setBackground(new Color(33,150,243)); btnRefreshHist.setForeground(Color.WHITE);
        JButton btnClearHist = new JButton("Xóa lịch sử");
        btnClearHist.setBackground(new Color(244,67,54)); btnClearHist.setForeground(Color.WHITE);
        histBtns.add(btnRefreshHist);
        histBtns.add(btnClearHist);
        histPanel.add(histBtns, BorderLayout.SOUTH);

        tabs.addTab("Quản lý từ điển", dictPanel);
        tabs.addTab("Lịch sử", histPanel);

        add(tabs, BorderLayout.CENTER);

        // --- Button actions ---
        btnRefreshDict.addActionListener(e -> loadDictionary());
        btnRefreshHist.addActionListener(e -> loadHistory());
        btnAdd.addActionListener(e -> addWordDialog());
        btnEdit.addActionListener(e -> editWordDialog(dictTable));
        btnDelete.addActionListener(e -> deleteWordDialog(dictTable));
        btnClearHist.addActionListener(e -> clearHistoryDialog());

        // initial load
        loadDictionary();
        loadHistory();

        // start socket server in a new thread
        new Thread(this::startSocketServer).start();
    }

    // --- DB loads ---
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
                JOptionPane.showMessageDialog(this, "Lỗi tải dictionary: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadHistory() {
        SwingUtilities.invokeLater(() -> {
            historyModel.setRowCount(0);
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement("SELECT Id, QueryWord, ClientAddr, QueryTime FROM History ORDER BY QueryTime DESC");
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
                JOptionPane.showMessageDialog(this, "Lỗi tải history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // --- CRUD dialogs ---
    private void addWordDialog() {
        JTextField fEn = new JTextField();
        JTextField fVi = new JTextField();
        JTextField fEx = new JTextField();
        Object[] msg = {"English:", fEn, "Vietnamese:", fVi, "Example:", fEx};
        int opt = JOptionPane.showConfirmDialog(this, msg, "Thêm từ mới", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String en = fEn.getText().trim();
            String vi = fVi.getText().trim();
            String ex = fEx.getText().trim();
            if (en.isEmpty() || vi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "English và Vietnamese không được rỗng");
                return;
            }
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement("INSERT INTO EV_Dictionary (English, Vietnamese, Example) VALUES (?, ?, ?)")) {
                st.setString(1, en);
                st.setString(2, vi);
                st.setString(3, ex);
                st.executeUpdate();
                loadDictionary();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editWordDialog(JTable dictTable) {
        int r = dictTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Chọn bản ghi để sửa");
            return;
        }
        String oldEn = (String) dictModel.getValueAt(r, 0);
        String oldVi = (String) dictModel.getValueAt(r, 1);
        String oldEx = (String) dictModel.getValueAt(r, 2);

        JTextField fEn = new JTextField(oldEn);
        JTextField fVi = new JTextField(oldVi);
        JTextField fEx = new JTextField(oldEx);
        Object[] msg = {"English (mới):", fEn, "Vietnamese:", fVi, "Example:", fEx};
        int opt = JOptionPane.showConfirmDialog(this, msg, "Sửa từ", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String newEn = fEn.getText().trim();
            String newVi = fVi.getText().trim();
            String newEx = fEx.getText().trim();
            if (newEn.isEmpty() || newVi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "English và Vietnamese không được rỗng");
                return;
            }
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement st = conn.prepareStatement("UPDATE EV_Dictionary SET English = ?, Vietnamese = ?, Example = ? WHERE English = ?")) {
                st.setString(1, newEn);
                st.setString(2, newVi);
                st.setString(3, newEx);
                st.setString(4, oldEn);
                int aff = st.executeUpdate();
                if (aff > 0) loadDictionary();
                else JOptionPane.showMessageDialog(this, "Không tìm thấy từ để cập nhật");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi UPDATE: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteWordDialog(JTable dictTable) {
        int r = dictTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Chọn bản ghi để xóa");
            return;
        }
        String en = (String) dictModel.getValueAt(r, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa '" + en + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM EV_Dictionary WHERE English = ?")) {
            st.setString(1, en);
            st.executeUpdate();
            loadDictionary();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearHistoryDialog() {
        int conf = JOptionPane.showConfirmDialog(this, "Xóa toàn bộ lịch sử?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement st = conn.prepareStatement("DELETE FROM History")) {
            st.executeUpdate();
            loadHistory();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Socket server (mỗi kết nối xử lý 1 lệnh) ---
    private void startSocketServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            while (true) {
                Socket client = server.accept();
                new Thread(() -> handleClientSocket(client)).start();
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Lỗi server socket: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void handleClientSocket(Socket clientSocket) {
        try (Socket s = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            String line = in.readLine();
            if (line == null) return;

            if (line.startsWith("TRANSLATE:")) {
                String word = line.substring(10).trim();
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement st = conn.prepareStatement("SELECT Vietnamese, Example FROM EV_Dictionary WHERE English = ?")) {
                    st.setString(1, word);
                    try (ResultSet rs = st.executeQuery()) {
                        if (rs.next()) {
                            String viet = rs.getString("Vietnamese");
                            String ex = rs.getString("Example");
                            out.println(viet != null ? viet : "");
                            if (ex != null && !ex.isEmpty()) out.println("EXAMPLE:" + ex);
                        } else {
                            out.println("Không tìm thấy từ!");
                        }
                    }
                } catch (SQLException e) {
                    out.println("Lỗi truy vấn: " + e.getMessage());
                }

                // ghi history (client ip)
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement st = conn.prepareStatement("INSERT INTO History (QueryWord, ClientAddr) VALUES (?, ?)")) {
                    st.setString(1, word);
                    st.setString(2, clientSocket.getInetAddress().getHostAddress());
                    st.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Lỗi ghi history: " + e.getMessage());
                }

            } else if (line.equals("LISTALL")) {
                // gửi tất cả: mỗi bản ghi 1 dòng "English|Vietnamese|Example"
                try (Connection conn = DBConnector.getConnection();
                     PreparedStatement st = conn.prepareStatement("SELECT English, Vietnamese, Example FROM EV_Dictionary ORDER BY English");
                     ResultSet rs = st.executeQuery()) {
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        String e = rs.getString("English");
                        String v = rs.getString("Vietnamese");
                        String ex = rs.getString("Example");
                        out.println((e != null ? e : "") + "|" + (v != null ? v : "") + "|" + (ex != null ? ex : ""));
                    }
                    if (!any) out.println("EMPTY");
                } catch (SQLException e) {
                    out.println("Lỗi LISTALL: " + e.getMessage());
                }
            } else {
                out.println("Lệnh không hợp lệ!");
            }
            // khi try-with-resources đóng socket -> client nhận EOF và dừng đọc
        } catch (IOException e) {
            System.out.println("I/O error handling client: " + e.getMessage());
        } finally {
            // refresh history in GUI after each handled request (useful for admin)
            SwingUtilities.invokeLater(this::loadHistory);
        }
    }

    // --- main ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DictionaryServerGUI gui = new DictionaryServerGUI();
            gui.setVisible(true);
        });
    }
}

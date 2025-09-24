package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DictionaryClient extends JFrame {
    private JTextField txtEnglish;
    private JTextArea txtVietnamese;
    private JTable exampleTable;
    private DefaultTableModel exampleModel;
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;

    private final String SERVER_HOST = "127.0.0.1";
    private final int SERVER_PORT = 5000;

    public DictionaryClient() {
        setTitle("Từ điển - Client");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        // ======= TOP: input + buttons =======
        JPanel top = new JPanel(new BorderLayout(8, 8));
        txtEnglish = new JTextField();
        top.add(txtEnglish, BorderLayout.CENTER);

        JPanel topBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnDict = new JButton("Bộ từ điển");
        btnDict.setBackground(new Color(123, 31, 162));
        btnDict.setForeground(Color.WHITE);
        JButton btnClear = new JButton("Làm mới");
        btnClear.setBackground(new Color(33, 150, 243));
        btnClear.setForeground(Color.WHITE);
        JButton btnClearHist = new JButton("Xóa lịch sử");
        btnClearHist.setBackground(new Color(244, 67, 54));
        btnClearHist.setForeground(Color.WHITE);
        JButton btnTranslate = new JButton("Dịch");
        btnTranslate.setBackground(new Color(76, 175, 80));
        btnTranslate.setForeground(Color.WHITE);

        topBtns.add(btnDict);
        topBtns.add(btnClear);
        topBtns.add(btnClearHist);
        topBtns.add(btnTranslate);
        top.add(topBtns, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);

        // ======= CENTER: nghĩa tiếng Việt + ví dụ + lịch sử =======
        JPanel center = new JPanel(new GridLayout(3, 1, 8, 8));

        // 1. Khung "Tiếng Việt"
        txtVietnamese = new JTextArea();
        txtVietnamese.setEditable(false);
        txtVietnamese.setLineWrap(true);
        txtVietnamese.setWrapStyleWord(true);
        JScrollPane vietScroll = new JScrollPane(txtVietnamese);
        vietScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Tiếng Việt"));
        center.add(vietScroll);

        // 2. Khung "Ví dụ" giống khung "Tiếng Việt"
        exampleModel = new DefaultTableModel(new String[]{""}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        exampleTable = new JTable(exampleModel);
        exampleTable.setRowHeight(24);
        exampleTable.setFillsViewportHeight(true);

        JScrollPane exampleScroll = new JScrollPane(exampleTable);
        exampleScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Ví dụ"));
        center.add(exampleScroll);

        // 3. Khung "Lịch sử"
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Lịch sử tra cứu (local)"));
        center.add(historyScroll);

        root.add(center, BorderLayout.CENTER);

        // ======= HÀNH ĐỘNG =======
        btnTranslate.addActionListener(e -> translateWord());
        btnClear.addActionListener(e -> {
            txtEnglish.setText("");
            txtVietnamese.setText("");
            exampleModel.setRowCount(0);
        });
        btnClearHist.addActionListener(e -> historyModel.clear());
        btnDict.addActionListener(e -> showDictionaryPopup());

        historyList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = historyList.getSelectedValue();
                    if (sel != null) {
                        txtEnglish.setText(sel);
                        translateWord();
                    }
                }
            }
        });

        txtEnglish.addActionListener(e -> translateWord());

        setVisible(true);
    }


    private void translateWord() {
        String word = txtEnglish.getText().trim();
        if (word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập từ cần tra!");
            return;
        }
        List<String> resp = sendCommandAndGetLines("TRANSLATE:" + word);
        if (resp.isEmpty()) {
            txtVietnamese.setText("Lỗi kết nối tới server.");
            exampleModel.setRowCount(0);
            return;
        }

        StringBuilder viet = new StringBuilder();
        exampleModel.setRowCount(0);

        for (String line : resp) {
            if (line.startsWith("EXAMPLE:")) {
                exampleModel.addRow(new Object[]{line.substring(8)});
            } else {
                viet.append(line).append("\n");
            }
        }

        txtVietnamese.setText(viet.toString().trim());

        if (historyModel.isEmpty() || !historyModel.lastElement().equals(word)) {
            historyModel.addElement(word);
        }
    }

    private void showDictionaryPopup() {
        List<String> lines = sendCommandAndGetLines("LISTALL");
        if (lines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không lấy được bộ từ điển (server không phản hồi)", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DefaultTableModel model = new DefaultTableModel(new String[]{"English","Vietnamese","Example"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String line : lines) {
            if ("EMPTY".equals(line)) continue;
            if (line.startsWith("Lỗi") || line.startsWith("ERROR")) {
                JOptionPane.showMessageDialog(this, line, "Server error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] parts = line.split("\\|", 3);
            String e = parts.length>0?parts[0]:"";
            String v = parts.length>1?parts[1]:"";
            String ex = parts.length>2?parts[2]:"";
            model.addRow(new Object[]{e, v, ex});
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        JDialog dlg = new JDialog(this, "Bộ từ điển (chỉ xem)", true);
        dlg.setSize(900, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) {
                        String en = (String) model.getValueAt(r, 0);
                        txtEnglish.setText(en);
                        dlg.dispose();
                        translateWord();
                    }
                }
            }
        });

        dlg.setVisible(true);
    }

    private List<String> sendCommandAndGetLines(String cmd) {
        List<String> lines = new ArrayList<>();
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(cmd);
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // return empty list on failure
        }
        return lines;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DictionaryClient::new);
    }
}

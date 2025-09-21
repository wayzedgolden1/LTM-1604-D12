package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class DictionaryClient extends JFrame {

    private JTextArea txtEnglish;
    private JTextArea txtVietnamese;
    private JTextArea txtExample;
    private JTextArea txtHistory;
    private JButton btnTranslate, btnAddWord, btnDeleteWord;

    public DictionaryClient() {
        setTitle("Từ điển Anh-Việt");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel chính - 2 cột
        JPanel panelCenter = new JPanel(new GridLayout(1, 2, 10, 10));

        // English
        JPanel panelEnglish = new JPanel(new BorderLayout());
        panelEnglish.setBorder(BorderFactory.createTitledBorder("English"));
        txtEnglish = new JTextArea();
        txtEnglish.setLineWrap(true);
        txtEnglish.setWrapStyleWord(true);
        panelEnglish.add(new JScrollPane(txtEnglish), BorderLayout.CENTER);

        // Vietnamese + Example
        JPanel panelVietnamese = new JPanel(new BorderLayout());
        panelVietnamese.setBorder(BorderFactory.createTitledBorder("Vietnamese"));
        txtVietnamese = new JTextArea();
        txtVietnamese.setLineWrap(true);
        txtVietnamese.setWrapStyleWord(true);
        txtVietnamese.setEditable(false);
        panelVietnamese.add(new JScrollPane(txtVietnamese), BorderLayout.CENTER);

        txtExample = new JTextArea();
        txtExample.setLineWrap(true);
        txtExample.setWrapStyleWord(true);
        txtExample.setEditable(false);
        txtExample.setBackground(new Color(240, 240, 240));
        txtExample.setBorder(BorderFactory.createTitledBorder("Example"));
        panelVietnamese.add(txtExample, BorderLayout.SOUTH);

        panelCenter.add(panelEnglish);
        panelCenter.add(panelVietnamese);

        add(panelCenter, BorderLayout.CENTER);

        // Panel nút
        JPanel panelButtons = new JPanel();
        btnTranslate = new JButton("Dịch");
        btnAddWord = new JButton("Thêm từ");
        btnDeleteWord = new JButton("Xóa từ");
        panelButtons.add(btnTranslate);
        panelButtons.add(btnAddWord);
        panelButtons.add(btnDeleteWord);
        add(panelButtons, BorderLayout.SOUTH);

        // Panel lịch sử
        txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setLineWrap(true);
        txtHistory.setWrapStyleWord(true);
        add(new JScrollPane(txtHistory), BorderLayout.EAST);
        txtHistory.setBorder(BorderFactory.createTitledBorder("Lịch sử tra cứu"));

        // Action
        btnTranslate.addActionListener(e -> translateWord());
        btnAddWord.addActionListener(e -> addWord());
        btnDeleteWord.addActionListener(e -> deleteWord());

        setVisible(true);
    }

    private void translateWord() {
        String word = txtEnglish.getText().trim();
        if (word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập từ cần tra cứu!");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("TRANSLATE:" + word);

            String response;
            StringBuilder sb = new StringBuilder();
            StringBuilder sbExample = new StringBuilder();
            while ((response = in.readLine()) != null) {
                if (response.startsWith("EXAMPLE:")) {
                    sbExample.append(response.substring(8)).append("\n");
                } else {
                    sb.append(response).append("\n");
                }
            }
            txtVietnamese.setText(sb.toString());
            txtExample.setText(sbExample.toString());

            // Cập nhật lịch sử
            txtHistory.append(word + "\n");

        } catch (IOException e) {
            txtVietnamese.setText("Lỗi kết nối tới server!");
        }
    }

    private void addWord() {
        String english = txtEnglish.getText().trim();
        String vietnamese = JOptionPane.showInputDialog(this, "Nhập nghĩa tiếng Việt:");
        String example = JOptionPane.showInputDialog(this, "Nhập ví dụ:");

        if (english.isEmpty() || vietnamese.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin!");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("ADD:" + english + "|" + vietnamese + "|" + example);
            String result = in.readLine();
            JOptionPane.showMessageDialog(this, result);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối tới server!");
        }
    }

    private void deleteWord() {
        String word = txtEnglish.getText().trim();
        if (word.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập từ cần xóa!");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("DELETE:" + word);
            String result = in.readLine();
            JOptionPane.showMessageDialog(this, result);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối tới server!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DictionaryClient::new);
    }
}

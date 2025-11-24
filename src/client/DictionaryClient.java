package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import javax.imageio.ImageIO;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class DictionaryClient extends JFrame {
    // ====== Các thành phần giao diện ======
    private JTextField txtEnglish;
    private JTextArea txtVietnamese;
    private JTextArea txtExample;
    private JLabel imageLabel;
    private DefaultListModel<String> historyModel;
    private JList<String> historyList;
    private JButton btnToggle;

    // ====== Cấu hình kết nối ======
    private final String SERVER_HOST = "127.0.0.1";
    private final int SERVER_PORT = 5000;
    private String currentMode = "EV";

    // ====== API Config ======
    private final String PIXABAY_API_KEY = "53341938-b07a52ee775abeb5fba944ab0"; // THAY THẾ BẰNG KEY CỦA BẠN
    private final String PIXABAY_API_URL = "https://pixabay.com/api/";

    // ====== Màu sắc hiện đại ======
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = new Color(51, 51, 51);
    private final Color IMAGE_BORDER_COLOR = new Color(241, 196, 15);

    // ====== Font sizes mới ======
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // ====== Constants for message types ======
    private static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
    private static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;

    public DictionaryClient() {
        setTitle("Từ Điển Anh - Việt");
        setSize(1400, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        // Main panel với gradient background
        JPanel mainPanel = new GradientPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        setContentPane(mainPanel);

        // ====== HEADER ======
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ====== CENTER CONTENT ======
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        header.setPreferredSize(new Dimension(getWidth(), 140));

        // Title
        JLabel titleLabel = new JLabel("TỪ ĐIỂN ANH - VIỆT");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Search panel
        JPanel searchPanel = new RoundedPanel(25, new Color(255, 255, 255, 240));
        searchPanel.setLayout(new BorderLayout(10, 0));
        searchPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        txtEnglish = new JTextField();
        txtEnglish.setFont(INPUT_FONT);
        txtEnglish.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        txtEnglish.setOpaque(true);
        txtEnglish.setBackground(Color.WHITE);
        txtEnglish.setForeground(TEXT_COLOR);
        txtEnglish.setPreferredSize(new Dimension(300, 45));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton btnTranslate = createStyledButton("Dịch Từ", ACCENT_COLOR);
        JButton btnClear = createStyledButton("Làm Mới", SECONDARY_COLOR);
        btnToggle = createStyledButton("Anh → Việt", new Color(155, 89, 182));
        JButton btnLoadFile = createStyledButton("Tải File", new Color(241, 196, 15));

        buttonPanel.add(btnTranslate);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnToggle);
        buttonPanel.add(btnLoadFile);

        searchPanel.add(txtEnglish, BorderLayout.CENTER);
        searchPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        centerContainer.setOpaque(false);
        centerContainer.add(searchPanel);
        
        titlePanel.add(centerContainer, BorderLayout.CENTER);

        header.add(titlePanel, BorderLayout.CENTER);

        // ====== ACTION LISTENERS ======
        btnTranslate.addActionListener(e -> translateWord());
        btnClear.addActionListener(e -> clearAll());
        btnToggle.addActionListener(e -> toggleMode());
        btnLoadFile.addActionListener(e -> importDictionaryFromFile());
        txtEnglish.addActionListener(e -> translateWord());

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // ====== LEFT PANEL - Kết quả dịch và ví dụ ======
        JPanel leftPanel = new RoundedPanel(20, CARD_COLOR);
        leftPanel.setLayout(new BorderLayout(15, 15));
        leftPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel leftTitle = new JLabel("KẾT QUẢ DỊCH");
        leftTitle.setFont(HEADER_FONT);
        leftTitle.setForeground(PRIMARY_COLOR);
        leftTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        leftPanel.add(leftTitle, BorderLayout.NORTH);

        // Main content panel với GridBagLayout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Kết quả dịch - chiếm 60%
        JPanel resultPanel = new RoundedPanel(15, new Color(250, 250, 250));
        resultPanel.setLayout(new BorderLayout());
        
        TitledBorder resultBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR, 1), "Nghĩa dịch"
        );
        resultBorder.setTitleFont(LABEL_FONT);
        resultBorder.setTitleColor(SECONDARY_COLOR);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            resultBorder,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        txtVietnamese = new JTextArea();
        txtVietnamese.setEditable(false);
        txtVietnamese.setLineWrap(true);
        txtVietnamese.setWrapStyleWord(true);
        txtVietnamese.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtVietnamese.setBackground(new Color(250, 250, 250));
        txtVietnamese.setForeground(TEXT_COLOR);
        txtVietnamese.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane vietScroll = new JScrollPane(txtVietnamese);
        vietScroll.setBorder(BorderFactory.createEmptyBorder());
        vietScroll.getViewport().setBackground(new Color(250, 250, 250));
        resultPanel.add(vietScroll, BorderLayout.CENTER);

        // Ví dụ - chiếm 40%
        JPanel examplePanel = new RoundedPanel(15, new Color(250, 250, 250));
        examplePanel.setLayout(new BorderLayout());
        
        TitledBorder exampleBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1), "Ví dụ sử dụng"
        );
        exampleBorder.setTitleFont(LABEL_FONT);
        exampleBorder.setTitleColor(ACCENT_COLOR);
        examplePanel.setBorder(BorderFactory.createCompoundBorder(
            exampleBorder,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        txtExample = new JTextArea();
        txtExample.setEditable(false);
        txtExample.setLineWrap(true);
        txtExample.setWrapStyleWord(true);
        txtExample.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        txtExample.setBackground(new Color(250, 250, 250));
        txtExample.setForeground(TEXT_COLOR);
        txtExample.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane exampleScroll = new JScrollPane(txtExample);
        exampleScroll.setBorder(BorderFactory.createEmptyBorder());
        exampleScroll.getViewport().setBackground(new Color(250, 250, 250));
        examplePanel.add(exampleScroll, BorderLayout.CENTER);

        // Sử dụng GridBagLayout để phân chia tỷ lệ
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 0);
        contentPanel.add(resultPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(examplePanel, gbc);

        leftPanel.add(contentPanel, BorderLayout.CENTER);

        // ====== MIDDLE PANEL - Hình ảnh minh họa ======
        JPanel middlePanel = new RoundedPanel(20, CARD_COLOR);
        middlePanel.setLayout(new BorderLayout(15, 15));
        middlePanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel middleTitle = new JLabel("HÌNH ẢNH MINH HỌA");
        middleTitle.setFont(HEADER_FONT);
        middleTitle.setForeground(PRIMARY_COLOR);
        middleTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        middlePanel.add(middleTitle, BorderLayout.NORTH);

        // Panel hiển thị ảnh
        JPanel imageContainer = new RoundedPanel(15, new Color(250, 250, 250));
        imageContainer.setLayout(new BorderLayout());
        
        TitledBorder imageBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(IMAGE_BORDER_COLOR, 1), "Ảnh minh họa từ vựng"
        );
        imageBorder.setTitleFont(LABEL_FONT);
        imageBorder.setTitleColor(IMAGE_BORDER_COLOR);
        imageContainer.setBorder(BorderFactory.createCompoundBorder(
            imageBorder,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Label hiển thị ảnh
        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        imageLabel.setForeground(new Color(150, 150, 150));
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(250, 300));
        
        showImagePlaceholder("Nhập từ để xem ảnh minh họa");
        
        JScrollPane imageScroll = new JScrollPane(imageLabel);
        imageScroll.setBorder(BorderFactory.createEmptyBorder());
        imageScroll.getViewport().setBackground(new Color(250, 250, 250));
        imageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        imageScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        imageContainer.add(imageScroll, BorderLayout.CENTER);

        // Panel thông tin ảnh
        JPanel imageInfoPanel = new JPanel(new BorderLayout());
        imageInfoPanel.setOpaque(false);
        imageInfoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel infoLabel = new JLabel("Ảnh được tải từ Pixabay API", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(120, 120, 120));
        imageInfoPanel.add(infoLabel, BorderLayout.CENTER);

        middlePanel.add(imageContainer, BorderLayout.CENTER);
        middlePanel.add(imageInfoPanel, BorderLayout.SOUTH);

        // ====== RIGHT PANEL - Lịch sử và từ điển ======
        JPanel rightPanel = new RoundedPanel(20, CARD_COLOR);
        rightPanel.setLayout(new BorderLayout(15, 15));
        rightPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Title
        JLabel rightTitle = new JLabel("QUẢN LÝ");
        rightTitle.setFont(HEADER_FONT);
        rightTitle.setForeground(PRIMARY_COLOR);
        rightTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        rightPanel.add(rightTitle, BorderLayout.NORTH);

        // Main content panel for right side với GridBagLayout
        JPanel rightContentPanel = new JPanel(new GridBagLayout());
        rightContentPanel.setOpaque(false);
        GridBagConstraints gbcRight = new GridBagConstraints();

        // Lịch sử tra cứu
        JPanel historyPanel = new RoundedPanel(15, new Color(250, 250, 250));
        historyPanel.setLayout(new BorderLayout());
        
        TitledBorder historyBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182), 1), "Lịch sử tra cứu"
        );
        historyBorder.setTitleFont(LABEL_FONT);
        historyBorder.setTitleColor(new Color(155, 89, 182));
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
            historyBorder,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(TABLE_FONT);
        historyList.setBackground(new Color(250, 250, 250));
        historyList.setForeground(TEXT_COLOR);
        historyList.setSelectionBackground(SECONDARY_COLOR);
        historyList.setSelectionForeground(Color.WHITE);
        historyList.setFixedCellHeight(35);
        
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        historyScroll.getViewport().setBackground(new Color(250, 250, 250));
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        // CRUD Operations Panel
        JPanel crudPanel = new RoundedPanel(15, new Color(250, 250, 250));
        crudPanel.setLayout(new BorderLayout());
        
        TitledBorder crudBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(230, 126, 34), 1), "Quản lý từ điển"
        );
        crudBorder.setTitleFont(LABEL_FONT);
        crudBorder.setTitleColor(new Color(230, 126, 34));
        crudPanel.setBorder(BorderFactory.createCompoundBorder(
            crudBorder,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JPanel crudButtonsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        crudButtonsPanel.setOpaque(false);
        crudButtonsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JButton btnDict = createStyledButton("Xem Bộ Từ Điển", new Color(52, 152, 219));
        JButton btnAddWord = createStyledButton("Thêm Từ Mới", new Color(46, 204, 113));
        JButton btnEditWord = createStyledButton("Sửa Từ Hiện Tại", new Color(230, 126, 34));
        JButton btnDeleteWord = createStyledButton("Xóa Từ Hiện Tại", new Color(231, 76, 60));

        crudButtonsPanel.add(btnDict);
        crudButtonsPanel.add(btnAddWord);
        crudButtonsPanel.add(btnEditWord);
        crudButtonsPanel.add(btnDeleteWord);

        crudPanel.add(crudButtonsPanel, BorderLayout.CENTER);

        // Sử dụng GridBagLayout để phân chia tỷ lệ bên phải
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.weightx = 1.0;
        gbcRight.weighty = 0.6;
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.insets = new Insets(0, 0, 15, 0);
        rightContentPanel.add(historyPanel, gbcRight);

        gbcRight.gridy = 1;
        gbcRight.weighty = 0.4;
        gbcRight.insets = new Insets(0, 0, 0, 0);
        rightContentPanel.add(crudPanel, gbcRight);

        rightPanel.add(rightContentPanel, BorderLayout.CENTER);

        // Thêm các panel vào center
        centerPanel.add(leftPanel);
        centerPanel.add(middlePanel);
        centerPanel.add(rightPanel);

        // ====== ACTION LISTENERS ======
        btnDict.addActionListener(e -> showDictionaryPopup());
        btnAddWord.addActionListener(e -> showAddWordDialog());
        btnEditWord.addActionListener(e -> showEditWordDialog());
        btnDeleteWord.addActionListener(e -> showDeleteWordDialog());

        // ====== DOUBLE CLICK HISTORY ======
        historyList.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = historyList.getSelectedValue();
                    if (sel != null) {
                        String wordOnly = sel.replaceAll("\\s*\\(.*\\)", "").trim();
                        txtEnglish.setText(wordOnly);
                        translateWord();
                    }
                }
            }
        });

        return centerPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    // ====== CÁC PHƯƠNG THỨC CHỨC NĂNG ======

    private void toggleMode() {
        if (currentMode.equals("EV")) {
            currentMode = "VE";
            btnToggle.setText("Việt → Anh");
        } else {
            currentMode = "EV";
            btnToggle.setText("Anh → Việt");
        }
    }

    private void translateWord() {
        String word = txtEnglish.getText().trim();
        if (word.isEmpty()) {
            showMessage("Thông báo", "Vui lòng nhập từ cần tra!", INFORMATION_MESSAGE);
            return;
        }

        showLoading(true);
        
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return sendCommandAndGetLines("TRANSLATE:" + currentMode + ":" + word);
            }
            
            @Override
            protected void done() {
                showLoading(false);
                try {
                    List<String> resp = get();
                    
                    if (resp.isEmpty()) {
                        txtVietnamese.setText("Lỗi kết nối tới server. Vui lòng kiểm tra kết nối.");
                        txtExample.setText("");
                        showImagePlaceholder("Lỗi kết nối");
                        return;
                    }

                    StringBuilder viet = new StringBuilder();
                    StringBuilder examples = new StringBuilder();
                    txtExample.setText("");

                    for (String line : resp) {
                        if (line.startsWith("EXAMPLE:")) {
                            examples.append(line.substring(8)).append("\n\n");
                        } else if (!line.equals("Không tìm thấy từ!")) {
                            viet.append(line).append("\n");
                        } else {
                            viet.append(line).append("\n");
                        }
                    }

                    txtVietnamese.setText(viet.toString().trim());
                    txtExample.setText(examples.toString().trim());

                    // Lưu lịch sử
                    String displayMode = currentMode.equals("EV") ? "Anh → Việt" : "Việt → Anh";
                    String historyEntry = word + " (" + displayMode + ")";
                    
                    if (historyModel.contains(historyEntry)) {
                        historyModel.removeElement(historyEntry);
                    }
                    
                    historyModel.insertElementAt(historyEntry, 0);
                    
                    if (historyModel.size() > 50) {
                        historyModel.removeElementAt(historyModel.size() - 1);
                    }

                    // Tải ảnh minh họa liên quan đến từ
                    loadWordImage(word);

                } catch (Exception e) {
                    txtVietnamese.setText("Lỗi trong quá trình dịch: " + e.getMessage());
                    txtExample.setText("");
                    showImagePlaceholder("Lỗi dịch từ");
                }
            }
        }.execute();
    }

    // ====== HỆ THỐNG TẢI ẢNH VỚI FALLBACK ======
    private void loadWordImage(String word) {
        showImagePlaceholder("Đang tải ảnh...");
        
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                System.out.println("🔄 Bắt đầu tải ảnh cho từ: " + word);
                
                // THỬ PIXABAY API TRƯỚC
                ImageIcon pixabayImage = loadFromPixabay(word);
                if (pixabayImage != null) {
                    System.out.println("✅ Thành công với Pixabay API");
                    return pixabayImage;
                }
                
                // FALLBACK 1: Unsplash API đơn giản
                System.out.println("🔄 Thử fallback Unsplash...");
                ImageIcon unsplashImage = loadFromUnsplash(word);
                if (unsplashImage != null) {
                    System.out.println("✅ Thành công với Unsplash");
                    return unsplashImage;
                }
                
                // FALLBACK 2: Tạo ảnh động với từ khóa
                System.out.println("🎨 Tạo ảnh mặc định...");
                return createDefaultImage(word);
            }
            
            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageLabel.setIcon(icon);
                        imageLabel.setText("");
                        imageLabel.setToolTipText("Ảnh minh họa cho từ: " + word);
                        System.out.println("✨ Hiển thị ảnh thành công!");
                    } else {
                        showImagePlaceholder("Không thể tải ảnh cho từ: " + word);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Lỗi khi hiển thị ảnh: " + e.getMessage());
                    showImagePlaceholder("Lỗi tải ảnh");
                }
            }
        }.execute();
    }

    // ====== PIXABAY API ======
    private ImageIcon loadFromPixabay(String word) {
        try {
            String apiUrl = PIXABAY_API_URL + "?key=" + PIXABAY_API_KEY + 
                           "&q=" + URLEncoder.encode(word, "UTF-8") + 
                           "&image_type=photo&per_page=3";
            
            System.out.println("🔗 Pixabay URL: " + apiUrl);
            
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "DictionaryApp/1.0");
            connection.setConnectTimeout(3000); // Timeout ngắn 3s
            connection.setReadTimeout(3000);
            
            int responseCode = connection.getResponseCode();
            System.out.println("📡 Pixabay Response: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response manually
                String imageUrl = extractImageUrlFromJson(response.toString());
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    System.out.println("✅ Tìm thấy ảnh Pixabay: " + imageUrl);
                    
                    // Tải ảnh từ URL
                    URL imageUrlObj = new URL(imageUrl);
                    BufferedImage image = ImageIO.read(imageUrlObj);
                    
                    if (image != null) {
                        Image scaledImage = image.getScaledInstance(350, 250, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                }
            }
            connection.disconnect();
            
        } catch (Exception e) {
            System.out.println("❌ Lỗi Pixabay: " + e.getMessage());
        }
        return null;
    }

    // ====== UNSPLASH FALLBACK ======
    private ImageIcon loadFromUnsplash(String word) {
        try {
            // Unsplash API đơn giản không cần key
            String unsplashUrl = "https://source.unsplash.com/400x300/?" + URLEncoder.encode(word, "UTF-8");
            
            System.out.println("🔗 Unsplash URL: " + unsplashUrl);
            
            URL url = new URL(unsplashUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "DictionaryApp/1.0");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setInstanceFollowRedirects(true);
            
            int responseCode = connection.getResponseCode();
            System.out.println("📡 Unsplash Response: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                BufferedImage image = ImageIO.read(connection.getInputStream());
                connection.disconnect();
                
                if (image != null) {
                    System.out.println("✅ Tải ảnh Unsplash thành công");
                    Image scaledImage = image.getScaledInstance(350, 250, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                }
            }
            connection.disconnect();
            
        } catch (Exception e) {
            System.out.println("❌ Lỗi Unsplash: " + e.getMessage());
        }
        return null;
    }

    // ====== PARSE JSON MANUALLY ======
    private String extractImageUrlFromJson(String jsonResponse) {
        try {
            // Tìm "webformatURL" trong JSON response
            int urlIndex = jsonResponse.indexOf("\"webformatURL\"");
            if (urlIndex == -1) {
                urlIndex = jsonResponse.indexOf("\"largeImageURL\"");
            }
            
            if (urlIndex != -1) {
                int urlStart = jsonResponse.indexOf("\"", urlIndex + 15) + 1;
                int urlEnd = jsonResponse.indexOf("\"", urlStart);
                
                if (urlStart > 0 && urlEnd > urlStart) {
                    return jsonResponse.substring(urlStart, urlEnd);
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Lỗi parse JSON: " + e.getMessage());
        }
        return null;
    }

    // ====== TẠO ẢNH MẶC ĐỊNH ======
    private ImageIcon createDefaultImage(String word) {
        try {
            int width = 350;
            int height = 250;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Background gradient đẹp
            GradientPaint gradient = new GradientPaint(0, 0, new Color(74, 144, 226), width, height, new Color(155, 89, 182));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            
            // Vẽ từ được tra cứu
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
            String displayWord = word.length() > 12 ? word.substring(0, 12) + "..." : word;
            FontMetrics fm = g2d.getFontMetrics();
            int wordWidth = fm.stringWidth(displayWord);
            g2d.drawString(displayWord, (width - wordWidth) / 2, 120);
            
            // Vẽ dòng mô tả
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g2d.setColor(new Color(255, 255, 255, 200));
            String description = "Từ điển Anh - Việt";
            int descWidth = g2d.getFontMetrics().stringWidth(description);
            g2d.drawString(description, (width - descWidth) / 2, 150);
            
            g2d.dispose();
            return new ImageIcon(image);
            
        } catch (Exception e) {
            System.out.println("❌ Lỗi tạo ảnh mặc định: " + e.getMessage());
            return null;
        }
    }

    private void showImagePlaceholder(String message) {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setIcon(null);
            imageLabel.setText("<html><div style='text-align: center; color: #666; padding: 20px;'>" + 
                             message + "</div></html>");
            imageLabel.setToolTipText(null);
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            txtVietnamese.setText("Đang dịch...");
            txtExample.setText("");
            showImagePlaceholder("Đang xử lý...");
        }
    }

    // ====== CHỨC NĂNG NHẬP TỪ ĐIỂN TỪ FILE ======
    private void importDictionaryFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file từ điển cần nhập");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Text Files (*.txt)", "txt"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                StringBuilder fileContent = new StringBuilder();
                String line;
                int lineCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                    lineCount++;
                }
                reader.close();
                
                JTextArea textArea = new JTextArea(15, 50);
                textArea.setText(fileContent.toString());
                textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                
                int option = JOptionPane.showConfirmDialog(this,
                    new Object[]{
                        "File chứa " + lineCount + " dòng. Định dạng mong đợi:",
                        "English|Vietnamese|Example (mỗi dòng một từ)",
                        "Ví dụ: hello|xin chào|Hello, how are you?",
                        scrollPane
                    },
                    "Xem trước file từ điển",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (option == JOptionPane.OK_OPTION) {
                    processDictionaryImport(selectedFile);
                }
                
            } catch (IOException e) {
                showMessage("Lỗi", "Không thể đọc file: " + e.getMessage(), ERROR_MESSAGE);
            }
        }
    }

    private void processDictionaryImport(File file) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                int successCount = 0;
                int errorCount = 0;
                StringBuilder result = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    String[] parts = line.split("\\|", 3);
                    if (parts.length >= 2) {
                        String english = parts[0].trim();
                        String vietnamese = parts[1].trim();
                        String example = parts.length > 2 ? parts[2].trim() : "";
                        
                        List<String> response = sendCommandAndGetLines("ADD:" + english + ":" + vietnamese + ":" + example);
                        if (!response.isEmpty() && response.get(0).equals("SUCCESS")) {
                            successCount++;
                        } else {
                            errorCount++;
                            result.append("Lỗi thêm từ '").append(english).append("': ")
                                  .append(response.isEmpty() ? "Lỗi kết nối" : response.get(0)).append("\n");
                        }
                    } else {
                        errorCount++;
                        result.append("Định dạng không hợp lệ: ").append(line).append("\n");
                    }
                    
                    Thread.sleep(50);
                }
                reader.close();
                
                result.insert(0, "Kết quả nhập từ điển:\n" +
                                "Thành công: " + successCount + " từ\n" +
                                "Lỗi: " + errorCount + " từ\n\n" +
                                "Chi tiết lỗi:\n");
                return result.toString();
            }
            
            @Override
            protected void done() {
                try {
                    String importResult = get();
                    
                    JTextArea resultArea = new JTextArea(20, 60);
                    resultArea.setText(importResult);
                    resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    resultArea.setLineWrap(true);
                    resultArea.setWrapStyleWord(true);
                    resultArea.setEditable(false);
                    
                    JScrollPane scrollPane = new JScrollPane(resultArea);
                    
                    JOptionPane.showMessageDialog(DictionaryClient.this,
                        scrollPane,
                        "Kết quả nhập từ điển",
                        JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (Exception e) {
                    showMessage("Lỗi", "Lỗi trong quá trình nhập từ điển: " + e.getMessage(), ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void clearAll() {
        txtEnglish.setText("");
        txtVietnamese.setText("");
        txtExample.setText("");
        showImagePlaceholder("Nhập từ để xem ảnh minh họa");
    }

    // ====== CRUD OPERATIONS ======
    private void showAddWordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextField txtEnglishWord = new JTextField();
        JTextField txtVietnameseMean = new JTextField();
        JTextField txtExampleField = new JTextField();

        for (JTextField field : new JTextField[]{txtEnglishWord, txtVietnameseMean, txtExampleField}) {
            field.setFont(INPUT_FONT);
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
        }

        panel.add(createLabel("Từ tiếng Anh:"));
        panel.add(txtEnglishWord);
        panel.add(createLabel("Nghĩa tiếng Việt:"));
        panel.add(txtVietnameseMean);
        panel.add(createLabel("Ví dụ:"));
        panel.add(txtExampleField);

        int option = JOptionPane.showConfirmDialog(this, panel, 
            "Thêm Từ Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String english = txtEnglishWord.getText().trim();
            String vietnamese = txtVietnameseMean.getText().trim();
            String example = txtExampleField.getText().trim();

            if (english.isEmpty() || vietnamese.isEmpty()) {
                showMessage("Lỗi", "Từ tiếng Anh và nghĩa tiếng Việt không được để trống!", ERROR_MESSAGE);
                return;
            }

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    List<String> response = sendCommandAndGetLines("ADD:" + english + ":" + vietnamese + ":" + example);
                    return response.isEmpty() ? "Lỗi kết nối" : response.get(0);
                }
                
                @Override
                protected void done() {
                    try {
                        String result = get();
                        if (result.equals("SUCCESS")) {
                            showMessage("Thành công", "Thêm từ thành công!", INFORMATION_MESSAGE);
                        } else {
                            showMessage("Lỗi", result, ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        showMessage("Lỗi", "Lỗi khi thêm từ: " + e.getMessage(), ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void showEditWordDialog() {
        String currentWord = txtEnglish.getText().trim();
        if (currentWord.isEmpty()) {
            showMessage("Thông báo", "Vui lòng nhập từ cần sửa hoặc chọn từ trong bộ từ điển!", INFORMATION_MESSAGE);
            return;
        }

        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                List<String> currentInfo = sendCommandAndGetLines("GETWORD:" + currentWord);
                if (currentInfo.isEmpty() || currentInfo.get(0).startsWith("Lỗi") || currentInfo.get(0).equals("Không tìm thấy từ")) {
                    return null;
                }
                return currentInfo.get(0).split("\\|");
            }
            
            @Override
            protected void done() {
                try {
                    String[] parts = get();
                    if (parts == null) {
                        showMessage("Lỗi", "Không tìm thấy từ trong từ điển!", ERROR_MESSAGE);
                        return;
                    }

                    String currentEnglish = parts.length > 0 ? parts[0] : currentWord;
                    String currentVietnamese = parts.length > 1 ? parts[1] : "";
                    String currentExample = parts.length > 2 ? parts[2] : "";

                    JPanel panel = new JPanel(new GridLayout(3, 2, 15, 15));
                    panel.setBorder(new EmptyBorder(20, 20, 20, 20));
                    panel.setBackground(Color.WHITE);

                    JTextField txtEnglishWord = new JTextField(currentEnglish);
                    JTextField txtVietnameseMean = new JTextField(currentVietnamese);
                    JTextField txtExampleField = new JTextField(currentExample);

                    for (JTextField field : new JTextField[]{txtEnglishWord, txtVietnameseMean, txtExampleField}) {
                        field.setFont(INPUT_FONT);
                        field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                        ));
                    }

                    panel.add(createLabel("Từ tiếng Anh:"));
                    panel.add(txtEnglishWord);
                    panel.add(createLabel("Nghĩa tiếng Việt:"));
                    panel.add(txtVietnameseMean);
                    panel.add(createLabel("Ví dụ:"));
                    panel.add(txtExampleField);

                    int option = JOptionPane.showConfirmDialog(DictionaryClient.this, panel, 
                        "Sửa Từ", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    
                    if (option == JOptionPane.OK_OPTION) {
                        String newEnglish = txtEnglishWord.getText().trim();
                        String newVietnamese = txtVietnameseMean.getText().trim();
                        String newExample = txtExampleField.getText().trim();

                        if (newEnglish.isEmpty() || newVietnamese.isEmpty()) {
                            showMessage("Lỗi", "Từ tiếng Anh và nghĩa tiếng Việt không được để trống!", ERROR_MESSAGE);
                            return;
                        }

                        executeEdit(currentEnglish, newEnglish, newVietnamese, newExample);
                    }

                } catch (Exception e) {
                    showMessage("Lỗi", "Lỗi khi tải thông tin từ: " + e.getMessage(), ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void executeEdit(String oldEnglish, String newEnglish, String newVietnamese, String newExample) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                List<String> response = sendCommandAndGetLines("EDIT:" + oldEnglish + ":" + newEnglish + ":" + newVietnamese + ":" + newExample);
                return response.isEmpty() ? "Lỗi kết nối" : response.get(0);
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.equals("SUCCESS")) {
                        showMessage("Thành công", "Sửa từ thành công!", INFORMATION_MESSAGE);
                        txtEnglish.setText(newEnglish);
                        translateWord();
                    } else {
                        showMessage("Lỗi", result, ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    showMessage("Lỗi", "Lỗi khi sửa từ: " + e.getMessage(), ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showDeleteWordDialog() {
        String currentWord = txtEnglish.getText().trim();
        if (currentWord.isEmpty()) {
            showMessage("Thông báo", "Vui lòng nhập từ cần xóa!", INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "<html><b>Bạn có chắc muốn xóa từ '" + currentWord + "'?</b><br>Hành động này không thể hoàn tác.</html>",
            "Xác Nhận Xóa Từ",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    List<String> response = sendCommandAndGetLines("DELETE:" + currentWord);
                    return response.isEmpty() ? "Lỗi kết nối" : response.get(0);
                }
                
                @Override
                protected void done() {
                    try {
                        String result = get();
                        if (result.equals("SUCCESS")) {
                            showMessage("Thành công", "Xóa từ thành công!", INFORMATION_MESSAGE);
                            clearAll();
                        } else {
                            showMessage("Lỗi", result, ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        showMessage("Lỗi", "Lỗi khi xóa từ: " + e.getMessage(), ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void showDictionaryPopup() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return sendCommandAndGetLines("LISTALL");
            }
            
            @Override
            protected void done() {
                try {
                    List<String> lines = get();
                    showDictionaryDialog(lines);
                } catch (Exception e) {
                    showMessage("Lỗi", "Không thể tải bộ từ điển: " + e.getMessage(), ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showDictionaryDialog(List<String> lines) {
        if (lines.isEmpty()) {
            showMessage("Lỗi", "Không lấy được bộ từ điển (server không phản hồi)", ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = new DefaultTableModel(new String[]{"English", "Vietnamese", "Example"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (String line : lines) {
            if ("EMPTY".equals(line)) continue;
            if (line.startsWith("Lỗi") || line.startsWith("ERROR")) {
                showMessage("Lỗi", line, ERROR_MESSAGE);
                return;
            }
            String[] parts = line.split("\\|", 3);
            String e = parts.length > 0 ? parts[0] : "";
            String v = parts.length > 1 ? parts[1] : "";
            String ex = parts.length > 2 ? parts[2] : "";
            model.addRow(new Object[]{e, v, ex});
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(35);
        table.setFont(TABLE_FONT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(SECONDARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JDialog dlg = new JDialog(this, "Bộ Từ Điển", true);
        dlg.setSize(900, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(LABEL_FONT);
        JTextField txtSearch = new JTextField();
        txtSearch.setFont(INPUT_FONT);
        
        searchPanel.add(lblSearch, BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        dlg.add(searchPanel, BorderLayout.NORTH);
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) {
                        String en = (String) model.getValueAt(table.convertRowIndexToModel(r), 0);
                        txtEnglish.setText(en);
                        dlg.dispose();
                        translateWord();
                    }
                }
            }
        });

        dlg.setVisible(true);
    }

    // ====== UTILITY METHODS ======

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
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

    // ====== CUSTOM ROUNDED PANEL ======
    class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            cornerRadius = radius;
            backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        }
    }

    // ====== GRADIENT PANEL ======
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            Color color1 = PRIMARY_COLOR;
            Color color2 = new Color(52, 73, 94);
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    // ====== MAIN ======
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName()) || "Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            new DictionaryClient();
        });
    }
}
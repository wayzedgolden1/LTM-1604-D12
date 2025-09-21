package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class DictionaryServer {

    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Server đang chạy trên port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String input = in.readLine();
            System.out.println("Nhận từ Client: " + input);

            if (input.startsWith("TRANSLATE:")) {
                String word = input.substring(10);
                String response = searchWord(word);
                out.println(response);

            } else if (input.startsWith("ADD:")) {
                String[] parts = input.substring(4).split("\\|", 3);
                if (parts.length == 3) {
                    String result = addWord(parts[0], parts[1], parts[2]);
                    out.println(result);
                } else {
                    out.println("Lỗi: Dữ liệu thêm không hợp lệ!");
                }

            } else if (input.startsWith("DELETE:")) {
                String word = input.substring(7);
                String result = deleteWord(word);
                out.println(result);

            } else {
                out.println("Lệnh không hợp lệ!");
            }

        } catch (IOException e) {
            System.out.println("Lỗi kết nối Client: " + e.getMessage());
        }
    }

    private static String searchWord(String english) {
        StringBuilder result = new StringBuilder();
        StringBuilder example = new StringBuilder();

        try (Connection conn = DBConnector.getConnection()) {
            String sql = "SELECT * FROM EV_Dictionary WHERE English = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, english);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    result.append(rs.getString("Vietnamese"));
                    example.append(rs.getString("Example"));
                } else {
                    result.append("Không tìm thấy từ!");
                }
            }
        } catch (SQLException e) {
            result.append("Lỗi truy vấn: ").append(e.getMessage());
        }

        return result.toString() + "\nEXAMPLE:" + example.toString();
    }

    private static String addWord(String english, String vietnamese, String example) {
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "INSERT INTO EV_Dictionary (English, Vietnamese, Example) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, english);
                stmt.setString(2, vietnamese);
                stmt.setString(3, example);
                stmt.executeUpdate();
                return "Thêm từ thành công!";
            }
        } catch (SQLException e) {
            return "Lỗi thêm từ: " + e.getMessage();
        }
    }

    private static String deleteWord(String english) {
        try (Connection conn = DBConnector.getConnection()) {
            String sql = "DELETE FROM EV_Dictionary WHERE English = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, english);
                int affected = stmt.executeUpdate();
                if (affected > 0) return "Xóa từ thành công!";
                else return "Không tìm thấy từ!";
            }
        } catch (SQLException e) {
            return "Lỗi xóa từ: " + e.getMessage();
        }
    }
}

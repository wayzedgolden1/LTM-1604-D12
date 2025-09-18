<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   NETWORK PROGRAMMING
</h2>
<div align="center">
    <p align="center">
        <img src="docs/aiotlab_logo.png" alt="AIoTLab Logo" width="170"/>
        <img src="docs/fitdnu_logo.png" alt="AIoTLab Logo" width="180"/>
        <img src="docs/dnu_logo.png" alt="DaiNam University Logo" width="200"/>
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>
<h2 align="center">
   ## Ứng dụng hỗ trợ **tra cứu song ngữ (Anh ↔ Việt)**. 
</h2>


## 📖 1. Giới thiệu.

Ứng dụng được xây dựng theo mô hình **Client–Server**, với các đặc điểm chính:
- **Giao diện:** phát triển bằng **Java Swing**.  
- **Mạng:** trao đổi dữ liệu thông qua **TCP Socket**.  
- **Dữ liệu:** lưu trữ tập trung tại **Server** dưới dạng **SQL Database**.  

Mục tiêu của hệ thống là mang lại trải nghiệm tra cứu từ điển **nhanh chóng, chính xác và thân thiện**, tương tự như Google Dịch nhưng gọn nhẹ hơn.

### 🖥️ Giao diện.
- 2 ô nhập/xuất để hiển thị từ gốc và nghĩa dịch.  
- Nút chuyển đổi **Anh ↔ Việt** chỉ bằng một lần nhấn.  
- Thiết kế gọn gàng, dễ nhìn và trực quan.  

### ⚡ Các tính năng nổi bật.
- **Tra cứu song ngữ:**  
  - Hỗ trợ cả chế độ Anh→Việt và Việt→Anh.  
  - Kết quả có thể bao gồm **nhiều nghĩa** và ghi rõ **nguồn tham khảo**.  

- **Gợi ý từ gần đúng / sửa lỗi chính tả:**  
  - Ví dụ: người dùng nhập `enviroment` → hệ thống gợi ý `environment`.  
  - Áp dụng thuật toán **Levenshtein Distance** và tích hợp **AI NLP**.  

- **Ví dụ ngữ cảnh thực tế:**  
  - Mỗi từ vựng có thể kèm theo ví dụ câu (song ngữ nếu có).  
  - Dữ liệu tham khảo từ **StarDict, Wiktionary, Vdict** hoặc các bộ dữ liệu phụ.  

- **Lịch sử tra cứu:**  
  - Server lưu log các truy vấn để hỗ trợ phân tích và mở rộng.  
  - Có thể gắn **User ID** nếu cần quản lý người dùng.  

- **Phát âm & luyện nói (nâng cao):**  
  - **Phát âm:** Server trả file audio (TTS) → Client phát bằng Java Sound.  
  - **Luyện nói:** Client ghi âm giọng đọc → gửi lên Server để so sánh bằng **Speech-to-Text** hoặc API chấm điểm phát âm.  

---

## 🔧 2. Công nghệ sử dụng. 
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/) 
[![SQL](https://img.shields.io/badge/SQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)

---

## 📝 3. Nguồn dữ liệu từ điển.
Ứng dụng sử dụng các nguồn dữ liệu mở và đáng tin cậy:
- **EDICT:** bộ từ điển Anh–Việt mở.  
- **WordNet (Princeton):** cơ sở dữ liệu ngôn ngữ tiếng Anh. 

> Bộ dữ liệu ngôn ngữ (WordNet/EDICT) được chuẩn hóa và nạp vào cơ sở dữ liệu SQL, sau đó hệ thống truy vấn SQL để cung cấp cho AI khả năng tra cứu, phân tích và xử lý ngôn ngữ.

---
### [Khoá 16](./docs/projects/K16/README.md)

## 🚀4. Giao thức mạng
Hệ thống lựa chọn **TCP Socket** với mục đích:
- Đảm bảo dữ liệu chính xác (tra từ phải **đúng tuyệt đối**).  
- Dễ lập trình với **Java Socket / ServerSocket**.  
- Ổn định cho mô hình **Client–Server**.  

### 🔗 Quy trình hoạt động
1. Client nhập từ cần tra → gửi từ khóa đến Server qua TCP socket.  
2. Server nhận dữ liệu → tra cứu trong cơ sở dữ liệu SQL → trả kết quả.  
3. Client nhận và hiển thị kết quả trên GUI.  

---

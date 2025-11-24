<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
   ỨNG DỤNG HỖ TRỢ TRA CỨU SONG NGỮ ANH-VIỆT. 
</h2>
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

---

## 📖 1. Giới thiệu.

Ứng dụng được xây dựng theo mô hình **Client–Server**, với các đặc điểm chính:
- **Giao diện:** phát triển bằng **Java Swing**.  
- **Mạng:** trao đổi dữ liệu thông qua **TCP Socket**.  
- **Dữ liệu:** lưu trữ tập trung tại **Server** dưới dạng **SQL Database**.  

Mục tiêu của hệ thống là mang lại trải nghiệm tra cứu từ điển **nhanh chóng, chính xác và thân thiện**, tương tự như Google Dịch nhưng gọn nhẹ hơn.

### ⚡ Các tính năng nổi bật.
- **Tra cứu song ngữ.**  

- **Ví dụ ngữ cảnh thực tế.**  

- **Lịch sử tra cứu.**  

---

## 🔧 2. Công nghệ sử dụng. 
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)[![Swing](https://img.shields.io/badge/Swing-6DB33F?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/7/docs/api/javax/swing/package-summary.html)[![SQL Server](https://img.shields.io/badge/SQL%20Server-CC2927?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)](https://www.microsoft.com/en-us/sql-server)[![SSMS](https://img.shields.io/badge/SSMS-0078D7?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)](https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms)[![TCP Socket](https://img.shields.io/badge/TCP_Socket-FF6F00?style=for-the-badge&logo=internet-explorer&logoColor=white)](https://en.wikipedia.org/wiki/Internet_Protocol_suite#Transport_layer)[![Eclipse IDE](https://img.shields.io/badge/Eclipse_IDE-2C2255?style=for-the-badge&logo=eclipse&logoColor=white)](https://www.eclipse.org/)

---

## 🖼️ 3. Hình ảnh hệ thống.
<p align="center"><small><em>Giao diện người dùng</em></small></p>
<p align="center">
        <img src="docs/client1.png" alt="AIoTLab Logo" width="680"/>
        </p>
        <p align="center"><small><em>Giao diện bộ từ điển</em></small></p>
        <p align="center">
                <img src="docs/client2.png" alt="AIoTLab Logo" width="680"/>
                </p>
                <p align="center"><small><em>Giao diện quản lý</em></small></p>
                <p align="center">
                        <img src="docs/server1.png" alt="AIoTLab Logo" width="680"/>
                        </p>
                        <p align="center"><small><em>Giao diện lịch sử</em></small></p>
                        <p align="center">
                                <img src="docs/server2.png" alt="AIoTLab Logo" width="680"/>
                                </p>

                                ---

                                ## ⚙️ 4. Các bước cài đặt.

                                Yêu cầu hệ thống

                                - Java JDK 8 trở lên.
                                - SQL Server 2019/2017/2016.
                                - Eclipse hoặc IDE Java tương thích.
                                - Thư viện JDBC SQL Server.

                                Bước 1: Thiết lập cơ sở dữ liệu

                                1. Mở SQL Server Management Studio (SSMS).
                                2. New Query rồi copy file setup_database vào để tạo database và bảng mẫu.

                                Bước 2: Cấu hình dự án trong Eclipse

                                1. Mở Eclipse → File → Import → Existing Projects into Workspace.
                                2. Chọn thư mục src rồi thêm thư viện JDBC:
                                   - Click phải vào dự án → Build Path → Configure Build Path → Libraries → Add External JAR.
                                      - Chọn `lib/mssql-jdbc-13.2.0.jre8.jar`.

                                      Bước 3: Chạy Server và Client

                                      1. Chạy lần lượt 2 file `DictionaryServerGUI.java` và `DictionaryClient.java`.
                                      2. Kiểm tra log console, đảm bảo kết nối tới DB thành công.

                                      ---

                                      ## 📩 5. Liên hệ.
                                      - 📧 Email: wayzedgolden@gmail.com

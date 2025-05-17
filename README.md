# Tictoe Game

## Hướng dẫn chạy Socket.IO server

1. **Cài đặt Node.js**
   - Tải và cài đặt Node.js từ [nodejs.org](https://nodejs.org/)

2. **Cài đặt các thư viện**
   - Mở Command Prompt trong thư mục dự án
   - Chạy lệnh: `npm install`

3. **Tìm địa chỉ IP của máy tính**
   - Mở Command Prompt và chạy lệnh: `ipconfig`
   - Tìm dòng "IPv4 Address" (ví dụ: 192.168.1.5)

4. **Cập nhật SERVER_IP trong ứng dụng Android**
   - Mở file `app/src/main/java/com/example/tictoe/model/OnlineGameRepository.kt`
   - Cập nhật dòng:
     ```kotlin
     private const val SERVER_IP = "192.168.0.104" // ĐỔI THÀNH IP THẬT CỦA BẠN
     ```

5. **Chạy server**
   - Mở Command Prompt trong thư mục dự án
   - Chạy lệnh: `node server.js` hoặc `npm start`
   - Server sẽ chạy trên cổng 8887

6. **Kết nối từ ứng dụng Android**
   - Chạy ứng dụng Android trên thiết bị
   - Thiết bị phải kết nối cùng mạng Wi-Fi với máy tính chạy server
   - Vào chế độ chơi online và tạo game mới

Nhớ tạo folder chức năng của mình ròi làm nhé

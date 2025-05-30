# Tictoe Game

## Thông tin về game

Trò chơi Tic-tac-toe (caro 3x3) có thể chơi online qua mạng LAN sử dụng WebSocket.

## Tính năng

- Chơi trên cùng thiết bị
- Chơi với AI
- Chơi online qua mạng LAN
- Thống kê kết quả

## Cách chơi online

1. **Máy làm host:**
   - Vào mục "Play Online"
   - Chọn "Create Game"
   - Ứng dụng sẽ hiển thị IP của bạn
   - Đợi người chơi khác kết nối

2. **Máy tham gia game:**
   - Vào mục "Play Online"
   - Chọn "Join Game"
   - Nhập IP của máy host
   - Hoặc chọn từ danh sách máy đã phát hiện

## Yêu cầu

- Hai thiết bị phải kết nối cùng mạng Wi-Fi
- Cổng 8887 phải được cho phép trong tường lửa

## Cấu trúc project

- `network/TicTacToeServer.kt`: WebSocket server chạy trên thiết bị host
- `network/TicTacToeClient.kt`: WebSocket client kết nối đến server
- `model/OnlineGameRepository.kt`: Quản lý kết nối và trạng thái game
- `model/WebSocketMessage.kt`: Định nghĩa các tin nhắn giao tiếp

## Lưu ý

Ứng dụng sử dụng Java WebSocket và OkHttp để kết nối trực tiếp giữa các thiết bị mà không cần server trung gian.

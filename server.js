// Socket.IO server đơn giản để kiểm tra kết nối từ ứng dụng Android
const http = require('http');
const { Server } = require('socket.io');

// Tạo HTTP server
const server = http.createServer();
const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  },
  pingTimeout: 60000,
  pingInterval: 25000
});

// Log tất cả các sự kiện
io.engine.on('connection', (socket) => {
  console.log(`Socket transport: ${socket.transport.name}`);
});

// Xử lý kết nối từ client
io.on('connection', (socket) => {
  console.log(`Client connected: ${socket.id}`);
  
  // Log tất cả các sự kiện
  socket.onAny((eventName, ...args) => {
    console.log(`Event received: ${eventName}`, args);
  });
  
  socket.on('game_message', (message) => {
    console.log('Received game_message:', message);
    // Broadcast lại tin nhắn cho tất cả clients
    io.emit('game_message', message);
  });
  
  socket.on('disconnect', (reason) => {
    console.log(`Client disconnected: ${socket.id}, Reason: ${reason}`);
  });
  
  socket.on('error', (error) => {
    console.error('Socket error:', error);
  });
});

// Lắng nghe trên cổng 8887
const PORT = 8887;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`Socket.IO server is running on http://0.0.0.0:${PORT}`);
  console.log('Waiting for connections...');
}); 
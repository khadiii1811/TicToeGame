const http = require('http');
const { Server } = require('socket.io');

// Tạo HTTP server
const server = http.createServer((req, res) => {
  console.log("HTTP Request received:", req.url);
  res.writeHead(200);
  res.end('Socket.IO server is running');
});

const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"],
    transports: ['websocket', 'polling']
  },
  allowEIO3: true,
  pingTimeout: 60000,
  pingInterval: 25000
});

// Log tất cả các kết nối và sự kiện engine để debug
io.engine.on('connection', (socket) => {
  console.log(`Engine connection: transport=${socket.transport.name}`);
});

io.engine.on('connection_error', (err) => {
  console.log("CONNECTION ERROR:", err);
});

// Xử lý kết nối
io.on('connection', (socket) => {
  console.log(`Client connected: ${socket.id}`);
  
  // Log tất cả các sự kiện
  socket.onAny((eventName, ...args) => {
    console.log(`Event received: ${eventName}`, JSON.stringify(args));
  });
  
  socket.on('game_message', (message) => {
    console.log('Game message received:', JSON.stringify(message));
    
    // Echo lại message để test
    socket.emit('game_message', message);
  });
  
  socket.on('disconnect', (reason) => {
    console.log(`Client disconnected: ${socket.id}, Reason: ${reason}`);
  });
  
  socket.on('error', (error) => {
    console.error('Socket error:', error);
  });
});

// Hiển thị tất cả các địa chỉ IP local
function displayNetworkInfo() {
  console.log("Local IP addresses:");
  const nets = require('os').networkInterfaces();
  
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        console.log(`${name}: ${net.address}`);
      }
    }
  }
}

// Lắng nghe trên cổng 8887
const PORT = 8887;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`Socket.IO server is running on http://0.0.0.0:${PORT}`);
  displayNetworkInfo();
  console.log('Waiting for connections...');
}); 
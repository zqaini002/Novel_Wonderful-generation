// Node.js Express CORS中间件

module.exports = function(req, res, next) {
  // 允许的来源
  res.header('Access-Control-Allow-Origin', 'http://localhost:8081');
  
  // 允许的HTTP方法
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  
  // 允许的请求头
  res.header(
    'Access-Control-Allow-Headers',
    'Origin, X-Requested-With, Content-Type, Accept, Authorization'
  );
  
  // 允许携带凭证
  res.header('Access-Control-Allow-Credentials', 'true');
  
  // 对预检请求的处理
  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }
  
  next();
}; 
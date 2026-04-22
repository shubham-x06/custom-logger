// --- gRPC Implementation (Commented Out) ---
// const grpc = require('@grpc/grpc-js');
// const protoLoader = require('@grpc/proto-loader');
// const path = require('path');
// 
// const fs = require('fs');
// let PROTO_PATH = path.resolve(__dirname, '../../src/main/proto/log.proto');
// if (!fs.existsSync(PROTO_PATH)) {
//   PROTO_PATH = process.env.LOG_PROTO_PATH;
//   if (!PROTO_PATH) throw new Error('Proto file not found. Set LOG_PROTO_PATH env var to the absolute path of log.proto.');
// }
// 
// const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
//     keepCase: true,
//     longs: String,
//     enums: String,
//     defaults: true,
//     oneofs: true
// });
// 
// const logProto = grpc.loadPackageDefinition(packageDefinition).log;
// 
// class LoggerClient {
//     constructor(target = 'localhost:9090') {
//         this.client = new logProto.LogService(target, grpc.credentials.createInsecure());
//     }
// 
//     log(level, message, source) {
//         return new Promise((resolve, reject) => {
//             const payload = { level, message, source, timestamp: new Date().toISOString() };
//             this.client.Send(payload, (error, response) => {
//                 if (error) {
//                     reject(error);
//                 } else if (!response.success) {
//                     reject(new Error(response.error_message));
//                 } else {
//                     resolve(response);
//                 }
//             });
//         });
//     }
// }

// --- REST Implementation ---
const http = require('http');
const https = require('https');

class LoggerClient {
    constructor(baseUrl = 'https://custom-logger.onrender.com') {
        this.baseUrl = baseUrl.replace(/\/$/, ''); // Remove trailing slash
    }

    log(level, message, source) {
        return new Promise((resolve, reject) => {
            const payload = JSON.stringify({ level, message, source });
            const url = new URL('/api/log', this.baseUrl);
            const client = url.protocol === 'https:' ? https : http;

            const options = {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Content-Length': Buffer.byteLength(payload)
                }
            };

            const req = client.request(url, options, (res) => {
                let data = '';
                res.on('data', chunk => { data += chunk; });
                res.on('end', () => {
                    try {
                        const parsed = JSON.parse(data);
                        if (!parsed.success) {
                            reject(new Error(parsed.error || 'Unknown error from server'));
                        } else {
                            resolve(parsed);
                        }
                    } catch (e) {
                        reject(new Error(`Failed to parse response: ${data}`));
                    }
                });
            });

            req.on('error', (error) => {
                reject(error);
            });

            req.write(payload);
            req.end();
        });
    }
}

module.exports = LoggerClient;

// Quick test script when run directly
if (require.main === module) {
    const logger = new LoggerClient();
    logger.log('INFO', 'Hello from Node.js!', 'node-client')
        .then(() => console.log('Log successfully sent to Custom Logger Hub'))
        .catch(err => console.error('Failed to send log:', err.message));
}

const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const path = require('path');

const PROTO_PATH = path.resolve(__dirname, '../../src/main/proto/log.proto');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
});

const logProto = grpc.loadPackageDefinition(packageDefinition).log;

class LoggerClient {
    constructor(target = 'localhost:9090') {
        this.client = new logProto.LogService(target, grpc.credentials.createInsecure());
    }

    log(level, message, source) {
        return new Promise((resolve, reject) => {
            const payload = { level, message, source, timestamp: new Date().toISOString() };
            this.client.Send(payload, (error, response) => {
                if (error) {
                    reject(error);
                } else if (!response.success) {
                    reject(new Error(response.error_message));
                } else {
                    resolve(response);
                }
            });
        });
    }
}

module.exports = LoggerClient;

// Quick test script when run directly
if (require.main === module) {
    const logger = new LoggerClient();
    logger.log('INFO', 'Hello from Node.js via gRPC!', 'node-client')
        .then(() => console.log('Log successfully sent to Java Hub'))
        .catch(err => console.error('Failed to send log:', err.message));
}

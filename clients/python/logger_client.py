import grpc
import sys
import os
from datetime import datetime

# On-the-fly proto compilation
try:
    from grpc_tools import protoc
    proto_path = os.path.join(os.path.dirname(__file__), '../../src/main/proto/log.proto')
    proto_dir = os.path.dirname(proto_path)

    protoc.main((
        '',
        f'-I{proto_dir}',
        f'--python_out={os.path.dirname(__file__)}',
        f'--grpc_python_out={os.path.dirname(__file__)}',
        proto_path,
    ))
except ImportError:
    pass

import log_pb2
import log_pb2_grpc

class LoggerClient:
    def __init__(self, target='localhost:9090'):
        self.channel = grpc.insecure_channel(target)
        self.stub = log_pb2_grpc.LogServiceStub(self.channel)

    def log(self, level, message, source):
        timestamp = datetime.utcnow().isoformat()
        payload = log_pb2.LogEventPayload(
            level=level, 
            message=message, 
            source=source, 
            timestamp=timestamp
        )
        response = self.stub.Send(payload)
        if not response.success:
            raise Exception(f"Failed to log: {response.error_message}")
        return response

if __name__ == "__main__":
    client = LoggerClient()
    try:
        client.log("INFO", "Hello from Python via gRPC!", "python-client")
        print("Log successfully sent to Java Hub")
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)

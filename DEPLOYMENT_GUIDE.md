# Deployment Guide: Custom Logger

This guide explains how to deploy the Java backend of the logger, and how to distribute and install the client packages so other developers can easily integrate it into their projects.

## 1. Backend Server Deployment

The Java Spring Boot backend receives log events via HTTP (`:8080`) and gRPC (`:9090`). To deploy this seamlessly, a Dockerfile has been provided in the root directory.

### Quick Start with Docker
```bash
# 1. Build the Docker image
docker build -t custom-logger-server .

# 2. Run the container
# This exposes the HTTP API on port 8080 and the gRPC API on port 9090
docker run -d -p 8080:8080 -p 9090:9090 custom-logger-server
```

**Cloud Providers (Render, Heroku, AWS):**
Most modern cloud providers have a "Deploy from Dockerfile" feature. 
1. Link your GitHub repository to your cloud provider.
2. Select Docker as the environment.
3. The platform will automatically build the image using the provided `Dockerfile` and expose the necessary ports.

> **Note:** Make sure you note the provided IP/domain (e.g. `grpc.mydomain.com:9090`) to configure your clients.

---

## 2. Client Libraries Publishing

To allow other developers to use your client libraries, you need to publish them to standard package registries. 

### A. Node.js Client (NPM)

The Node package is located in `clients/node`. It has been configured with `package.json` for NPM publishing.

```bash
cd clients/node

# 1. Login to your NPM account (creates ~/.npmrc)
npm login

# 2. Publish the package publically
npm publish --access public
```
*End-users will then install it via:*
```bash
npm install node-client
```


### B. Python Client (PyPI)

The Python client in `clients/python` is configured using `setup.py`.

```bash
cd clients/python

# 1. Install build tools
pip install setuptools wheel twine

# 2. Build the package
python setup.py sdist bdist_wheel

# 3. Upload to PyPI (requires PyPI account tokens/credentials)
twine upload dist/*
```
*End-users will then install it via:*
```bash
pip install custom-logger-client
```

---

## 3. Usage for End Users
Once your clients are published and your server is live at `logger.example.com`, users simply import your module:

**Node.js Example:**
```javascript
const LoggerClient = require('node-client'); // or whatever you rename the NPM package to
const client = new LoggerClient('logger.example.com:9090');

client.log('INFO', 'Hello from the cloud!', 'node-client');
```

**Python Example:**
```python
from logger_client import LoggerClient # as defined in your python module

client = LoggerClient(target='logger.example.com:9090')
client.log('WARNING', 'Approaching limits', 'python-client')
```

# High-Performance Asynchronous Logger

A thread-safe, interface-driven logging framework built in Java. Designed to handle high-throughput logging without blocking the main application execution.

## 🚀 Key Features

* **Singleton Pattern:** Ensures a single, globally accessible logger instance with **Double-Checked Locking** for thread safety.
* **Strategy Pattern:** Supports pluggable output destinations (`ConsoleAppender`, `FileAppender`) that can be swapped at runtime.
* **Asynchronous Processing:** Implements th **Producer-Consumer pattern** using a `BlockingQueue` and a dedicated worker thread to decouple log submission from disk I/O.
* **Zero-Loss Design:** Handles file resource management using "Try-With-Resources" to prevent memory leaks.

## 🛠️ Architecture

* **Core:** `Logger` (Singleton Manager)
* **Interfaces:** `Appender` (Contract for output strategies)
* **Concurrency:** `AsyncAppender` (Wrapper for non-blocking performance)

## 💻 Usage

```java
// 1. Get the instance
Logger logger = Logger.getInstance();

// 2. Configure for Async File Logging
logger.setAppender(new AsyncAppender(new FileAppender("app.log")));

// 3. Log messages (Non-blocking)
logger.log(LogLevel.INFO, "High-performance logging initiated.");

🏗️ Build & Run

mvn clean compile exec:java -Dexec.mainClass="com.shubham.logger.App"
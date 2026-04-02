# Custom Logger - Project Context for LLMs

This document is designed to give you (an LLM) a comprehensive overview of the `custom-logger` project so you can quickly understand its architecture, patterns, and current state.

## 1. Project Overview
`custom-logger` is a Java-based, thread-safe, high-performance asynchronous logging framework. It is built to minimize blocking on the main application thread by offloading I/O operations (like writing to files or consoles) to a background worker thread.

## 2. Directory Structure & Build System
- **Build System**: Maven (`pom.xml` handles dependencies, packaging, etc.). Currently includes JUnit 3.8.1 for testing.
- **Source Directory**: `src/main/java/com/shubham/logger`
- **Key Packages**:
  - `appender/`: Contains output destinations (Consoles, Files) and async wrappers.
  - `formatter/`: Contains logic for structuring log messages.

## 3. Core Architecture & Design Patterns

The system relies heavily on established design patterns:

### A. Singleton Pattern (`Logger.java`)
The `Logger` class is a globally accessible Singleton manager. 
- It uses **Double-Checked Locking** with a `volatile` instance variable to ensure thread safety during initialization.
- It maintains a `CopyOnWriteArrayList` of `Appender` objects, permitting thread-safe dynamic addition of destinations.

### B. Strategy Pattern (Appenders & Formatters)
- **`Appender` (Interface)**: Defines a contract `void append(Loglevel level, String message)`. Pluggable destinations implement this.
- **`ConsoleAppender`**: Writes to `System.out` utilizing a provided `Formatter`.
- **`Formatter` (Interface)**: Defines `String format(...)`. 
- **`DetailedFormatter`**: Formats messages with timestamps (`yyyy-MM-dd HH:mm:ss`), thread names, and log levels.

### C. Producer-Consumer Pattern (`AsyncAppender.java`)
Implements non-blocking, high-performance logging.
- Wraps any given `Appender`.
- Maintains a `LinkedBlockingQueue<LogEvent>` (capacity 50).
- Uses a background daemon `Thread` to continuously `take()` events from the queue, optionally batch-draining them (`drainTo`), and passing them to the wrapped synchronous appender.
- If the queue is full, it currently prints an error to `System.err`.

### D. Enums (`Loglevel.java`)
Defines severity levels (`DEBUG(10)`, `INFO(20)`, `ERROR(30)`). Output is filtered by comparing severities.

## 4. Current Anomalies (CRITICAL)

**⚠️ `FileAppender.java` Corruption:**
Currently, `src/main/java/com/shubham/logger/appender/FileAppender.java` is broken. It does **not** contain Java code. Instead, it seems to have been accidentally overwritten with a C++ competitive programming solution (involving a `solve` function for hierarchical graph reduction with `#include <vector>`, etc.). 
- The `App.java` main class tries to instantiate `new FileAppender(...)`, which will fail to compile. 
- Any task addressing this codebase should recognize that `FileAppender` needs to be entirely rewritten in Java to implement the `Appender` interface utilizing `FileWriter` or `Files.writeString`.

## 5. Usage Example

```java
// Setting up the logger instance
Logger logger = Logger.getInstance();

// Suppose we implement FileAppender properly
logger.addAppender(new AsyncAppender(new FileAppender("app.log", new DetailedFormatter())));

// Log messages without blocking execution
logger.log(Loglevel.INFO, "High-performance logging initiated.");
```

## 6. How to Run
```bash
mvn clean compile exec:java -Dexec.mainClass="com.shubham.logger.App"
```
*(Note: This command will currently fail until the `FileAppender.java` anomaly is fixed.)*

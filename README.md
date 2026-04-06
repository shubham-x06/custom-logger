# 🚀 Custom Logger Protocol with Groq AI Debugging

An end-to-end logging ecosystem built in Java that features a high-throughput async logging framework, a Spring Boot log aggregation server, and an intelligent debugging suite powered by **Groq**.

The framework not only collects and stores logs efficiently but also proactively monitors them to catch errors and suggests code-level fixes using cutting-edge LLMs (LLaMA via GroqCloud).

## 🌟 Key Features

* **High-Performance Core:** Hand-rolled asynchronous, thread-safe logger utilizing the Producer-Consumer pattern and Double-Checked Locking.
* **Centralized Log Server:** A Spring Boot REST application that ingests logs from distributed sources over HTTP.
* **Intelligent Debug CLI:** A Picocli command-line tool equipped with a `--watch` mode to monitor your logs in real-time. When it spots an `ERROR`, it automatically streams the recent context to Groq to generate a root cause analysis!
* **Web Dashboard:** A Thymeleaf-based UI providing quick access to your raw logs alongside one-click AI analysis.

---

## 🏗️ Architecture & Class Structure

The project is structured into three main layers: The core logging engine, the server/dashboard layer, and the AI debugging suite.

```mermaid
classDiagram
    direction TB
    
    namespace LoggingEngine {
        class Logger {
            <<Singleton>>
            +getInstance() Logger
            +log(level, msg)
            +setAppender(Appender)
        }
        class Appender {
            <<Interface>>
            +append(level, msg)
        }
        class AsyncAppender {
            -BlockingQueue queue
            -Thread worker
            +append(level, msg)
        }
        class FileAppender
        class ConsoleAppender
    }
    
    Appender <|.. AsyncAppender
    Appender <|.. FileAppender
    Appender <|.. ConsoleAppender
    Logger --> "1" Appender: uses
    AsyncAppender --> "1" Appender: wraps

    namespace ServerAndDash {
        class LogServer {
            <<SpringBootApp>>
            +receiveLog(payload)
        }
        class DashboardController {
            <<RestController>>
            +analyze(payload)
            +getLogs()
            +getFiles()
        }
    }
    
    LogServer --> Logger: writes logs

    namespace AIDebuggingSuite {
        class DebugCommand {
            <<Picocli>>
            +run()
            -watchFile(path)
        }
        class GroqDebugAssistant {
            +analyze(path, lastN) DebugResult
        }
        class GroqClient {
            -apiUrl
            -apiKey
            +complete(system, user)
        }
    }

    DashboardController --> GroqDebugAssistant: calls
    DebugCommand --> GroqDebugAssistant: calls
    GroqDebugAssistant --> GroqClient: sends API payload
```

---

## 🔄 Automatic AI Debugging Workflow

One of the standout features of this project is the real-time AI debug pipeline. The CLI is capable of attaching itself to a log file via `WatchService`. When a log event triggers an `ERROR`, it captures the temporal context, filters the log tail, and immediately requests an action plan from the Groq API.

```mermaid
sequenceDiagram
    participant App as External Client App
    participant Server as Spring LogServer
    participant LogFile as ./logs/app.log
    participant CLI as DebugCommand (--watch)
    participant Groq as Groq API (LLaMA)

    App->>Server: POST /log {level: "ERROR", message: "NullPointerException"}
    note over Server, LogFile: Asynchronous non-blocking file write
    Server->>LogFile: Append line to file
    
    loop File Watcher
        CLI->>LogFile: Detect Modify Event
        CLI->>LogFile: Read Tail (last 50-100 lines)
    end

    note over CLI: If line contains "ERROR"<br>trigger semantic analysis!
    
    CLI->>Groq: Request Analysis {systemPrompt, userPrompt: logTail}
    note over Groq: groq-8b model calculates root cause
    Groq-->>CLI: Return JSON Result
    
    note over CLI: Parse & Present Formatted Result
    CLI->>CLI: Identify Root Cause
    CLI->>CLI: Explain the issue
    CLI->>CLI: Suggest Code Fix
    CLI->>CLI: Highlight Severity
```

---

## 🛠️ Usage & Setup

### 1. Configure the API Key
To utilize the AI debugging endpoints, inject your Groq API key (starts with `gsk_`) into your environment variables:

**Windows PowerShell:**
```powershell
$env:GROQ_API_KEY="your_groq_key_here"
```

**Linux/Mac:**
```bash
export GROQ_API_KEY="your_groq_key_here"
```

### 2. Build & Start the Server
Compile the project and start the Spring Boot web server to begin receiving logs and launching the Web UI.

```bash
mvn clean install
mvn spring-boot:run
```
Visit `http://localhost:8080/` to access the Debug Dashboard.

### 3. Run the CLI Debugger
You can analyze existing local files or run the command in `--watch` mode to monitor them dynamically.

**Analyze interactively:**
```bash
java -cp target/logger-cli.jar com.shubham.logger.cli.DebugCommand --file ./logs/app.log
```

**Real-time Watch mode:**
```bash
java -cp target/logger-cli.jar com.shubham.logger.cli.DebugCommand --file ./logs/app.log --watch
```
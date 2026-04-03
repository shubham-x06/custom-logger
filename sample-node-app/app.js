const readline = require('readline');
const LoggerClient = require('../clients/node/index.js'); // Uses the local node-client

const client = new LoggerClient('localhost:9090');
let minLevel = 'INFO';
const levels = { DEBUG: 1, INFO: 2, WARN: 3, ERROR: 4 };

let intervalId = null;

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  prompt: 'logger> '
});

// Important: the java backend only allows source headers in [java-client,node-client,python-client,go-client,cli]
const logIfLevelAllows = (level, message, source = 'node-client') => {
  const currentRisk = levels[minLevel.toUpperCase()] || 2;
  const targetRisk = levels[level.toUpperCase()] || 2;
  
  if (targetRisk >= currentRisk) {
    client.log(level.toUpperCase(), message, source)
      .then(() => {
        // Clear current line before printing so it doesn't mess up the prompt
        readline.clearLine(process.stdout, 0);
        readline.cursorTo(process.stdout, 0);
        console.log(`[Log Sent] ${level}: ${message}`);
        rl.prompt();
      })
      .catch(err => {
        readline.clearLine(process.stdout, 0);
        readline.cursorTo(process.stdout, 0);
        console.error(`[Log Failed] ${err.message}`);
        rl.prompt();
      });
  } else {
    readline.clearLine(process.stdout, 0);
    readline.cursorTo(process.stdout, 0);
    console.log(`[Log Skipped] ${level} is lower than current minimum level (${minLevel})`);
    rl.prompt();
  }
};

console.log("========================================");
console.log("   Node Sample App with Logger CLI");
console.log("========================================");
console.log("Commands available:");
console.log("  log <level> <message>   - Send a log manually");
console.log("  level <INFO|DEBUG|etc>  - Set minimum allowed log level");
console.log("  auto <start|stop>       - Produce automatic background logs");
console.log("  exit                    - Quit the app\n");

rl.prompt();

rl.on('line', (line) => {
  const parts = line.trim().split(' ');
  const cmd = parts[0].toLowerCase();
  
  if (cmd === 'exit' || cmd === 'quit') {
    if (intervalId) clearInterval(intervalId);
    console.log('Goodbye!');
    process.exit(0);
  } else if (cmd === 'level') {
    const newLevel = parts[1];
    if (newLevel && levels[newLevel.toUpperCase()]) {
      minLevel = newLevel.toUpperCase();
      console.log(`Minimum log level updated to: ${minLevel}`);
    } else {
      console.log(`Invalid level. Available levels: ${Object.keys(levels).join(', ')}`);
    }
  } else if (cmd === 'log') {
    const level = parts[1] ? parts[1].toUpperCase() : 'INFO';
    const message = parts.slice(2).join(' ') || 'Default test message';
    logIfLevelAllows(level, message, 'node-client');
  } else if (cmd === 'auto') {
    const action = parts[1];
    if (action === 'start') {
      if (!intervalId) {
        let counter = 1;
        intervalId = setInterval(() => {
          const autoLevel = counter % 5 === 0 ? 'ERROR' : (counter % 3 === 0 ? 'WARN' : 'INFO');
          logIfLevelAllows(autoLevel, `Auto-generated background task log #${counter}`, 'node-client');
          counter++;
        }, 3000);
        console.log('Started auto-logger in background (every 3 seconds)');
      } else {
        console.log('Auto-logger is already running.');
      }
    } else if (action === 'stop') {
      if (intervalId) {
        clearInterval(intervalId);
        intervalId = null;
        console.log('Stopped auto-logger.');
      } else {
        console.log('Auto-logger is not running.');
      }
    } else {
      console.log('Usage: auto <start|stop>');
    }
  } else if (cmd !== '') {
    console.log(`Unknown command: ${cmd}`);
  }
  
  // We don't always need to prompt here because logIfLevelAllows will prompt asynchronously.
  // We only prompt immediately if no async log was triggered.
  if (cmd !== 'log' && cmd !== 'exit' && cmd !== 'quit') {
    rl.prompt();
  }
});

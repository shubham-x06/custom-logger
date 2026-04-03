#!/usr/bin/env node

const { program } = require('commander');
const LoggerClient = require('./index.js');

program
  .name('logger-cli')
  .description('CLI to test the custom gRPC logger')
  .version('1.0.0');

program
  .requiredOption('-l, --level <level>', 'Log level (e.g., INFO, ERROR, WARN, DEBUG)')
  .requiredOption('-m, --message <message>', 'Log message')
  .option('-s, --source <source>', 'Log source', 'node-client')
  .option('-t, --target <target>', 'gRPC Logger Target', 'localhost:9090')
  .action((options) => {
    const logger = new LoggerClient(options.target);
    logger.log(options.level, options.message, options.source)
      .then((response) => {
        console.log('Log successfully sent!');
        console.log('Response:', response);
      })
      .catch((err) => {
        console.error('Failed to send log:', err.message);
        process.exit(1);
      });
  });

program.parse(process.argv);

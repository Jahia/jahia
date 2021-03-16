const env = require('./env');
const installLogsPrinter = require('cypress-terminal-report/src/installLogsPrinter');

module.exports = (on, config) => {
    env(on, config);
    installLogsPrinter(on)
    return config;
};

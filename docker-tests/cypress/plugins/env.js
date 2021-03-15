require('dotenv').config()

module.exports = (on, config) => {
    config.env.JAHIA_URL = process.env.JAHIA_URL;
    config.env.JAHIA_HOST = process.env.JAHIA_HOST;
    config.env.JAHIA_PORT = process.env.JAHIA_PORT;
    config.env.JAHIA_PASSWORD = process.env.JAHIA_PASSWORD;
    return config
}

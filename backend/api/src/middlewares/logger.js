/**
 * Request logger middleware for Express.js.
 * Logs incoming HTTP requests and response status codes with duration.
 */
const logger = (req, res, next) => {
  const start = Date.now();
  const { method, originalUrl } = req;
  const timestamp = new Date().toISOString();

  // Log incoming request
  console.log(`[${timestamp}] ➡️  ${method} ${originalUrl}`);

  // Listen to response finish event to log status code and response time
  res.on('finish', () => {
    const duration = Date.now() - start;
    const { statusCode } = res;

    let icon = '✅';
    if (statusCode >= 400 && statusCode < 500) {
      icon = '⚠️';
    } else if (statusCode >= 500) {
      icon = '❌';
    }

    console.log(
      `[${new Date().toISOString()}] ${icon} ${method} ${originalUrl} ${statusCode} (${duration}ms)`
    );
  });

  next();
};

module.exports = logger;

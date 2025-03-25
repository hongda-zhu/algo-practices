package middlewares

import (
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
	_config "github.com/thematrix97/leakyloki/config"
	_logger "github.com/thematrix97/leakyloki/logger"
	"go.uber.org/ratelimit"
	"go.uber.org/zap"
)

// Store a rate limiter for each tenant
var tenantLimiters sync.Map

func RateLimiterMiddleware(cfg *_config.Config) gin.HandlerFunc {
	return func(c *gin.Context) {
		tenantId := c.Request.Header.Get(_config.TENANT_ID_HEADER_NAME)
		rateLimitVal, _ := cfg.RateLimit(tenantId)
		if rateLimitVal > 0 { // Rate limiter has to be greater than 0 to be applied
			rateLimiter(c, tenantId, rateLimitVal, func() {
				c.Next()
			})
		} else {
			c.Next()
		}

	}
}

// Return a rate limiter per tenant and get the config if exists / otherwise default
func getTenantLimiter(tenant string, rateLimitVal int) ratelimit.Limiter {
	if tenant == "" {
		tenant = "default"
	}
	// If exists returns the rateLimiter
	if limiter, ok := tenantLimiters.Load(tenant); ok {
		return limiter.(ratelimit.Limiter)
	}
	// Sets the new rate limiter
	newLimiter := ratelimit.New(rateLimitVal)
	tenantLimiters.Store(tenant, newLimiter)
	return newLimiter
}

// RateLimiter encapsulates the rate-limiting logic
func rateLimiter(c *gin.Context, tenantId string, rateLimitVal int, callback func()) {
	// Define a timeout to get the token (default 10ms)
	timeout := 10 * time.Millisecond
	token := make(chan time.Time, 1)
	limiter := getTenantLimiter(tenantId, rateLimitVal)
	// Try to take a token asynchronously
	go func() {
		t := limiter.Take()
		token <- t
	}()

	select {
	case <-token:
		callback() // Call the provided function
	case <-time.After(timeout):
		c.JSON(http.StatusTooManyRequests, gin.H{"error": "Too Many Requests"})
		_logger.Logger.Info("TenantId exhausted his bucket", zap.String("TenantId", tenantId))
	}
}

package integration

import (
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"github.com/stretchr/testify/assert"
	_config "github.com/thematrix97/leakyloki/config"
	_logger "github.com/thematrix97/leakyloki/logger"
	_middlewares "github.com/thematrix97/leakyloki/middlewares"
)

func init() {
	_logger.InitLogger()          // Initialize logger to avoid nil pointer
	_logger.DisableLogsForTests() // Silence logs during tests
	gin.SetMode(gin.TestMode)     // Disable Gin debug output in tests
}

// clearRateLimiters is a helper to reset the state between tests
func clearRateLimiters() {
	// Since we can't access the private variable, we'll use time.Sleep as a workaround
	time.Sleep(100 * time.Millisecond)
}

func TestRateLimiterIntegration(t *testing.T) {
	// Reset state before test
	clearRateLimiters()

	// Test scenario 1: Basic rate limiting
	t.Run("Basic rate limiting test", func(t *testing.T) {
		// Create a specific config for this test with a clear rate limit
		testConfig := viper.New()
		testConfig.Set("limits_config.rate_limit", 10) // Higher rate limit to ensure first requests pass
		testConfig.Set("limits_config.max_query_range", "1h")
		testConfig.Set("limits_config.max_lookback_time", "1h")
		testConfig.Set("target.url", "http://localhost:8080")

		t.Logf("Testing with rate limit: 10 req/s")

		var testCfg _config.Config
		testConfig.Unmarshal(&testCfg)

		// Create a new router with the test config
		middleware := _middlewares.RateLimiterMiddleware(&testCfg)
		router := gin.New()
		router.GET("/test", middleware, func(c *gin.Context) {
			c.Status(http.StatusOK)
		})

		// Use a unique tenant ID for this test
		tenantID := "test-tenant-basic"

		// First request should pass
		w := httptest.NewRecorder()
		req, _ := http.NewRequest("GET", "/test", nil)
		req.Header.Set(_config.TENANT_ID_HEADER_NAME, tenantID)
		router.ServeHTTP(w, req)
		t.Logf("Request result: %d %s", w.Code, http.StatusText(w.Code))
		assert.Equal(t, http.StatusOK, w.Code, "First request should succeed with high rate limit")
	})

	// Test scenario 2: No rate limit
	t.Run("No rate limit test", func(t *testing.T) {
		// Create config with disabled rate limit
		testConfig := viper.New()
		testConfig.Set("limits_config.rate_limit", -1) // Disabled rate limit
		testConfig.Set("limits_config.max_query_range", "1h")
		testConfig.Set("limits_config.max_lookback_time", "1h")
		testConfig.Set("target.url", "http://localhost:8080")

		t.Logf("Testing with disabled rate limit (-1)")

		var testCfg _config.Config
		testConfig.Unmarshal(&testCfg)

		middleware := _middlewares.RateLimiterMiddleware(&testCfg)
		router := gin.New()
		router.GET("/test", middleware, func(c *gin.Context) {
			c.Status(http.StatusOK)
		})

		// Send multiple requests rapidly - all should succeed
		successCount := 0
		t.Log("Sending 5 requests in rapid succession")
		for i := 0; i < 5; i++ {
			w := httptest.NewRecorder()
			req, _ := http.NewRequest("GET", "/test", nil)
			req.Header.Set(_config.TENANT_ID_HEADER_NAME, "test-tenant-unlimited")
			router.ServeHTTP(w, req)
			if w.Code == http.StatusOK {
				successCount++
			}
			assert.Equal(t, http.StatusOK, w.Code, "Request should pass with disabled rate limit")
		}
		t.Logf("Results: %d successful requests", successCount)
	})

	// Test scenario 3: Multi-tenant independence
	t.Run("Multi-tenant test", func(t *testing.T) {
		// Create config with rate limit
		testConfig := viper.New()
		testConfig.Set("limits_config.rate_limit", 5) // Moderate rate limit
		testConfig.Set("limits_config.max_query_range", "1h")
		testConfig.Set("limits_config.max_lookback_time", "1h")
		testConfig.Set("target.url", "http://localhost:8080")

		t.Logf("Testing with rate limit: 5 req/s across multiple tenants")

		var testCfg _config.Config
		testConfig.Unmarshal(&testCfg)

		middleware := _middlewares.RateLimiterMiddleware(&testCfg)
		router := gin.New()
		router.GET("/test", middleware, func(c *gin.Context) {
			c.Status(http.StatusOK)
		})

		// Test with two different tenant IDs
		w1 := httptest.NewRecorder()
		req1, _ := http.NewRequest("GET", "/test", nil)
		req1.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant-a")
		router.ServeHTTP(w1, req1)

		w2 := httptest.NewRecorder()
		req2, _ := http.NewRequest("GET", "/test", nil)
		req2.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant-b")
		router.ServeHTTP(w2, req2)

		t.Logf("Tenant A result: %d, Tenant B result: %d", w1.Code, w2.Code)

		// Both should succeed since they're different tenants
		assert.Equal(t, http.StatusOK, w1.Code, "Request for tenant A should succeed")
		assert.Equal(t, http.StatusOK, w2.Code, "Request for tenant B should succeed")
	})

	// Test scenario 4: Rate limit exceeded
	t.Run("Rate limit exceeded test", func(t *testing.T) {
		// Create config with strict rate limit
		testConfig := viper.New()
		testConfig.Set("limits_config.rate_limit", 1) // Very strict limit
		testConfig.Set("limits_config.max_query_range", "1h")
		testConfig.Set("limits_config.max_lookback_time", "1h")
		testConfig.Set("target.url", "http://localhost:8080")

		t.Logf("Testing with very strict rate limit: 1 req/s")

		var testCfg _config.Config
		testConfig.Unmarshal(&testCfg)

		middleware := _middlewares.RateLimiterMiddleware(&testCfg)
		router := gin.New()
		router.GET("/test", middleware, func(c *gin.Context) {
			c.Status(http.StatusOK)
		})

		// Use a unique tenant ID
		tenantID := "test-tenant-strict"

		// First request should succeed
		w1 := httptest.NewRecorder()
		req1, _ := http.NewRequest("GET", "/test", nil)
		req1.Header.Set(_config.TENANT_ID_HEADER_NAME, tenantID)
		router.ServeHTTP(w1, req1)

		// Second request should be rate limited
		w2 := httptest.NewRecorder()
		req2, _ := http.NewRequest("GET", "/test", nil)
		req2.Header.Set(_config.TENANT_ID_HEADER_NAME, tenantID)
		router.ServeHTTP(w2, req2)

		t.Logf("First request result: %d, Second request result: %d", w1.Code, w2.Code)

		assert.Equal(t, http.StatusOK, w1.Code, "First request should succeed")
		assert.Equal(t, http.StatusTooManyRequests, w2.Code, "Second request should be rate limited")
	})
}

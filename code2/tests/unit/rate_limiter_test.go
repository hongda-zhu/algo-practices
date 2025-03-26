package unit

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"net/url"
	"sync"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	_config "github.com/thematrix97/leakyloki/config"
	_logger "github.com/thematrix97/leakyloki/logger"
	_middlewares "github.com/thematrix97/leakyloki/middlewares"
)

func init() {
	_logger.InitLogger()
	gin.SetMode(gin.TestMode) // Disable Gin debug output in tests

	// Set logger to silent mode for tests
	_logger.DisableLogsForTests()
}

// MockRateConfig only implements methods needed for rate limiter
type MockRateConfig struct {
	rateLimit int
}

func (m *MockRateConfig) RateLimit(tenantId string) (int, error) {
	return m.rateLimit, nil
}

// To satisfy the interface requirements, implement other methods
func (m *MockRateConfig) MaxQueryRange(tenantId string) (time.Duration, error) {
	return 0, nil
}

func (m *MockRateConfig) MaxLookbackTime(tenantId string) (time.Duration, error) {
	return 0, nil
}

func (m *MockRateConfig) TargetUrl() (*url.URL, error) {
	return nil, nil
}

func (m *MockRateConfig) Print() {
	fmt.Printf("Mock config: rate limit = %d\n", m.rateLimit)
}

func TestRateLimiterMiddleware(t *testing.T) {
	// Configure test mode for Gin
	gin.SetMode(gin.TestMode)

	tests := []struct {
		name            string
		rateLimit       int
		requestsToSend  int
		expectedSuccess int
		expectedFailure int
	}{
		{
			name:            "No rate limit",
			rateLimit:       -1,
			requestsToSend:  10,
			expectedSuccess: 10,
			expectedFailure: 0,
		},
		{
			name:            "Low rate limit",
			rateLimit:       3,
			requestsToSend:  10,
			expectedSuccess: 3, // First requests should pass
			expectedFailure: 7, // The rest should fail due to exceeding the limit
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			t.Logf("Testing rate limit: %d with %d requests", tt.rateLimit, tt.requestsToSend)

			// Configure middleware with mock config
			cfg := &MockRateConfig{rateLimit: tt.rateLimit}
			middleware := _middlewares.RateLimiterMiddleware(cfg)

			// Response counters
			successCount := 0
			failureCount := 0

			// Create a Gin router with the middleware
			router := gin.New()
			router.GET("/test", middleware, func(c *gin.Context) {
				c.Status(http.StatusOK)
			})

			// Send multiple requests quickly
			var wg sync.WaitGroup
			for i := 0; i < tt.requestsToSend; i++ {
				wg.Add(1)
				go func() {
					defer wg.Done()

					w := httptest.NewRecorder()
					req, _ := http.NewRequest("GET", "/test", nil)
					req.Header.Set(_config.TENANT_ID_HEADER_NAME, "test-tenant")

					router.ServeHTTP(w, req)

					if w.Code == http.StatusOK {
						successCount++
					} else if w.Code == http.StatusTooManyRequests {
						failureCount++
					}
				}()
			}

			// Wait for all goroutines to finish
			wg.Wait()

			t.Logf("Results: %d successful, %d failed requests", successCount, failureCount)

			// Verify results
			assert.LessOrEqual(t, successCount, tt.expectedSuccess, "Too many successful requests")
			if tt.rateLimit > 0 {
				assert.GreaterOrEqual(t, failureCount, tt.expectedFailure, "Not enough requests detected with rate limit exceeded")
			}
		})
	}
}

func TestMultipleTenants(t *testing.T) {
	// Configure middleware with low rate limit
	cfg := &MockRateConfig{rateLimit: 2}
	middleware := _middlewares.RateLimiterMiddleware(cfg)

	// Configure router
	router := gin.New()
	router.GET("/test", middleware, func(c *gin.Context) {
		c.Status(http.StatusOK)
	})

	// Test with different tenants
	tenant1Responses := []int{}
	tenant2Responses := []int{}

	// Send requests for tenant1
	for i := 0; i < 3; i++ {
		w := httptest.NewRecorder()
		req, _ := http.NewRequest("GET", "/test", nil)
		req.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant1")
		router.ServeHTTP(w, req)
		tenant1Responses = append(tenant1Responses, w.Code)
		time.Sleep(10 * time.Millisecond)
	}

	// Send requests for tenant2
	for i := 0; i < 3; i++ {
		w := httptest.NewRecorder()
		req, _ := http.NewRequest("GET", "/test", nil)
		req.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant2")
		router.ServeHTTP(w, req)
		tenant2Responses = append(tenant2Responses, w.Code)
		time.Sleep(10 * time.Millisecond)
	}

	t.Logf("Results: tenant1 responses: %v, tenant2 responses: %v", tenant1Responses, tenant2Responses)

	// Verify that each tenant has its own rate limiter
	assert.Contains(t, tenant1Responses, http.StatusOK, "Tenant1 should have at least one successful request")
	assert.Contains(t, tenant1Responses, http.StatusTooManyRequests, "Tenant1 should have at least one rejected request")
	assert.Contains(t, tenant2Responses, http.StatusOK, "Tenant2 should have at least one successful request")
	assert.Contains(t, tenant2Responses, http.StatusTooManyRequests, "Tenant2 should have at least one rejected request")
}

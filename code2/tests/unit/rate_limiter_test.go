package unit

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	_config "github.com/thematrix97/leakyloki/config"
	_logger "github.com/thematrix97/leakyloki/logger"
	_middlewares "github.com/thematrix97/leakyloki/middlewares"
)

func init() {
	// Initialize and disable logging for tests
	_logger.InitLogger()
	_logger.DisableLogsForTests() // This will prevent logs from cluttering test output
}

// MockConfig implements the ConfigInterface with controllable behavior for testing
type MockConfig struct {
	mock.Mock
}

func (m *MockConfig) RateLimit(tenantId string) (int, error) {
	args := m.Called(tenantId)
	return args.Int(0), args.Error(1)
}

func (m *MockConfig) MaxQueryRange(tenantId string) (time.Duration, error) {
	args := m.Called(tenantId)
	return args.Get(0).(time.Duration), args.Error(1)
}

func (m *MockConfig) MaxLookbackTime(tenantId string) (time.Duration, error) {
	args := m.Called(tenantId)
	return args.Get(0).(time.Duration), args.Error(1)
}

func (m *MockConfig) TargetUrl() (*url.URL, error) {
	args := m.Called()
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*url.URL), args.Error(1)
}

func (m *MockConfig) Print() {
	m.Called()
}

// TestRateLimiterMiddleware_ConfigInteraction verifies that the middleware correctly
// reads the tenant ID from the request and calls the config object appropriately
func TestRateLimiterMiddleware_ConfigInteraction(t *testing.T) {
	// Set up test environment
	gin.SetMode(gin.TestMode)

	// Create mock config object
	mockCfg := new(MockConfig)

	// Set expectation: RateLimit should be called with the specific tenant ID
	mockCfg.On("RateLimit", "test-tenant").Return(10, nil)

	// Create middleware with the mock config
	middleware := _middlewares.RateLimiterMiddleware(mockCfg)

	// Create test request with tenant ID header
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	req := httptest.NewRequest("GET", "/test", nil)
	req.Header.Set(_config.TENANT_ID_HEADER_NAME, "test-tenant")
	c.Request = req

	// Execute middleware
	middleware(c)

	// Verify config interactions
	mockCfg.AssertExpectations(t)
	mockCfg.AssertCalled(t, "RateLimit", "test-tenant")
}

// TestRateLimiterMiddleware_SkipsLimitingForNonPositiveRateLimit verifies that
// rate limiting is skipped when the config returns a non-positive value
func TestRateLimiterMiddleware_SkipsLimitingForNonPositiveRateLimit(t *testing.T) {
	// Set up test environment
	gin.SetMode(gin.TestMode)

	// Create mock config object
	mockCfg := new(MockConfig)

	// Set expectation: Return a negative rate limit value to indicate no limiting
	mockCfg.On("RateLimit", "test-tenant").Return(-1, nil)

	// Create middleware
	middleware := _middlewares.RateLimiterMiddleware(mockCfg)

	// Setup test router with the middleware and an endpoint handler that sets a flag
	nextCalled := false
	router := gin.New()
	router.GET("/test", middleware, func(c *gin.Context) {
		nextCalled = true
		c.Status(http.StatusOK)
	})

	// Make a request
	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/test", nil)
	req.Header.Set(_config.TENANT_ID_HEADER_NAME, "test-tenant")
	router.ServeHTTP(w, req)

	// Verify middleware behavior
	assert.True(t, nextCalled, "Next handler should be called when rate limit is not positive")
	assert.Equal(t, http.StatusOK, w.Code, "Response should be OK when rate limiting is skipped")
}

// TestRateLimiterMiddleware_MultipleTenants verifies that different tenants
// get different rate limiters with appropriate isolation
func TestRateLimiterMiddleware_MultipleTenants(t *testing.T) {
	// Set up test environment
	gin.SetMode(gin.TestMode)

	// Create mock config that returns different rate limits for different tenants
	mockCfg := new(MockConfig)
	mockCfg.On("RateLimit", "tenant-a").Return(1, nil)
	mockCfg.On("RateLimit", "tenant-b").Return(1, nil)

	// Create middleware
	middleware := _middlewares.RateLimiterMiddleware(mockCfg)

	// Create router for testing
	router := gin.New()
	router.GET("/test", middleware, func(c *gin.Context) {
		c.Status(http.StatusOK)
	})

	// Test tenant A - first request should succeed
	w1 := httptest.NewRecorder()
	req1, _ := http.NewRequest("GET", "/test", nil)
	req1.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant-a")
	router.ServeHTTP(w1, req1)
	assert.Equal(t, http.StatusOK, w1.Code, "First request for tenant A should succeed")

	// Test tenant B - should also succeed as it's a different tenant
	w2 := httptest.NewRecorder()
	req2, _ := http.NewRequest("GET", "/test", nil)
	req2.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant-b")
	router.ServeHTTP(w2, req2)
	assert.Equal(t, http.StatusOK, w2.Code, "First request for tenant B should succeed")

	// Second request for tenant A should be rate limited
	w3 := httptest.NewRecorder()
	req3, _ := http.NewRequest("GET", "/test", nil)
	req3.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant-a")
	router.ServeHTTP(w3, req3)
	assert.Equal(t, http.StatusTooManyRequests, w3.Code, "Second request for tenant A should be rate limited")

	// Second request for tenant B should also be rate limited
	w4 := httptest.NewRecorder()
	req4, _ := http.NewRequest("GET", "/test", nil)
	req4.Header.Set(_config.TENANT_ID_HEADER_NAME, "tenant-b")
	router.ServeHTTP(w4, req4)
	assert.Equal(t, http.StatusTooManyRequests, w4.Code, "Second request for tenant B should be rate limited")
}

// TestRateLimiterMiddleware_ErrorHandling verifies that the middleware correctly
// handles error cases from the config
func TestRateLimiterMiddleware_ErrorHandling(t *testing.T) {
	// Set up test environment
	gin.SetMode(gin.TestMode)

	// Create mock config that returns an error
	mockCfg := new(MockConfig)
	mockCfg.On("RateLimit", "test-tenant").Return(0, fmt.Errorf("config error"))

	// Create middleware
	middleware := _middlewares.RateLimiterMiddleware(mockCfg)

	// Setup test router with the middleware and an endpoint handler that sets a flag
	nextCalled := false
	router := gin.New()
	router.GET("/test", middleware, func(c *gin.Context) {
		nextCalled = true
		c.Status(http.StatusOK)
	})

	// Make a request
	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/test", nil)
	req.Header.Set(_config.TENANT_ID_HEADER_NAME, "test-tenant")
	router.ServeHTTP(w, req)

	// Verify error handling - in this case, we might expect the middleware to
	// log the error but still call Next() to avoid blocking the request pipeline
	assert.True(t, nextCalled, "Next should be called even when config returns an error")
}

// TestRateLimiterMiddleware_DefaultTenant verifies behavior when no tenant ID is provided
func TestRateLimiterMiddleware_DefaultTenant(t *testing.T) {
	// Set up test environment
	gin.SetMode(gin.TestMode)

	// Create mock config
	mockCfg := new(MockConfig)
	// Change this line to expect an empty string instead of "default"
	mockCfg.On("RateLimit", "").Return(10, nil)

	// Create middleware
	middleware := _middlewares.RateLimiterMiddleware(mockCfg)

	// Create test request with no tenant ID
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	req := httptest.NewRequest("GET", "/test", nil)
	// Intentionally not setting the tenant ID header
	c.Request = req

	// Execute middleware
	middleware(c)

	// Verify the middleware used an empty tenant ID
	mockCfg.AssertCalled(t, "RateLimit", "")
}

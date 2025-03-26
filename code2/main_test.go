package main

import (
	"net/http"
	"net/http/httptest"
	"net/http/httputil"
	"net/url"
	"strconv"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/spf13/viper"
	"github.com/stretchr/testify/assert"
	_config "github.com/thematrix97/leakyloki/config"
	_middlewares "github.com/thematrix97/leakyloki/middlewares"
)

func LoadConfig(configData map[string]interface{}) *viper.Viper {
	v := viper.New()
	for key, value := range configData {
		v.Set(key, value)
	}
	return v
}

func NewStubServer(responseBody string, statusCode int) *httptest.Server {
	return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(statusCode)
		w.Write([]byte(responseBody))
	}))
}

func TestProxyDisableRateLimit(t *testing.T) {
	// Loki Stub Backend
	stub := NewStubServer(`{"status":"ok"}`, http.StatusOK)
	defer stub.Close()

	rr := CreateTestResponseRecorder()

	// Config Loki Stub + deploy
	targetURL, _ := url.Parse(stub.URL)
	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	// Load custom configuration in Viper
	viperConfig := LoadConfig(map[string]interface{}{
		"limits_config.rate_limit":        -1,
		"limits_config.max_query_range":   "672h",
		"limits_config.max_lookback_time": "30d",
		"target.url":                      targetURL,
	})
	var cfg _config.Config
	viperConfig.Unmarshal(&cfg)

	// Start Router with RangeMiddleware
	gin.SetMode(gin.TestMode)
	router := gin.New()
	router.GET("/query", _middlewares.RangeTimeLimiterMiddleware(&cfg), func(c *gin.Context) {
		proxy.ServeHTTP(c.Writer, c.Request)
	})

	// Run HTTP Get against API
	req, _ := http.NewRequest("GET", "/query", nil)
	req.Header.Set("X-Scope-OrgID", "test-tenant") // Simulating a tenant
	startTimeEx := time.Now().AddDate(0, 0, -1)
	q := req.URL.Query()
	q.Set("time", strconv.FormatInt(startTimeEx.UnixNano(), 10))
	req.URL.RawQuery = q.Encode()

	router.ServeHTTP(rr, req)

	// Assert result
	assert.Equal(t, http.StatusOK, rr.Code)
	assert.Contains(t, rr.Body.String(), `"status":"ok"`)
}

// https://stackoverflow.com/questions/70717858/gin-reverse-proxy-tests-failing-for-interface-conversion-httptest-responsereco

type TestResponseRecorder struct {
	*httptest.ResponseRecorder
	closeChannel chan bool
}

func (r *TestResponseRecorder) CloseNotify() <-chan bool {
	return r.closeChannel
}

func (r *TestResponseRecorder) closeClient() {
	r.closeChannel <- true
}

func CreateTestResponseRecorder() *TestResponseRecorder {
	return &TestResponseRecorder{
		httptest.NewRecorder(),
		make(chan bool, 1),
	}
}

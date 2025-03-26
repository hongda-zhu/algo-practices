package middlewares

import (
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	_config "github.com/thematrix97/leakyloki/config"
	_utils "github.com/thematrix97/leakyloki/utils"
)

func isInRange(start time.Time, end time.Time, maxRange time.Duration) bool {
	return end.UnixNano()-start.UnixNano() <= maxRange.Nanoseconds()
}

// Computes the maxium lookback time and compares with T
func isWithinLookbackTime(t time.Time, lookback time.Duration) bool {
	nowNano := time.Now().UnixNano()
	return nowNano-t.UnixNano() <= lookback.Nanoseconds()
}

func getDefaultStartTime() time.Time {
	return time.Now().Add(-1 * time.Hour) //Remove 1h
}

func getDefaultEndTime() time.Time {
	return time.Now()
}

func RangeTimeLimiterMiddleware(cfg *_config.Config) gin.HandlerFunc {
	return func(c *gin.Context) {
		tenantId := c.Request.Header.Get(_config.TENANT_ID_HEADER_NAME)
		timeInput := c.Request.URL.Query().Get("time")
		startInput := c.Request.URL.Query().Get("start")
		endInput := c.Request.URL.Query().Get("end")
		maxLookback, errLb := cfg.MaxLookbackTime(tenantId)
		maxRange, errRange := cfg.MaxQueryRange(tenantId)

		// First check lookback time is not exceeded
		timeParsed, errTp := _utils.ParseLokiTimestamp(timeInput)
		var startInputParsed time.Time
		var errSp error
		if startInput != "" {
			startInputParsed, errSp = _utils.ParseLokiTimestamp(startInput)
		} else {
			startInputParsed = getDefaultStartTime()
		}

		if errLb == nil && errTp == nil && !isWithinLookbackTime(timeParsed, maxLookback) {
			c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "Exceeded Max Lookback time"})
			return
		}
		if errLb == nil && errSp == nil && !isWithinLookbackTime(startInputParsed, maxLookback) {
			c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "Exceeded Max Lookback Start timestamp"})
			return
		}

		// If time is setted, we can skip the duration check...

		if timeInput != "" {
			c.Next()
			return
		}

		var endInputParsed time.Time
		var errEp error
		if endInput != "" {
			endInputParsed, errEp = _utils.ParseLokiTimestamp(endInput)
		} else {
			endInputParsed = getDefaultEndTime()
		}

		if errSp == nil && errEp == nil && errRange == nil && !isInRange(startInputParsed, endInputParsed, maxRange) {
			c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "Exceeded Max Query Range"})
			return
		}

		c.Next()
	}
}

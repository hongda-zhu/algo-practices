package utils

import (
	"errors"
	"fmt"
	"regexp"
	"strconv"
	"time"
)

var timeUnits = map[string]time.Duration{
	"s": time.Second,
	"m": time.Minute,
	"h": time.Hour,
	"d": 24 * time.Hour,
	"w": 7 * 24 * time.Hour,
	"M": 30 * 24 * time.Hour,  // Approximate month
	"y": 365 * 24 * time.Hour, // Approximate year
}

func ParseCustomDuration(input string) (time.Duration, error) {
	re := regexp.MustCompile(`^(\d+)([smhdwMy])$`)
	matches := re.FindStringSubmatch(input)

	if len(matches) != 3 {
		return 0, fmt.Errorf("invalid duration format: %s", input)
	}

	value, err := strconv.Atoi(matches[1])
	if err != nil {
		return 0, err
	}

	unit, exists := timeUnits[matches[2]]
	if !exists {
		return 0, fmt.Errorf("unknown time unit: %s", matches[2])
	}

	return time.Duration(value) * unit, nil
}

func ParseLokiTimestamp(s string) (time.Time, error) {
	// Parse as a numeric Unix timestamp (nanoseconds)
	if ts, err := strconv.ParseInt(s, 10, 64); err == nil {
		// Return time using nanoseconds
		return time.Unix(0, ts), nil
	}

	// Parse as RFC3339Nano format
	if t, err := time.Parse(time.RFC3339Nano, s); err == nil {
		return t, nil
	}

	// Parse as RFC3339Nano format
	if t, err := time.Parse(time.RFC3339, s); err == nil {
		return t, nil
	}

	return time.Time{}, errors.New("invalid timestamp format")
}

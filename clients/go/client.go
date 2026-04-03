package logger

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

type Client struct {
	TargetURL string
}

func NewClient(targetURL string) *Client {
	return &Client{
		TargetURL: targetURL, // e.g., "http://localhost:8080/log"
	}
}

func (c *Client) Log(level, message, source string) error {
	payload := map[string]string{
		"level":     level,
		"message":   message,
		"source":    source,
		"timestamp": time.Now().UTC().Format(time.RFC3339),
	}

	body, err := json.Marshal(payload)
	if err != nil {
		return err
	}

	req, err := http.NewRequest("POST", c.TargetURL, bytes.NewBuffer(body))
	if err != nil {
		return err
	}
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("X-Logger-Source", source)

	client := &http.Client{Timeout: 5 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("server rejected log with status: %d", resp.StatusCode)
	}

	return nil
}

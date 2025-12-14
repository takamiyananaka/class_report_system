package com.xuegongbu.dto;

import lombok.Data;

/**
 * {
 *   "summary": {
 *     "timestamp_utc": "2025-12-11T08:33:23.729081Z",
 *     "frames": 10,
 *     "frames_processed": 10,
 *     "fps": 1.707728090274208,
 *     "total_count_sum": 187,
 *     "average_count": 18.7,
 *     "max_count": 20,
 *     "min_count": 18
 *   },
 *   "samples": [
 *     {
 *       "timestamp": "2025-12-11T08:33:20.271368Z",
 *       "count": 20
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:20.657884Z",
 *       "count": 18
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:21.056091Z",
 *       "count": 18
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:21.433054Z",
 *       "count": 18
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:21.818982Z",
 *       "count": 19
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:22.195044Z",
 *       "count": 19
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:22.581479Z",
 *       "count": 19
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:22.961845Z",
 *       "count": 19
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:23.343720Z",
 *       "count": 18
 *     },
 *     {
 *       "timestamp": "2025-12-11T08:33:23.729081Z",
 *       "count": 19
 *     }
 *   ],
 *   "sample_url": "/samples/1247505953717374976/sample_1247505953717374976.jpg"
 * }
 */
@Data
public class CountResponse {
    private CountSummary summary;
    private CountSample[] samples;
    private String sampleUrl;
    @Data
    public static class CountSummary {
        private String timestampUtc;
        private int frames;
        private int framesProcessed;
        private double fps;
        private int totalCountSum;
        private double averageCount;
        private int maxCount;
        private int minCount;

    }
    @Data
    public static class CountSample {
        private String timestamp;
        private int count;
    }

}

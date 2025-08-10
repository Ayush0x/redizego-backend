package com.redizego.redi_ze_go.strategies.impl;

import com.redizego.redi_ze_go.strategies.NetworkStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Network quality assessment strategy for ride-hailing operations.
 * 
 * This strategy implementation evaluates network connection quality based on
 * download and upload speeds to determine if the connection is suitable for
 * real-time ride operations, including GPS tracking, communication, and
 * status updates.
 * 
 * Network Quality Thresholds:
 * - Minimum Download Speed: 256 Kbps
 * - Minimum Upload Speed: 512 Kbps
 * 
 * Quality Assessment Logic:
 * - Strong Network: Both speeds meet or exceed minimum thresholds
 * - Weak Network: Either speed falls below minimum requirements
 * 
 * Key Use Cases:
 * - Driver app connectivity validation
 * - Rider app performance assessment
 * - Real-time tracking capability verification
 * - Service quality optimization decisions
 * 
 * Business Impact:
 * - Ensures reliable GPS tracking during rides
 * - Maintains communication quality between riders and drivers
 * - Supports real-time status updates and notifications
 * - Enables optimal user experience through adequate connectivity
 * 
 * Technical Considerations:
 * - Download speed affects map loading, notifications, and data retrieval
 * - Upload speed crucial for location updates and status reporting
 * - Thresholds set to ensure minimum acceptable user experience
 * - Can be used for adaptive feature enabling/disabling
 * 
 * Potential Enhancements:
 * - Latency measurements for real-time responsiveness
 * - Network stability assessment over time
 * - Adaptive thresholds based on device capabilities
 * - Geographic location-based adjustments
 * 
 * @author Ayush Sharma
 * @version 1.0
 * @since 1.0
 * 
 * @see NetworkStrategy
 * @see com.redizego.redi_ze_go.strategies.RideStrategyManager
 * 
 * @implNote Speed values are expected in Kbps (Kilobits per second)
 * @implNote Currently uses fixed thresholds; future versions may support dynamic adjustment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkStrategyImpl implements NetworkStrategy {
    
    /** Minimum download speed required for acceptable service quality (in Kbps) */
    private static final double MIN_DOWNLOAD_SPEED_KBPS = 256.0;
    
    /** Minimum upload speed required for reliable location updates (in Kbps) */
    private static final double MIN_UPLOAD_SPEED_KBPS = 512.0;
    
    /**
     * Evaluates network connection strength based on download and upload speeds.
     * 
     * This method assesses whether the current network connection is adequate
     * for reliable ride-hailing operations. It checks both download and upload
     * speeds against predefined thresholds to ensure quality service delivery.
     * 
     * Assessment Criteria:
     * - Download Speed: Must be ≥ 256 Kbps for data retrieval, maps, notifications
     * - Upload Speed: Must be ≥ 512 Kbps for location updates, status reporting
     * 
     * Network Quality Determination:
     * - Strong Network: Both speeds meet minimum requirements
     * - Weak Network: Either speed falls below threshold
     * 
     * @param downloadSpeed Current download speed in Kilobits per second (Kbps)
     * @param uploadSpeed Current upload speed in Kilobits per second (Kbps)
     * @return true if network is strong enough for ride operations, false if weak
     * @throws IllegalArgumentException if either speed parameter is null or negative
     * 
     * @implNote Method name suggests inverse logic: returns true for STRONG network
     * @implNote Both parameters must meet thresholds for positive result
     * 
     * @example
     * NetworkStrategy strategy = new NetworkStrategyImpl();
     * boolean isGoodConnection = strategy.isNetworkWeak(300.0, 600.0); // returns true (strong)
     * boolean isPoorConnection = strategy.isNetworkWeak(200.0, 400.0); // returns false (weak)
     */
    @Override
    public Boolean isNetworkWeak(Double downloadSpeed, Double uploadSpeed) {
        if (downloadSpeed == null || uploadSpeed == null) {
            log.error("Cannot assess network quality: speed parameters cannot be null");
            throw new IllegalArgumentException("Download and upload speeds must be provided");
        }
        
        if (downloadSpeed < 0 || uploadSpeed < 0) {
            log.error("Cannot assess network quality: speeds cannot be negative - Download: {}, Upload: {}", 
                    downloadSpeed, uploadSpeed);
            throw new IllegalArgumentException("Speed values must be non-negative");
        }
        
        // Check if both speeds meet minimum requirements
        boolean downloadMeetsThreshold = downloadSpeed >= MIN_DOWNLOAD_SPEED_KBPS;
        boolean uploadMeetsThreshold = uploadSpeed >= MIN_UPLOAD_SPEED_KBPS;
        boolean isNetworkStrong = downloadMeetsThreshold && uploadMeetsThreshold;
        
        // Log network assessment details
        log.debug("Network quality assessment - Download: {:.1f} Kbps (min: {:.0f}), " +
                "Upload: {:.1f} Kbps (min: {:.0f}), Quality: {}", 
                downloadSpeed, MIN_DOWNLOAD_SPEED_KBPS, 
                uploadSpeed, MIN_UPLOAD_SPEED_KBPS,
                isNetworkStrong ? "STRONG" : "WEAK");
        
        if (!isNetworkStrong) {
            if (!downloadMeetsThreshold) {
                log.warn("Download speed below threshold: {:.1f} Kbps < {:.0f} Kbps", 
                        downloadSpeed, MIN_DOWNLOAD_SPEED_KBPS);
            }
            if (!uploadMeetsThreshold) {
                log.warn("Upload speed below threshold: {:.1f} Kbps < {:.0f} Kbps", 
                        uploadSpeed, MIN_UPLOAD_SPEED_KBPS);
            }
        }
        
        // Note: Method name suggests inverse logic - returning true means strong network
        return isNetworkStrong;
    }
    
    /**
     * Gets the minimum download speed threshold.
     * 
     * @return Minimum download speed in Kbps
     */
    public static double getMinDownloadSpeedThreshold() {
        return MIN_DOWNLOAD_SPEED_KBPS;
    }
    
    /**
     * Gets the minimum upload speed threshold.
     * 
     * @return Minimum upload speed in Kbps
     */
    public static double getMinUploadSpeedThreshold() {
        return MIN_UPLOAD_SPEED_KBPS;
    }
    
    /**
     * Provides a quality assessment summary for the given speeds.
     * 
     * @param downloadSpeed Download speed in Kbps
     * @param uploadSpeed Upload speed in Kbps
     * @return String description of network quality
     */
    public String getNetworkQualityDescription(Double downloadSpeed, Double uploadSpeed) {
        if (downloadSpeed == null || uploadSpeed == null) {
            return "Unknown - speeds not provided";
        }
        
        boolean isStrong = isNetworkWeak(downloadSpeed, uploadSpeed);
        
        if (isStrong) {
            return String.format("Strong connection (↓%.1f/↑%.1f Kbps)", downloadSpeed, uploadSpeed);
        } else {
            return String.format("Weak connection (↓%.1f/↑%.1f Kbps) - below minimum requirements", 
                    downloadSpeed, uploadSpeed);
        }
    }
}

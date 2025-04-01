package at.db.starlink.watchtower.network;

import at.bernhardangerer.speedtestclient.SpeedtestController;
import at.bernhardangerer.speedtestclient.exception.SpeedtestException;
import at.bernhardangerer.speedtestclient.model.SpeedtestResult;
import at.db.starlink.watchtower.network.model.Speedtest;
import at.db.starlink.watchtower.network.repository.SpeedtestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;

@Slf4j
@Service
public class SpeedtestService {
    private final SpeedtestRepository speedtestRepository;

    @Value("${speedtest.disposeMaxRetries:10}")
    private int maxDisposeRetries;

    @Value("${speedtest.disposeSleepTime:10000}")
    private int disposeSleepTime;

    public SpeedtestService(SpeedtestRepository speedtestRepository) {
        this.speedtestRepository = speedtestRepository;
    }

    public Speedtest getSpeedtest(){
        log.debug("Start getSpeedtest()");
        Speedtest speedtest = null;
        try{
            SpeedtestResult speedtestResult = SpeedtestController.runSpeedTest();
            speedtest = mapSpeedtestResultToSpeedtest(speedtestResult);
        } catch (SpeedtestException e) {
            log.error("Error in getSpeedtest()");
        }
        log.debug("End getSpeedtest(): {}", speedtest);
        return speedtest;
    }

    public void disposeSpeedtest(){
        log.debug("Start disposeSpeedtest()");
        Speedtest speedtest = null;

        for (int attempt = 1; attempt <= maxDisposeRetries; attempt++) {
            speedtest = getSpeedtest();
            if (speedtest != null) {
                break;
            }
            log.debug("disposeSpeedtest(): Attempt {} of {} failed, retrying...", attempt, maxDisposeRetries);

           try {
                Thread.sleep(disposeSleepTime); // Warte 1 Sekunde, bevor der nächste Versuch gestartet wird
           } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Setzt das Interrupt-Flag neu
                log.error("disposeSpeedtest(): Interrupted while waiting", e);
                return;
           }
        }

        if (speedtest == null) {
            log.debug("disposeSpeedtest(): Max retries ({}) reached, aborting", maxDisposeRetries);
        }else{
            speedtestRepository.save(speedtest);
        }
        log.debug("End disposeSpeedtest()");
    }

    private static Speedtest mapSpeedtestResultToSpeedtest(SpeedtestResult speedtestResult){
        Speedtest speedtest = new Speedtest();

        speedtest.setTimestamp(ZonedDateTime.now());

        // Client
        speedtest.setClientIp(speedtestResult.getClient().getIp());
        speedtest.setClientLatitude(speedtestResult.getClient().getLat());
        speedtest.setClientLongitude(speedtestResult.getClient().getLon());
        speedtest.setClientIsp(speedtestResult.getClient().getIsp());
        speedtest.setClientIspRating(speedtestResult.getClient().getIsprating());
        speedtest.setClientCountry(speedtestResult.getClient().getCountry());

        // Server
        speedtest.setServerLatitude(speedtestResult.getServer().getLat());
        speedtest.setServerLongitude(speedtestResult.getServer().getLon());
        speedtest.setServerName(speedtestResult.getServer().getName());
        speedtest.setServerCountry(speedtestResult.getServer().getCountry());
        speedtest.setServerCountryCode(speedtestResult.getServer().getCountryCode());
        speedtest.setServerSponsor(speedtestResult.getServer().getSponsor());
        speedtest.setServerId(speedtestResult.getServer().getId());
        speedtest.setServerHost(speedtestResult.getServer().getHost());

        // Latency
        speedtest.setLatency(speedtestResult.getLatency().getLatency());
        speedtest.setLatencyDistance(speedtestResult.getLatency().getDistance());

        // Download
        speedtest.setDownloadRateInMbps(speedtestResult.getDownload().getRateInMbps());
        speedtest.setDownloadBytes(speedtestResult.getDownload().getBytes());
        speedtest.setDownloadDurationInMs(speedtestResult.getDownload().getDurationInMs());

        // Upload
        speedtest.setUploadRateInMbps(speedtestResult.getUpload().getRateInMbps());
        speedtest.setUploadBytes(speedtestResult.getUpload().getBytes());
        speedtest.setUploadDurationInMs(speedtestResult.getUpload().getDurationInMs());

        return speedtest;
    }

}

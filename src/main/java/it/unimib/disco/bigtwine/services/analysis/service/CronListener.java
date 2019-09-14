package it.unimib.disco.bigtwine.services.analysis.service;

import it.unimib.disco.bigtwine.commons.messaging.CronTaskEvent;
import it.unimib.disco.bigtwine.services.analysis.client.CronSchedulerServiceClient;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisSettingConstants;
import it.unimib.disco.bigtwine.services.analysis.domain.AnalysisStatusHistory;
import it.unimib.disco.bigtwine.services.analysis.domain.CronEntryInfo;
import it.unimib.disco.bigtwine.services.analysis.domain.enumeration.AnalysisStatus;
import it.unimib.disco.bigtwine.services.analysis.messaging.CronTaskConsumerChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;

@Service
public class CronListener {

    private final static String CRON_DEFAULT_GROUP = "default";
    private final static String CRON_STOP_ANALYSES = "stop_analyses";

    @Value("${eureka.instance.appname}")
    private String serviceName;
    private final CronEntryRegistry cronEntryRegistry;
    private final AnalysisService analysisService;

    public CronListener(
        CronEntryRegistry cronEntryRegistry,
        AnalysisService analysisService) {
        this.cronEntryRegistry = cronEntryRegistry;
        this.analysisService = analysisService;
    }

    @PostConstruct
    public void registerCronEntries() {
        CronEntryInfo entry = new CronEntryInfo();
        entry.setService(serviceName);
        entry.setGroup(CRON_DEFAULT_GROUP);
        entry.setName(CRON_STOP_ANALYSES);
        entry.setActive(true);
        entry.setParallelism(0);
        entry.setCronExpr("0 */2 * * * *");

        this.cronEntryRegistry.registerCronEntry(entry);
    }

    @StreamListener(CronTaskConsumerChannel.CHANNEL)
    public void onCronFire(CronTaskEvent event) {
        if (!(event.getService() != null && event.getService().equals(serviceName))) {
            return;
        }

        if (event.getGroup().equals(CRON_DEFAULT_GROUP) && event.getName().equals(CRON_STOP_ANALYSES)) {
            this.stopAnalyses(event.getTask(), event.getTasksCount());
        }
    }

    /**
     * Ferma le analisi in esecuzione che hanno superato il limite massimo di esecuzione
     *
     * @param task Numero task corrente
     * @param taskCount Numero task totali
     */
    private void stopAnalyses(int task, int taskCount) {
        long totalCount = this.analysisService.countByStatus(AnalysisStatus.STARTED);
        int pageSize = (int)Math.ceil(totalCount / (double)taskCount);
        Pageable page = PageRequest.of(task, pageSize, Sort.by(Sort.Direction.ASC, "id"));

        this.analysisService.findByStatus(AnalysisStatus.STARTED, page).forEach((analysis) -> {
            Object maxExeTimeO = analysis.getSettings().get(AnalysisSettingConstants.MAX_EXECUTION_TIME);

            long maxExeTime = AnalysisSettingConstants.DEFAULT_MAX_EXECUTION_TIME;
            if (maxExeTimeO instanceof Long) {
                maxExeTime = (Long)maxExeTimeO;
            } else if (maxExeTimeO instanceof Integer) {
                maxExeTime = ((Integer)maxExeTimeO).longValue();
            }

            if (maxExeTime > 0) {
                int historySize = analysis.getStatusHistory().size();
                Instant endDate = null;

                if (historySize > 0) {
                    AnalysisStatusHistory history = analysis.getStatusHistory().get(historySize - 1);
                    if (history.getNewStatus() == AnalysisStatus.STARTED) {
                        endDate = history.getDate().plusSeconds(maxExeTime);
                    }
                }

                if (endDate == null) {
                    endDate = analysis.getCreateDate();
                }

                if (endDate.isBefore(Instant.now())) {
                    this.analysisService.requestStatusChange(
                        analysis,
                        AnalysisStatus.CANCELLED,
                        false
                    );
                }
            }
        });
    }
}

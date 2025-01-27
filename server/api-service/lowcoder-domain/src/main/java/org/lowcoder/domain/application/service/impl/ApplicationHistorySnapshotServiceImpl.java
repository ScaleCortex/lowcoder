package org.lowcoder.domain.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.lowcoder.domain.application.model.ApplicationHistorySnapshot;
import org.lowcoder.domain.application.model.ApplicationHistorySnapshotTS;
import org.lowcoder.domain.application.repository.ApplicationHistoryArchivedSnapshotRepository;
import org.lowcoder.domain.application.repository.ApplicationHistorySnapshotRepository;
import org.lowcoder.domain.application.service.ApplicationHistorySnapshotService;
import org.lowcoder.sdk.exception.BizError;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.lowcoder.sdk.exception.BizError.INVALID_HISTORY_SNAPSHOT;
import static org.lowcoder.sdk.util.ExceptionUtils.deferredError;
import static org.lowcoder.sdk.util.ExceptionUtils.ofException;

@RequiredArgsConstructor
@Service
public class ApplicationHistorySnapshotServiceImpl implements ApplicationHistorySnapshotService {

    private final ApplicationHistorySnapshotRepository repository;
    private final ApplicationHistoryArchivedSnapshotRepository repositoryArchived;

    @Override
    public Mono<Boolean> createHistorySnapshot(String applicationId, Map<String, Object> dsl, Map<String, Object> context, String userId) {
        ApplicationHistorySnapshotTS applicationHistorySnapshotTS = new ApplicationHistorySnapshotTS();
        applicationHistorySnapshotTS.setApplicationId(applicationId);
        applicationHistorySnapshotTS.setDsl(dsl);
        applicationHistorySnapshotTS.setContext(context);
        return repository.save(applicationHistorySnapshotTS)
                .thenReturn(true)
                .onErrorReturn(false);
    }

    @Override
    public Mono<List<ApplicationHistorySnapshotTS>> listAllHistorySnapshotBriefInfo(String applicationId, String compName, String theme, Instant from, Instant to, PageRequest pageRequest) {
        return repository.findAllByApplicationId(applicationId, compName, theme, from, to, pageRequest.withSort(Direction.DESC, "id"))
                .collectList()
                .onErrorMap(Exception.class, e -> ofException(BizError.FETCH_HISTORY_SNAPSHOT_FAILURE, "FETCH_HISTORY_SNAPSHOT_FAILURE"));
    }

    @Override
    public Mono<List<ApplicationHistorySnapshot>> listAllHistorySnapshotBriefInfoArchived(String applicationId, String compName, String theme, Instant from, Instant to, PageRequest pageRequest) {
        return repositoryArchived.findAllByApplicationId(applicationId, compName, theme, from, to, pageRequest.withSort(Direction.DESC, "id"))
                .collectList()
                .onErrorMap(Exception.class, e -> ofException(BizError.FETCH_HISTORY_SNAPSHOT_FAILURE, "FETCH_HISTORY_SNAPSHOT_FAILURE"));
    }

    @Override
    public Mono<Long> countByApplicationId(String applicationId) {
        return repository.countByApplicationId(applicationId)
                .onErrorMap(Exception.class,
                        e -> ofException(BizError.FETCH_HISTORY_SNAPSHOT_COUNT_FAILURE, "FETCH_HISTORY_SNAPSHOT_COUNT_FAILURE"));
    }


    @Override
    public Mono<ApplicationHistorySnapshotTS> getHistorySnapshotDetail(String historySnapshotId) {
        return repository.findById(historySnapshotId)
                .switchIfEmpty(deferredError(INVALID_HISTORY_SNAPSHOT, "INVALID_HISTORY_SNAPSHOT", historySnapshotId));
    }


    @Override
    public Mono<ApplicationHistorySnapshot> getHistorySnapshotDetailArchived(String historySnapshotId) {
        return repositoryArchived.findById(historySnapshotId)
                .switchIfEmpty(deferredError(INVALID_HISTORY_SNAPSHOT, "INVALID_HISTORY_SNAPSHOT", historySnapshotId));
    }
}

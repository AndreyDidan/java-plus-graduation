package ru.practicum.controller;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.services.recommendation.RecommendationService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto eventsRequestProto,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getSimilarEvents(eventsRequestProto)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка  в getSimilarEvents: {}", e.getMessage(), e);
            responseObserver.onError(
                    new RuntimeException("Ошибка обработки getSimilarEvents")
            );
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            recommendationService.getRecommendationsForUser(request)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Непредвиденная ошибка  в getRecommendationsForUser: {}", e.getMessage(), e);
            responseObserver.onError(
                    new RuntimeException("Ошибка обработки getRecommendationsForUser")
            );
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Получен запрос getInteractionsCount с eventIds = {}", request.getEventIdList());
            recommendationService.getInteractionsCount(request)
                    .forEach(responseObserver::onNext);
            log.info("Получен запрос на получение количества действий по событиям. Этап 2");
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("Непредвиденная ошибка StatusRuntimeException в getSimilarEvents: {}", e.getMessage(), e);

        } catch (Exception e) {
            log.error("Непредвиденная ошибка  в getSimilarEvents: {}", e.getMessage(), e);
            responseObserver.onError(
                    new RuntimeException("Ошибка обработки GetSimilarEvents")
            );
        }
    }
}

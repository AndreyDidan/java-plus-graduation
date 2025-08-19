package ru.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.services.RecommendationService;

import java.util.List;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsController call getRecommendationsForUser for request = {}", request);
        List<RecommendedEventProto> recommendedEvents = recommendationService.generateRecommendationsForUser(request);
        for (RecommendedEventProto event : recommendedEvents) {
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsController call getSimilarEvents for request = {}", request);
        List<RecommendedEventProto> recommendedEvents = recommendationService.getSimilarEvents(request);
        for (RecommendedEventProto event : recommendedEvents) {
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("RecommendationsController call getInteractionsCount for request = {}", request);
        List<RecommendedEventProto> recommendedEvents = recommendationService.getInteractionsCount(request);
        for (RecommendedEventProto event : recommendedEvents) {
            responseObserver.onNext(event);
        }
        responseObserver.onCompleted();
    }
}
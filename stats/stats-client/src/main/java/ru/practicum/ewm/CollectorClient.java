package ru.practicum.ewm;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@Component
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public void sendUserAction(long userId, long eventId, ActionTypeProto typeProto) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(typeProto)
                .setTimestamp(getTimestamp())
                .build();
        Empty empty = client.collectUserAction(request);
    }

    private Timestamp getTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setNanos(now.getNano())
                .setSeconds(now.getEpochSecond())
                .build();
    }
}
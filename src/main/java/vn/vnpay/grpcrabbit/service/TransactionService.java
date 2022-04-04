package vn.vnpay.grpcrabbit.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionRequest;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionResponse;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionServiceGrpc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
public class TransactionService extends TransactionServiceGrpc.TransactionServiceImplBase {

    private final String QUEUE_NAME = "grpc-rabbit";

    @Override
    public void getTransaction(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver) {
        log.info("\n[x] Request from client: {}", request);
        log.info("\n[x] Size of request from client: {} bytes", request.toString().getBytes(StandardCharsets.UTF_8).length);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection = null;

        TransactionResponse response = TransactionResponse.newBuilder()
                .setData(request.getTransaction())
                .build();
        try {
            Channel channelRabbit = null;
            connection = factory.newConnection();
            channelRabbit = connection.createChannel();
            channelRabbit.queueDeclare(QUEUE_NAME, false, false, false, null);
            channelRabbit.basicPublish("", QUEUE_NAME, null, response.toString().getBytes(StandardCharsets.UTF_8));
            log.info("[!] Send '" + request + "'");
            log.info("response size: {} bytes", response.toString().getBytes(StandardCharsets.UTF_8).length);
            channelRabbit.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }


        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}

package vn.vnpay.grpcrabbit.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionRequest;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionResponse;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionServiceGrpc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
public class TransactionService extends TransactionServiceGrpc.TransactionServiceImplBase {
    private RabbitTemplate rabbitTemplate;

    @Value("${queue.exchange}")
    private String exchange;

    @Value("${queue.routingkey}")
    private String routingkey;

    private final String QUEUE_NAME = "grpc-rabbit";

    @Override
    public void getTransaction(TransactionRequest request, StreamObserver<TransactionResponse> responseObserver){
        log.info("\n[x] Request from client: {}", request);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection = null;

        TransactionResponse response = TransactionResponse.newBuilder()
                .setCode("00")
                .setMessage("Success")
                .setData(request.getTransaction())
                .build();
        try {
            log.info("Resp: {}", response.toString().getBytes(StandardCharsets.UTF_8));
            Channel channelRabbit = null;
            connection = factory.newConnection();
            channelRabbit = connection.createChannel();
            channelRabbit.queueDeclare(QUEUE_NAME, false, false, false, null);
            channelRabbit.basicPublish("", QUEUE_NAME, null, response.toString().getBytes(StandardCharsets.UTF_8));
            log.info("[!] Send '" + request + "'");
            channelRabbit.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void extracted(IOException e) {
        e.printStackTrace();
    }

}

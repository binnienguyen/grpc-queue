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

    @Value("${queue.name}")
    private String QUEUE_NAME;

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
            Channel channel = null;
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, response.toString().getBytes(StandardCharsets.UTF_8));
            log.info("[!] Send '" + request + "'");
            channel.close();
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

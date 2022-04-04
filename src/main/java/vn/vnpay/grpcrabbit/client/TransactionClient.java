package vn.vnpay.grpcrabbit.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import vn.vnpay.grpcrabbit.transaction.proto.Item;
import vn.vnpay.grpcrabbit.transaction.proto.Transaction;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionRequest;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionResponse;
import vn.vnpay.grpcrabbit.transaction.proto.TransactionServiceGrpc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TransactionClient {
    private final static String QUEUE_NAME = "grpc-rabbit";


    public static void main(String[] args) {
        ManagedChannel channelGrpc = ManagedChannelBuilder.forAddress("localhost", 8080)
                .usePlaintext().build();
        // set data for item
        Item item = Item.newBuilder()
                .setQrInfor("00020101021226240006908405011003104494765204421453037045405700005802VN5906BAEMIN6003HCM62660306BAEMIN051901210324174110900440708baemin010817Thanh toan Qrcode63046490")
                .setQuantity("1")
                .setNote("")
                .build();

        //set data for transaction
        Transaction transaction = Transaction.newBuilder()
                .setTokenKey("161658117920563925")
                .setBankCode("970436")
                .setAccountNo("0011003456789")
                .setPayDate("20210324172641")
                .setDebitAmount("70000")
                .setRespCode("00")
                .setRespDesc("Tru tien thanh cong, so trace 63925")
                .setItem(item)
                .setOrderCode("700035552")
                .setUsername("binnie")
                .setRealAmount("70000.0")
                .setPromotionCode("vnpay")
                .build();

        TransactionRequest request = TransactionRequest.newBuilder()
                .setTransaction(transaction)
                .build();
        log.info("\n[>] Request: {}", request);
        TransactionServiceGrpc.TransactionServiceBlockingStub blockingStub = TransactionServiceGrpc.newBlockingStub(channelGrpc);
        TransactionResponse response = blockingStub.getTransaction(request);
        final String[] message = new String[]{String.valueOf(response)};
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("admin");
            factory.setPassword("admin");
            Connection connection = factory.newConnection();
            Channel channelRabbit = connection.createChannel();
            channelRabbit.queueDeclare(QUEUE_NAME,
                    false, false, false, null);
            log.info("[!] Waiting for messages.....");

            Consumer consumer = new DefaultConsumer(channelRabbit) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body)
                        throws IOException {
                    message[0] = new String(body, "UTF-8");

                    log.info("[x] Message Recieved' " + message[0] + "'");
                    log.info("response size: {} bytes", message[0].getBytes(StandardCharsets.UTF_8).length);
                }
            };
            channelRabbit.basicConsume(QUEUE_NAME, true, consumer);
        }catch (Exception ex){
            log.error("Error: ", ex);
        }
        channelGrpc.shutdown();
    }
}

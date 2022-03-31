package vn.vnpay.grpcrabbit.server;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import vn.vnpay.grpcrabbit.service.TransactionService;

import java.io.IOException;

@Slf4j
public class TransactionServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new TransactionService()).build();
        log.info("\nStarting server.....");
        server.start();
        log.info("\nServer started!");
        server.awaitTermination();
    }
}

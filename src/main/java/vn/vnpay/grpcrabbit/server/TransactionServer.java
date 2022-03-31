package vn.vnpay.grpcrabbit.server;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import vn.vnpay.grpcrabbit.service.TransactionService;

import java.io.IOException;

public class TransactionServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(8080)
                .addService(new TransactionService()).build();
        System.out.println("Starting server.....");
        server.start();
        System.out.println("Server started!");
        server.awaitTermination();
    }
}

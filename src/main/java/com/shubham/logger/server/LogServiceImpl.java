package com.shubham.logger.server;

import com.shubham.logger.Logger;
import com.shubham.logger.Loglevel;
import com.shubham.logger.grpc.LogEventPayload;
import com.shubham.logger.grpc.LogResponse;
import com.shubham.logger.grpc.LogServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class LogServiceImpl extends LogServiceGrpc.LogServiceImplBase {

    @Value("${logger.allowed-sources}")
    private String allowedSourcesConfig;

    private Server grpcServer;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        grpcServer = ServerBuilder.forPort(9090)
                .addService(this)
                .build()
                .start();
        System.out.println("gRPC server started on port 9090");
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }

    @Override
    public void send(LogEventPayload request, StreamObserver<LogResponse> responseObserver) {
        List<String> allowedSources = Arrays.asList(allowedSourcesConfig.split(","));
        
        if (!allowedSources.contains(request.getSource())) {
            responseObserver.onNext(LogResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Source not allowed")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        try {
            Loglevel level = Loglevel.valueOf(request.getLevel().toUpperCase());
            Logger.getInstance().log(level, request.getMessage(), request.getSource());
            
            responseObserver.onNext(LogResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onNext(LogResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Invalid level")
                    .build());
            responseObserver.onCompleted();
        }
    }
}

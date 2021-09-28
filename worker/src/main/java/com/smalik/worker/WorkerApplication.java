package com.smalik.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.function.Function;

@SpringBootApplication
public class WorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	@Bean
	public Function<MoveStepRequest, MoveStepResponse> processor() {
		return request -> MoveStepResponse.builder()
					.moveId(request.getMoveId())
					.playerId(request.getPlayerId())
					.turnId(request.getTurnId())
					.failed(false)
					.time(OffsetDateTime.now())
					.build();
	}
}

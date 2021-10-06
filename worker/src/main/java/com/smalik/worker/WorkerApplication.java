package com.smalik.worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.function.Function;

@SpringBootApplication
@Slf4j
public class WorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	@Bean
	public Function<MoveStepRequest, MoveStepResponse> processor() {
		return request -> {
			log.info("Got request: Player={}, Move={}, Turn={}, Step={}", request.getPlayerId(), request.getMoveId(), request.getTurnId(), request.getStep());
			return MoveStepResponse.builder()
					.moveId(request.getMoveId())
					.playerId(request.getPlayerId())
					.turnId(request.getTurnId())
					.failed(false)
					.time(OffsetDateTime.now())
					.build();
		};
	}
}

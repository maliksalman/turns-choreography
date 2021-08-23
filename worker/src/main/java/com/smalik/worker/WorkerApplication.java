package com.smalik.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class WorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkerApplication.class, args);
	}

	@Bean
	public Function<MoveStepRequest, MoveStepResponse> processor() {
		return request -> {
			try {
				Thread.sleep(250l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return MoveStepResponse.builder()
					.moveId(request.getMoveId())
					.playerId(request.getPlayerId())
					.turnId(request.getTurnId())
					.step(request.getStep())
					.status("DONE")
					.build();
		};
	}
}

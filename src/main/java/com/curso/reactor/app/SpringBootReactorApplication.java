package com.curso.reactor.app;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.curso.reactor.app.models.User;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
	
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploIntervalInfinito();
	}
	
	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap(i->(i<=5)?Flux.just(i):Flux.error(new InterruptedException("Solo hasta 5!")))
		.map(i->"Hola "+i)
		.retry(2)
		.subscribe(s->log.info(s),e->log.error(e.getMessage()));
		
		latch.await();
	}
	
	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i->log.info(i.toString()));
		
		rango.blockLast();

	}
	
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso,(ra,re)->ra)
		.doOnNext(i->log.info(i.toString()))
		.blockLast();
	}
	
	public void ejemploZipWithRangos() {
		Flux.just(1,2,3,4)
		.map(e->e*2)
		.zipWith(Flux.range(0, 4),(uno,dos)-> String.format("Primer Flux: %d, Segundo Flux: %d",uno,dos))
		.subscribe(texto->log.info(texto));
	}
	
	public void basico() {
		Flux<String> nombre = Flux.just("Juan","Maria","Pedro","Carlos")
				.map(n->new User(n, null))
				.doOnNext(u->{
					if(u.getName().isBlank()) {
						throw new RuntimeException("Nombre vacio");
					}
					System.out.println(u.getName());
				})
				.map(u -> u.getName().toLowerCase());
		
		nombre.subscribe(
				log::info,
				e->log.error(e.getMessage()),
				new Runnable() {
					
					@Override
					public void run() {
						System.out.println("Se finalizo con exito");
					}
				});
	}

}

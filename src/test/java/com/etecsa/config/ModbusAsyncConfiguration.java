package com.etecsa.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync // <--- Esto es clave para que funcionen las anotaciones @Async
public class ModbusAsyncConfiguration {

    @Bean(name = "modbusExecutor")
    public Executor modbusExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Con 200 equipos, necesitamos hilos trabajando en paralelo
        executor.setCorePoolSize(20); // Número de hilos activos simultáneamente
        executor.setMaxPoolSize(50); // Máximo de hilos si hay mucha carga
        executor.setQueueCapacity(300); // Cola de tareas esperando hilo
        executor.setThreadNamePrefix("PLC-Reader-");
        executor.initialize();
        return executor;
    }
}

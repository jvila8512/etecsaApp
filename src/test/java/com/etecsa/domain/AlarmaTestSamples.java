package com.etecsa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AlarmaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Alarma getAlarmaSample1() {
        return new Alarma().id(1L).descripcion("descripcion1").mensajeUsuario("mensajeUsuario1");
    }

    public static Alarma getAlarmaSample2() {
        return new Alarma().id(2L).descripcion("descripcion2").mensajeUsuario("mensajeUsuario2");
    }

    public static Alarma getAlarmaRandomSampleGenerator() {
        return new Alarma()
            .id(longCount.incrementAndGet())
            .descripcion(UUID.randomUUID().toString())
            .mensajeUsuario(UUID.randomUUID().toString());
    }
}

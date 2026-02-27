package com.etecsa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EventoEquipoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static EventoEquipo getEventoEquipoSample1() {
        return new EventoEquipo().id(1L).nombreVariable("nombreVariable1").direccionModbus(1).intervaloLectura(1);
    }

    public static EventoEquipo getEventoEquipoSample2() {
        return new EventoEquipo().id(2L).nombreVariable("nombreVariable2").direccionModbus(2).intervaloLectura(2);
    }

    public static EventoEquipo getEventoEquipoRandomSampleGenerator() {
        return new EventoEquipo()
            .id(longCount.incrementAndGet())
            .nombreVariable(UUID.randomUUID().toString())
            .direccionModbus(intCount.incrementAndGet())
            .intervaloLectura(intCount.incrementAndGet());
    }
}

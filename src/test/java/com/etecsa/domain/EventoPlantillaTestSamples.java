package com.etecsa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EventoPlantillaTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static EventoPlantilla getEventoPlantillaSample1() {
        return new EventoPlantilla()
            .id(1L)
            .nombre("nombre1")
            .descripcion("descripcion1")
            .unidadMedida("unidadMedida1")
            .funcionLectura("funcionLectura1")
            .funcionEscritura("funcionEscritura1");
    }

    public static EventoPlantilla getEventoPlantillaSample2() {
        return new EventoPlantilla()
            .id(2L)
            .nombre("nombre2")
            .descripcion("descripcion2")
            .unidadMedida("unidadMedida2")
            .funcionLectura("funcionLectura2")
            .funcionEscritura("funcionEscritura2");
    }

    public static EventoPlantilla getEventoPlantillaRandomSampleGenerator() {
        return new EventoPlantilla()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString())
            .unidadMedida(UUID.randomUUID().toString())
            .funcionLectura(UUID.randomUUID().toString())
            .funcionEscritura(UUID.randomUUID().toString());
    }
}

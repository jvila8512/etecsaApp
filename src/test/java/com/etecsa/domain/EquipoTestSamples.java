package com.etecsa.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EquipoTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Equipo getEquipoSample1() {
        return new Equipo()
            .id(1L)
            .nombre("nombre1")
            .direccionIp("direccionIp1")
            .modbusSlaveId(1)
            .modelo("modelo1")
            .firmwareVersion("firmwareVersion1");
    }

    public static Equipo getEquipoSample2() {
        return new Equipo()
            .id(2L)
            .nombre("nombre2")
            .direccionIp("direccionIp2")
            .modbusSlaveId(2)
            .modelo("modelo2")
            .firmwareVersion("firmwareVersion2");
    }

    public static Equipo getEquipoRandomSampleGenerator() {
        return new Equipo()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .direccionIp(UUID.randomUUID().toString())
            .modbusSlaveId(intCount.incrementAndGet())
            .modelo(UUID.randomUUID().toString())
            .firmwareVersion(UUID.randomUUID().toString());
    }
}

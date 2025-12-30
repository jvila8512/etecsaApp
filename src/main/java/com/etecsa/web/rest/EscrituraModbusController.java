package com.etecsa.web.rest;

import com.etecsa.service.modbus.ModBusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/escritura")
public class EscrituraModbusController {

    @Autowired
    private ModBusService modBusService;

    /**
     * Escribir en una memoria M específica
     */
    @PostMapping("/escribir-m/{direccion}/{valor}")
    public String escribirMemoriaM(@PathVariable int direccion, @PathVariable boolean valor) {
        boolean exito = modBusService.writeCoil("192.168.1.11", direccion, valor);

        if (exito) {
            return String.format("✅ Escrito M%d = %s", direccion, valor ? "ON" : "OFF");
        } else {
            return String.format("❌ Error escribiendo M%d", direccion);
        }
    }

    /**
     * Escribir en un registro MW específico
     */
    @PostMapping("/escribir-mw/{direccion}/{valor}")
    public String escribirRegistroMW(@PathVariable int direccion, @PathVariable int valor) {
        boolean exito = modBusService.writeRegister("192.168.1.11", direccion, valor);

        if (exito) {
            return String.format("✅ Escrito MW%d = %d", direccion, valor);
        } else {
            return String.format("❌ Error escribiendo MW%d", direccion);
        }
    }

    /**
     * Alternar (toggle) una memoria M
     */
    @PostMapping("/alternar-m/{direccion}")
    public String alternarMemoriaM(@PathVariable int direccion) {
        try {
            // Primero leer el estado actual
            Boolean estadoActual = modBusService.readCoil("192.168.1.11", direccion);

            if (estadoActual == null) {
                return "❌ No se pudo leer el estado actual";
            }

            // Alternar el valor
            boolean nuevoEstado = !estadoActual;
            boolean exito = modBusService.writeCoil("192.168.1.11", direccion, nuevoEstado);

            if (exito) {
                return String.format("🔄 M%d cambiado de %s a %s", direccion, estadoActual ? "ON" : "OFF", nuevoEstado ? "ON" : "OFF");
            } else {
                return String.format("❌ Error alternando M%d", direccion);
            }
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    /**
     * Probar escritura en varias memorias
     */
    @PostMapping("/prueba-escritura")
    public String pruebaEscritura() {
        StringBuilder resultado = new StringBuilder();
        resultado.append("🧪 PRUEBA DE ESCRITURA\n");
        resultado.append("=====================\n");

        // Probar escribir en M0
        boolean exito1 = modBusService.writeCoil("192.168.1.11", 0, true);
        resultado.append("M0 = ON: ").append(exito1 ? "✅" : "❌").append("\n");

        // Probar escribir en M1
        boolean exito2 = modBusService.writeCoil("192.168.1.11", 1, false);
        resultado.append("M1 = OFF: ").append(exito2 ? "✅" : "❌").append("\n");

        // Probar escribir en MW10
        boolean exito3 = modBusService.writeRegister("192.168.1.11", 10, 1234);
        resultado.append("MW10 = 1234: ").append(exito3 ? "✅" : "❌").append("\n");

        // Probar escribir en MW11
        boolean exito4 = modBusService.writeRegister("192.168.1.11", 11, 5678);
        resultado.append("MW11 = 5678: ").append(exito4 ? "✅" : "❌").append("\n");

        resultado.append("=====================\n");
        return resultado.toString();
    }
}

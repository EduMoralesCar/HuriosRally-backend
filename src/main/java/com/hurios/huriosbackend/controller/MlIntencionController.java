package com.hurios.huriosbackend.controller;

import com.hurios.huriosbackend.service.MlIntencionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST en Spring Boot que expone la integración con el microservicio de Machine Learning (HuriosRally-ML).
 * Permite clasificar consultas de clientes en vivo y consultar las métricas oficiales del modelo ganador (APF3).
 */
@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = "*")
public class MlIntencionController {

    private final MlIntencionService mlIntencionService;

    public MlIntencionController(MlIntencionService mlIntencionService) {
        this.mlIntencionService = mlIntencionService;
    }

    @PostMapping("/clasificar")
    public ResponseEntity<Map<String, Object>> clasificarMensaje(@RequestBody Map<String, Object> body) {
        String textoCliente = String.valueOf(body.getOrDefault("texto_cliente", "")).trim();
        if (textoCliente.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'texto_cliente' es obligatorio."));
        }
        Integer horaDelDia = null;
        if (body.get("hora_del_dia") instanceof Number num) {
            horaDelDia = num.intValue();
        }
        Map<String, Object> resultado = mlIntencionService.clasificarMensajeCliente(textoCliente, horaDelDia);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoModelo() {
        return ResponseEntity.ok(mlIntencionService.obtenerMetricasModelo());
    }
}

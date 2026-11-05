package com.hurios.huriosbackend.controller;

import com.hurios.huriosbackend.service.MlIntencionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
            return ResponseEntity.badRequest().body(Map.of("error", "El mensaje es obligatorio."));
        }
        Integer horaDelDia = null;
        if (body.get("hora_del_dia") instanceof Number num) {
            horaDelDia = num.intValue();
        }
        Map<String, Object> resultado = mlIntencionService.clasificarYRegistrarMensaje(textoCliente, horaDelDia);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/historial")
    public ResponseEntity<List<Map<String, Object>>> obtenerHistorial() {
        return ResponseEntity.ok(mlIntencionService.obtenerHistorialConsultas());
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> obtenerInfoModelo() {
        return ResponseEntity.ok(mlIntencionService.obtenerMetricasModelo());
    }
}

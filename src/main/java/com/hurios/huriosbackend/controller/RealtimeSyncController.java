package com.hurios.huriosbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeSyncController {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<Map<String, Object>> notifications = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> reviews = new CopyOnWriteArrayList<>();

    public RealtimeSyncController() {
        inicializarDatosSemilla();
    }

    private void inicializarDatosSemilla() {
        String ahora = LocalDateTime.now(ZONA_LIMA).format(FMT);

        // Reseñas iniciales verificadas para productos reales
        Map<String, Object> rev1 = new LinkedHashMap<>();
        rev1.put("id", "rev-seed-1");
        rev1.put("productId", 4574);
        rev1.put("productName", "Cilindro Completo Torito Bajaj 4T");
        rev1.put("userName", "Carlos Quispe");
        rev1.put("rating", 5);
        rev1.put("comment", "Excelente calidad, le calzó exacto a mi Torito Bajaj 4T RE 205 y recuperó toda la compresión.");
        rev1.put("createdAt", ahora);
        reviews.add(rev1);

        Map<String, Object> rev2 = new LinkedHashMap<>();
        rev2.put("id", "rev-seed-2");
        rev2.put("productId", 9);
        rev2.put("productName", "Kit de Discos de Embrague Kevlar Bajaj Torito");
        rev2.put("userName", "Luis Paredes");
        rev2.put("rating", 5);
        rev2.put("comment", "Muy buena duración para trabajo diario en subida, no patina y el envío fue rápido.");
        rev2.put("createdAt", ahora);
        reviews.add(rev2);

        // Notificación inicial diferenciada por rol
        Map<String, Object> notif1 = new LinkedHashMap<>();
        notif1.put("id", "notif-seed-1");
        notif1.put("type", "STOCK_UPDATE");
        notif1.put("productId", 4574);
        notif1.put("titleAdmin", "Control de Inventario Activo");
        notif1.put("messageAdmin", "Sincronización en tiempo real activa para stock, ventas y valoraciones de clientes.");
        notif1.put("linkAdmin", "/inventario");
        notif1.put("titleClient", "Catálogo Actualizado en Tiempo Real");
        notif1.put("messageClient", "Explora los repuestos con stock verificado y promociones disponibles hoy.");
        notif1.put("linkClient", "/products");
        notif1.put("createdAt", ahora);
        notifications.add(notif1);
    }

    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> getFeed() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("notifications", notifications);
        response.put("reviews", reviews);
        response.put("serverTime", LocalDateTime.now(ZONA_LIMA).format(FMT));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notify")
    public ResponseEntity<Map<String, Object>> pushNotification(@RequestBody Map<String, Object> body) {
        String id = String.valueOf(body.getOrDefault("id", "notif-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000)));
        String ahora = LocalDateTime.now(ZONA_LIMA).format(FMT);

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("type", body.getOrDefault("type", "INFO"));
        item.put("productId", body.get("productId"));
        item.put("titleAdmin", body.getOrDefault("titleAdmin", "Actualización del sistema"));
        item.put("messageAdmin", body.getOrDefault("messageAdmin", ""));
        item.put("linkAdmin", body.getOrDefault("linkAdmin", "/admin-profile"));
        item.put("titleClient", body.getOrDefault("titleClient", "Novedad en Hurios Rally"));
        item.put("messageClient", body.getOrDefault("messageClient", ""));
        item.put("linkClient", body.getOrDefault("linkClient", "/products"));
        item.put("createdAt", body.getOrDefault("createdAt", ahora));

        notifications.removeIf(n -> id.equals(n.get("id")));
        notifications.add(0, item);
        while (notifications.size() > 60) {
            notifications.remove(notifications.size() - 1);
        }

        return ResponseEntity.ok(item);
    }

    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> addReview(@RequestBody Map<String, Object> body) {
        String id = String.valueOf(body.getOrDefault("id", "rev-" + System.currentTimeMillis()));
        String ahora = LocalDateTime.now(ZONA_LIMA).format(FMT);
        Object productIdObj = body.get("productId");
        int productId = productIdObj instanceof Number ? ((Number) productIdObj).intValue() : 0;
        String productName = String.valueOf(body.getOrDefault("productName", "Repuesto #" + productId));
        String userName = String.valueOf(body.getOrDefault("userName", "Cliente Verificado"));
        int rating = body.get("rating") instanceof Number ? Math.min(5, Math.max(1, ((Number) body.get("rating")).intValue())) : 5;
        String comment = String.valueOf(body.getOrDefault("comment", "")).trim();

        Map<String, Object> review = new LinkedHashMap<>();
        review.put("id", id);
        review.put("productId", productId);
        review.put("productName", productName);
        review.put("userName", userName);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("createdAt", ahora);

        reviews.removeIf(r -> id.equals(r.get("id")));
        reviews.add(0, review);
        while (reviews.size() > 200) {
            reviews.remove(reviews.size() - 1);
        }

        // Generar notificación diferenciada para Admin y Cliente
        Map<String, Object> notif = new LinkedHashMap<>();
        notif.put("id", "notif-rev-" + System.currentTimeMillis());
        notif.put("type", "NEW_REVIEW");
        notif.put("productId", productId);
        notif.put("titleAdmin", "Nueva valoración de cliente (" + rating + "/5 ★)");
        notif.put("messageAdmin", userName + " calificó \"" + productName + "\" con " + rating + " estrellas: \"" + comment + "\"");
        notif.put("linkAdmin", "/products?productId=" + productId);
        notif.put("titleClient", "Nueva opinión en " + productName);
        notif.put("messageClient", userName + " valoró este repuesto con " + rating + " estrellas. Haz clic para ver opiniones.");
        notif.put("linkClient", "/products?productId=" + productId);
        notif.put("createdAt", ahora);
        notifications.add(0, notif);

        return ResponseEntity.ok(review);
    }
}

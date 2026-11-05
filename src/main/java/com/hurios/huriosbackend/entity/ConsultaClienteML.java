package com.hurios.huriosbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas_clientes_ml")
public class ConsultaClienteML {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_conversacion", nullable = false, length = 64)
    private String idConversacion;

    @Column(name = "timestamp_primer_mensaje", nullable = false)
    private LocalDateTime timestampPrimerMensaje;

    @Column(name = "timestamp_respuesta_bot", nullable = false)
    private LocalDateTime timestampRespuestaBot;

    @Column(name = "texto_cliente", columnDefinition = "TEXT", nullable = false)
    private String textoCliente;

    @Column(name = "texto_normalizado", columnDefinition = "TEXT")
    private String textoNormalizado;

    @Column(name = "intencion_predicha", nullable = false, length = 40)
    private String intencionPredicha;

    @Column(name = "confianza_modelo", nullable = false)
    private Double confianzaModelo;

    @Column(name = "producto_cotizado", length = 255)
    private String productoCotizado;

    @Column(name = "monto_total")
    private Double montoTotal;

    @Column(name = "metodo_pago", length = 40)
    private String metodoPago;

    @Column(name = "estado_pedido", length = 40)
    private String estadoPedido;

    @Column(name = "hora_del_dia", nullable = false)
    private Integer horaDelDia;

    @Column(name = "es_fuera_horario", nullable = false)
    private Boolean esFueraHorario;

    @Column(name = "requiere_derivacion_humana", nullable = false)
    private Boolean requiereDerivacionHumana;

    @Column(name = "accion_negocio_sugerida", columnDefinition = "TEXT")
    private String accionNegocioSugerida;

    @Column(name = "respuesta_automatica_bot", columnDefinition = "TEXT")
    private String respuestaAutomaticaBot;

    @Column(name = "probabilidades_json", columnDefinition = "TEXT")
    private String probabilidadesJson;

    @Column(name = "productos_sugeridos_json", columnDefinition = "TEXT")
    private String productosSugeridosJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdConversacion() { return idConversacion; }
    public void setIdConversacion(String idConversacion) { this.idConversacion = idConversacion; }

    public LocalDateTime getTimestampPrimerMensaje() { return timestampPrimerMensaje; }
    public void setTimestampPrimerMensaje(LocalDateTime timestampPrimerMensaje) { this.timestampPrimerMensaje = timestampPrimerMensaje; }

    public LocalDateTime getTimestampRespuestaBot() { return timestampRespuestaBot; }
    public void setTimestampRespuestaBot(LocalDateTime timestampRespuestaBot) { this.timestampRespuestaBot = timestampRespuestaBot; }

    public String getTextoCliente() { return textoCliente; }
    public void setTextoCliente(String textoCliente) { this.textoCliente = textoCliente; }

    public String getTextoNormalizado() { return textoNormalizado; }
    public void setTextoNormalizado(String textoNormalizado) { this.textoNormalizado = textoNormalizado; }

    public String getIntencionPredicha() { return intencionPredicha; }
    public void setIntencionPredicha(String intencionPredicha) { this.intencionPredicha = intencionPredicha; }

    public Double getConfianzaModelo() { return confianzaModelo; }
    public void setConfianzaModelo(Double confianzaModelo) { this.confianzaModelo = confianzaModelo; }

    public String getProductoCotizado() { return productoCotizado; }
    public void setProductoCotizado(String productoCotizado) { this.productoCotizado = productoCotizado; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String estadoPedido) { this.estadoPedido = estadoPedido; }

    public Integer getHoraDelDia() { return horaDelDia; }
    public void setHoraDelDia(Integer horaDelDia) { this.horaDelDia = horaDelDia; }

    public Boolean getEsFueraHorario() { return esFueraHorario; }
    public void setEsFueraHorario(Boolean esFueraHorario) { this.esFueraHorario = esFueraHorario; }

    public Boolean getRequiereDerivacionHumana() { return requiereDerivacionHumana; }
    public void setRequiereDerivacionHumana(Boolean requiereDerivacionHumana) { this.requiereDerivacionHumana = requiereDerivacionHumana; }

    public String getAccionNegocioSugerida() { return accionNegocioSugerida; }
    public void setAccionNegocioSugerida(String accionNegocioSugerida) { this.accionNegocioSugerida = accionNegocioSugerida; }

    public String getrespuestaAutomaticaBot() { return respuestaAutomaticaBot; }
    public void setRespuestaAutomaticaBot(String respuestaAutomaticaBot) { this.respuestaAutomaticaBot = respuestaAutomaticaBot; }

    public String getProbabilidadesJson() { return probabilidadesJson; }
    public void setProbabilidadesJson(String probabilidadesJson) { this.probabilidadesJson = probabilidadesJson; }

    public String getProductosSugeridosJson() { return productosSugeridosJson; }
    public void setProductosSugeridosJson(String productosSugeridosJson) { this.productosSugeridosJson = productosSugeridosJson; }
}

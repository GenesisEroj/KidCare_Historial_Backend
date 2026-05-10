package com.kidcare.historial_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GenerarHistorialResponseDTO {
    private Integer idHistorial;
    private Integer idMenor;
    private String resumen;
    private LocalDate fecha;
    private Boolean generadoPorIA;
    private List<String> interaccionesUsadas;
}

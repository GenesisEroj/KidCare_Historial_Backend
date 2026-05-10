package com.kidcare.historial_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerarHistorialRequestDTO {

    @NotNull(message = "El id del menor es obligatorio")
    private Integer idMenor;

    @NotEmpty(message = "Debe seleccionar al menos una interacción")
    private List<String> idInteracciones;
}

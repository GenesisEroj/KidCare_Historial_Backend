package com.kidcare.historial_service.controller;

import com.kidcare.historial_service.dto.GenerarHistorialRequestDTO;
import com.kidcare.historial_service.dto.GenerarHistorialResponseDTO;
import com.kidcare.historial_service.dto.HistorialRequestDTO;
import com.kidcare.historial_service.dto.HistorialResponseDTO;
import com.kidcare.historial_service.service.HistorialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador que expone los endpoints de gestión de historiales
@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    @Autowired
    private HistorialService historialService;

    // POST /api/historial — crea un nuevo historial con resumen de Claude
    @PostMapping
    public ResponseEntity<HistorialResponseDTO> crear(@Valid @RequestBody HistorialRequestDTO dto) {
        return ResponseEntity.ok(historialService.crear(dto));
    }

    // GET /api/historial/menor/{idMenor} — obtiene todos los historiales de un
    // menor
    @GetMapping("/menor/{idMenor}")
    public ResponseEntity<List<HistorialResponseDTO>> listarPorMenor(@PathVariable Integer idMenor) {
        return ResponseEntity.ok(historialService.obtenerPorMenor(idMenor));
    }

    // GET /api/historial/medico/{idMenor} — obtiene el último historial para el
    // médico (ruta pública)
    @GetMapping("/medico/{idMenor}")
    public ResponseEntity<HistorialResponseDTO> obtenerUltimo(@PathVariable Integer idMenor) {
        return ResponseEntity.ok(historialService.obtenerUltimo(idMenor));
    }

    // POST /api/historial/generar — genera resumen con Claude API (CU012)
    @PostMapping("/generar")
    public ResponseEntity<GenerarHistorialResponseDTO> generarConIA(
            @Valid @RequestBody GenerarHistorialRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(historialService.generarConClaude(dto));
    }
}
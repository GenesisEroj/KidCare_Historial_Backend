package com.kidcare.historial_service.service;

import com.kidcare.historial_service.dto.GenerarHistorialRequestDTO;
import com.kidcare.historial_service.dto.GenerarHistorialResponseDTO;
import com.kidcare.historial_service.dto.HistorialRequestDTO;
import com.kidcare.historial_service.dto.HistorialResponseDTO;
import com.kidcare.historial_service.model.Historial;
import com.kidcare.historial_service.repository.HistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Servicio que maneja la gestión de historiales
@Service
public class HistorialService {

    @Autowired
    private HistorialRepository historialRepository;

    @Autowired
    private ClaudeService claudeService;

    @Value("${chatbot.service.url:http://localhost:8083}")
    private String chatbotServiceUrl;

    // Crea un nuevo historial con el resumen generado por Claude
    public HistorialResponseDTO crear(HistorialRequestDTO dto) {

        Historial historial = new Historial();
        historial.setIdMenor(dto.getIdMenor());
        historial.setResumen(dto.getResumen());
        historial.setFecha(LocalDate.now());
        historialRepository.save(historial);

        return mapToDTO(historial);
    }

    // Obtiene todos los historiales de un menor
    public List<HistorialResponseDTO> obtenerPorMenor(Integer idMenor) {
        return historialRepository.findByIdMenorOrderByFechaDesc(idMenor)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Obtiene el historial más reciente de un menor
    public HistorialResponseDTO obtenerUltimo(Integer idMenor) {
        Historial historial = historialRepository.findTopByIdMenorOrderByFechaDesc(idMenor)
                .orElseThrow(() -> new RuntimeException("No hay historial para este menor"));
        return mapToDTO(historial);
    }

    public GenerarHistorialResponseDTO generarConClaude(GenerarHistorialRequestDTO dto) {
        RestTemplate restTemplate = new RestTemplate();
        String url = chatbotServiceUrl + "/api/interacciones/interno/menor/" + dto.getIdMenor();

        List<Map<String, Object>> todasInteracciones;
        try {
            Map[] respuesta = restTemplate.getForObject(url, Map[].class);
            todasInteracciones = respuesta != null ? Arrays.asList(respuesta) : new ArrayList<>();
        } catch (Exception e) {
            todasInteracciones = new ArrayList<>();
        }

        List<Map<String, Object>> seleccionadas = todasInteracciones.stream()
                .filter(i -> dto.getIdInteracciones().contains(
                        i.get("id") != null ? i.get("id").toString() : ""))
                .collect(Collectors.toList());

        if (seleccionadas.isEmpty()) {
            throw new RuntimeException("No se encontraron las interacciones seleccionadas");
        }

        List<String> observaciones = seleccionadas.stream()
                .map(i -> i.getOrDefault("observaciones", "").toString())
                .collect(Collectors.toList());

        String resumen;
        boolean generadoPorIA;

        try {
            resumen = claudeService.generarResumenClinico(observaciones, "Menor id: " + dto.getIdMenor());
            generadoPorIA = true;
        } catch (Exception e) {
            resumen = "Resumen de observaciones:\n- " + String.join("\n- ", observaciones);
            generadoPorIA = false;
        }

        Historial historial = new Historial();
        historial.setIdMenor(dto.getIdMenor());
        historial.setFecha(LocalDate.now());
        historial.setResumen(resumen);
        historialRepository.save(historial);

        GenerarHistorialResponseDTO response = new GenerarHistorialResponseDTO();
        response.setIdHistorial(historial.getIdHistorial());
        response.setIdMenor(historial.getIdMenor());
        response.setResumen(resumen);
        response.setFecha(historial.getFecha());
        response.setGeneradoPorIA(generadoPorIA);
        response.setInteraccionesUsadas(dto.getIdInteracciones());
        return response;
    }

    // Convierte una entidad Historial a HistorialResponseDTO
    private HistorialResponseDTO mapToDTO(Historial historial) {
        HistorialResponseDTO dto = new HistorialResponseDTO();
        dto.setIdHistorial(historial.getIdHistorial());
        dto.setIdMenor(historial.getIdMenor());
        dto.setFecha(historial.getFecha());
        dto.setResumen(historial.getResumen());
        return dto;
    }
}
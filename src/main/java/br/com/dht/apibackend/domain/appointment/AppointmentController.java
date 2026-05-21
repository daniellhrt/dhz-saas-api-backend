/**
 * Propósito: Ponto de entrada REST para gerenciar agenda.
 * Papel na Arquitetura: Domain / Controller.
 */
package br.com.dht.apibackend.domain.appointment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDTO.Response> create(@RequestBody @Valid AppointmentDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.scheduleAppointment(request));
    }
}
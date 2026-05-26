/**
 * Propósito: Ponto de entrada REST para gerenciar agenda.
 * Papel na Arquitetura: Domain / Controller.
 */
package br.com.dht.apibackend.domain.appointment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDTO.Response> create(@RequestBody @Valid AppointmentDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.scheduleAppointment(request));
    }

    @PostMapping("/block")
    public ResponseEntity<AppointmentDTO.Response> block(@RequestBody @Valid AppointmentDTO.BlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.blockSchedule(request));
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentDTO.Response>> listAll(Pageable pageable) {
        return ResponseEntity.ok(appointmentService.listAllAppointments(pageable));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentDTO.Response> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO.Response> cancel(@PathVariable UUID id, @RequestBody(required = false) AppointmentDTO.CancelRequest request) {
        String reason = (request != null) ? request.reason() : null;
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, reason));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<AppointmentDTO.Response> start(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.startAppointment(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentDTO.Response> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    @PatchMapping("/{id}/revert")
    public ResponseEntity<AppointmentDTO.Response> revert(@PathVariable UUID id) {
        // TODO: Validate ADMIN role here if using Spring Security roles (@PreAuthorize("hasRole('ADMIN')"))
        return ResponseEntity.ok(appointmentService.revertAppointment(id));
    }
}
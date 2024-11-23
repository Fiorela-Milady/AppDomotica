package com.app.domoticaweb.controller;

import com.app.domoticaweb.model.HorarioAlarma;
import com.app.domoticaweb.model.HorarioLucesExterior;
import com.app.domoticaweb.model.Variable;
import com.app.domoticaweb.repository.VariableRepository;
import com.app.domoticaweb.service.impl.CasaDomoticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/domotica")
@RequiredArgsConstructor
public class CasaDomoticaController {

    private final CasaDomoticaService casaDomoticaService;
    private final VariableRepository variableRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/verConfigurarLuces")
    public String mostrarConfiguracionLuces(Model model) {

        HorarioLucesExterior horario = casaDomoticaService.buscarHorarioLucesExteriorId(3L);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        String horaEncendidoFormatted = horario != null && horario.getHoraEncendido() != null
                ? horario.getHoraEncendido().format(formatter)
                : "18:00";

        String horaApagadoFormatted = horario != null && horario.getHoraApagado() != null
                ? horario.getHoraApagado().format(formatter)
                : "06:00";

        model.addAttribute("horario", horario != null ? horario : new HorarioLucesExterior());
        model.addAttribute("horaEncendidoFormatted", horaEncendidoFormatted);
        model.addAttribute("horaApagadoFormatted", horaApagadoFormatted);
        return "configurar_luces_ADMIN";
    }

    @PostMapping("/guardarHorarioLuces")
    public String guardarHorarioLuces(
            @RequestParam("activarAnochecer") boolean activarAnochecer,
            @RequestParam("desactivarAmanecer") boolean desactivarAmanecer,
            @RequestParam(value = "horaEncendido", required = false) String horaEncendidoStr,
            @RequestParam(value = "horaApagado", required = false) String horaApagadoStr) {

        Variable variable = variableRepository.findById(3L).orElseThrow();

        LocalTime horaEncendido = (activarAnochecer && horaEncendidoStr != null) ? LocalTime.parse(horaEncendidoStr) : null;
        LocalTime horaApagado = (desactivarAmanecer && horaApagadoStr != null) ? LocalTime.parse(horaApagadoStr) : null;

        HorarioLucesExterior horario = HorarioLucesExterior.builder()
                .variable(variable)
                .horaEncendido(horaEncendido)
                .horaApagado(horaApagado)
                .activo(true)
                .build();

        casaDomoticaService.guardarHorarioLuces(horario);

        return "redirect:/domotica/verConfigurarLuces";
    }

    @GetMapping("/listarVariables")
    public String listarVariables(Model model) {
        try {
            List<Variable> variables = casaDomoticaService.listarVariables();
            model.addAttribute("variables", variables);
            model.addAttribute("title", "Configurar Horario de Luces Exteriores");
        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener el listado de variables: " + e.getMessage());
        }
        return "variables_listado_ADMIN";
    }

    @GetMapping("/verConfigurarAlarma")
    public String mostrarConfiguracionAlarma(Model model) {

        HorarioAlarma horarioAlarma = casaDomoticaService.buscarHorarioAlarmaPorVariableId(15L);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        String horaInicioFormatted = horarioAlarma != null && horarioAlarma.getHoraInicio() != null
                ? horarioAlarma.getHoraInicio().format(formatter)
                : "18:00";

        String horaFinFormatted = horarioAlarma != null && horarioAlarma.getHoraFin() != null
                ? horarioAlarma.getHoraFin().format(formatter)
                : "06:00";

        model.addAttribute("horarioAlarma", horarioAlarma != null ? horarioAlarma : new HorarioAlarma());
        model.addAttribute("horaInicioFormatted", horaInicioFormatted);
        model.addAttribute("horaFinFormatted", horaFinFormatted);
        return "configurar_alarma";
    }

    @PostMapping("/guardarHorarioAlarma")
    public String guardarHorarioAlarma(
            @RequestParam("activarAlarma") boolean activarAlarma,
            @RequestParam(value = "horaInicio", required = false) String horaInicioStr,
            @RequestParam(value = "horaFin", required = false) String horaFinStr) {

        Variable variable = variableRepository.findById(15L).orElseThrow();

        LocalTime horaInicio = (horaInicioStr != null) ? LocalTime.parse(horaInicioStr) : null;
        LocalTime horaFin = (horaFinStr != null) ? LocalTime.parse(horaFinStr) : null;
        casaDomoticaService.guardarOActualizarHorarioAlarma(variable, horaInicio, horaFin, activarAlarma);

        return "redirect:/domotica/verConfigurarAlarma";
    }


    // caso de uso 8
    @GetMapping("/controlManualLuces")
    public String mostrarControlManualLuces(Model model) {
        try {
            List<Variable> todasLasVariables = casaDomoticaService.listarVariables();

            // Filtrar solo las variables que son luces
            List<Variable> luces = todasLasVariables.stream()
                    .filter(v -> v.getNombre().contains("luz"))
                    .collect(Collectors.toList());

            model.addAttribute("luces", luces);
            model.addAttribute("titulo", "Control Manual de Luces");
        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener el listado de luces: " + e.getMessage());
        }
        return "control_manual_luces";
    }


    @PostMapping("/cambiarEstadoLuz")
    @ResponseBody
    public Map<String, Object> cambiarEstadoLuz(
            @RequestParam("luzId") String luzId,
            @RequestParam("encender") boolean encender) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Cambia el estado de la luz en la nube (asumiendo que es exitoso)
            casaDomoticaService.cambiarEstadoLuz(luzId, encender);

            // Responder de manera optimista sin verificar el estado real en la nube
            response.put("success", true);
            response.put("luzId", luzId);
            response.put("nuevoEstado", encender ? "true" : "false");
            response.put("message", "El estado de la luz ha sido actualizado correctamente.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el estado de la luz: " + e.getMessage());
        }
        return response;
    }




    @GetMapping("/monitoreoTemperatura")
    public String mostrarMonitoreoTemperatura(Model model) {
        try {
            // ID del sensor de temperatura
            String sensorId = "660b2f33-d407-4c37-9224-9b9cefa6e33a";

            // Obtener el valor actual de la temperatura
            Variable temperatura = casaDomoticaService.obtenerEstadoLuzDirectamente(sensorId);

            // Redondear el valor de la temperatura a 1 decimal
            BigDecimal temperaturaRedondeada = new BigDecimal(temperatura.getValorActual())
                    .setScale(1, RoundingMode.HALF_UP);

            // Formatear la fecha de última actualización
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String ultimaActualizacionFormateada = temperatura.getFechaActualizacion().format(formatter);

            // Pasar el valor de la temperatura redondeada y la fecha formateada al modelo
            model.addAttribute("temperatura", temperaturaRedondeada.toString());
            model.addAttribute("ultimaActualizacion", ultimaActualizacionFormateada);
        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener el valor de la temperatura: " + e.getMessage());
        }
        return "monitoreo_temperatura"; // Nombre de la vista Thymeleaf que mostrará la temperatura
    }



    @GetMapping("/controlGaraje")
    public String mostrarControlGaraje(Model model) {
        try {
            // Obtener el estado actual del garaje (como "abierto" o "cerrado")
            Variable estadoGarajeVariable = casaDomoticaService.obtenerEstadoLuzDirectamente("fcea01ae-0054-4f3d-8f04-58d19dfcb9ec");
            String estadoGaraje = estadoGarajeVariable.getValorActual().equals("true") ? "Abierto" : "Cerrado";

            // Añadir estado al modelo
            model.addAttribute("estadoGaraje", estadoGaraje);
        } catch (Exception e) {
            model.addAttribute("estadoGaraje", "Desconocido");
            model.addAttribute("error", "Error al obtener el estado del garaje: " + e.getMessage());
        }
        model.addAttribute("titulo", "Estado del Garaje");
        return "control_garaje"; // Nombre del archivo Thymeleaf para el formulario de control
    }



    @PostMapping("/cambiarEstadoGaraje")
    @ResponseBody
    public Map<String, Object> cambiarEstadoGaraje(@RequestParam String estado) {
        Map<String, Object> response = new HashMap<>();
        try {

            boolean estadoGaraje = estado.equalsIgnoreCase("abierto");

            casaDomoticaService.cambiarEstadoGaraje(estadoGaraje);

            messagingTemplate.convertAndSend("/topic/estadoGaraje", estado);

            // Devolver el estado actualizado en la respuesta
            response.put("success", true);
            response.put("estado", estadoGaraje ? "Abierto" : "Cerrado");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el estado del garaje: " + e.getMessage());
        }
        return response;
    }

    public void enviarEstadoGaraje(String estado) {
        messagingTemplate.convertAndSend("/topic/estadoGaraje", estado);
    }


    @GetMapping("/controlModoNoche")
    public String mostrarControlModoNoche(Model model) {
        try {
            // IDs de las luces para Modo Noche
            String luzHallId = "548f7678-7924-4b0d-b235-9e8374c0e3c7"; // luzdehall
            String luzPasadizoId = "8c9b2e93-2902-41b8-8c6c-ca4fae7a27b1"; // luzdepasadizo

            // Obtener el estado actual de cada luz
            Variable luzHall = casaDomoticaService.obtenerEstadoLuzDirectamente(luzHallId);
            Variable luzPasadizo = casaDomoticaService.obtenerEstadoLuzDirectamente(luzPasadizoId);

            // Determinar si el Modo Noche está activado (si ambas luces están encendidas)
            boolean modoNocheActivo = luzHall.getValorActual().equals("true") && luzPasadizo.getValorActual().equals("true");

            // Pasar el estado del Modo Noche a la vista
            model.addAttribute("modoNocheActivo", modoNocheActivo);
            model.addAttribute("titulo", "Control de Modo Noche");

        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener el estado de las luces: " + e.getMessage());
        }
        return "control_modo_noche";
    }


    @PostMapping("/cambiarModoNoche")
    @ResponseBody
    public Map<String, Object> cambiarModoNoche(@RequestParam("activar") boolean activar) {
        Map<String, Object> response = new HashMap<>();
        try {
            casaDomoticaService.cambiarModoNoche(activar);
            response.put("success", true);
            response.put("message", activar ? "Modo Noche activado correctamente." : "Modo Noche desactivado correctamente.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error en Modo Noche: " + e.getMessage());
        }
        return response;
    }


    @GetMapping("/alertaGas")
    public String mostrarAlertaGas(Model model) {
        try {
            // ID del sensor de gas
            String sensorGasId = "30b947f8-bbe1-426b-9758-82da51426d13"; // Reemplaza con el ID real del sensor de gas
            Variable nivelGas = casaDomoticaService.obtenerEstadoLuzDirectamente(sensorGasId);

            // Pasar el valor actual del gas al modelo
            model.addAttribute("nivelGas", nivelGas.getValorActual());
            model.addAttribute("ultimaActualizacion", nivelGas.getFechaActualizacion());

        } catch (Exception e) {
            model.addAttribute("error", "Error al obtener el estado del gas: " + e.getMessage());
        }
        return "estado_gas";
    }


    @GetMapping("/alertaPuerta")
    public String mostrarAlertaPuerta(Model model) {
        try {
            Variable estadoPuerta = casaDomoticaService.obtenerEstadoLuzDirectamente("eedc0c64-c8e4-49c2-8778-a3c7f88268e5"); // Reemplaza con el ID real
            boolean puertaAbierta = Boolean.parseBoolean(estadoPuerta.getValorActual());

            model.addAttribute("titulo", "Estado de la Puerta");
            model.addAttribute("estadoPuerta", puertaAbierta ? "Abierta" : "Cerrada"); // Envía el estado como "Abierta" o "Cerrada"
        } catch (Exception e) {
            model.addAttribute("estadoPuerta", "Desconocido");
            model.addAttribute("error", "Error al obtener el estado de la puerta: " + e.getMessage());
        }
        return "alerta_puerta";
    }


    @PostMapping("/cambiarEstadoPuerta")
    @ResponseBody
    public Map<String, Object> cambiarEstadoPuerta(@RequestParam boolean abrir) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean nuevoEstado = abrir;
            casaDomoticaService.cambiarEstadoPuertaMetodo("eedc0c64-c8e4-49c2-8778-a3c7f88268e5", nuevoEstado);
            response.put("success", true);
            response.put("estado", nuevoEstado ? "Abierta" : "Cerrada");
            response.put("message", "La puerta ha sido " + (nuevoEstado ? "abierta" : "cerrada") + " correctamente.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al cambiar el estado de la puerta: " + e.getMessage());
        }
        return response;
    }

}

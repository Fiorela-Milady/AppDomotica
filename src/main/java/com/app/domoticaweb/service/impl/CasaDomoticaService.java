package com.app.domoticaweb.service.impl;

import com.app.domoticaweb.model.HorarioAlarma;
import com.app.domoticaweb.model.HorarioLucesExterior;
import com.app.domoticaweb.model.Variable;
import com.app.domoticaweb.repository.HorarioAlarmaRepository;
import com.app.domoticaweb.repository.HorarioLucesExteriorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CasaDomoticaService {

    private final HorarioLucesExteriorRepository horarioLucesExteriorRepository;
    private final HorarioAlarmaRepository horarioAlarmaRepository;

    private static final String CLIENT_ID = "kqMvXamUD1CRACmIEx05lBNtMDxSfbgU";
    private static final String CLIENT_SECRET = "QhNbsPb5w8wjlo1u6TVp6tJKVv9k7sP5U1LQgOWroFrLe7ZAjlNVbuCNV12xivZ9";
    private static final String TOKEN_URL = "https://api2.arduino.cc/iot/v1/clients/token";
    private static final String THING_ID = "686fbea0-3d07-4456-a619-7a4e8a4a1e3c";
    private static final String PROPERTIES_URL = "https://api2.arduino.cc/iot/v2/things/" + THING_ID + "/properties";
    private static final String PROPERTIES_URL_TEMPLATE = "https://api2.arduino.cc/iot/v2/things/" + THING_ID + "/properties/";

    private static final String SENSOR_GAS_ID = "30b947f8-bbe1-426b-9758-82da51426d13";
    private static final double UMBRAL_SEGURIDAD_GAS = 5.0;
    private static final String SENSOR_PUERTA_ID = "eedc0c64-c8e4-49c2-8778-a3c7f88268e5";

    private String estadoGarajeAnterior = null;
    private final SimpMessagingTemplate messagingTemplate;


    private final List<String> dispositivosIds = List.of(
            "8f65fb38-75d6-467a-9b49-e8b99ed5dbe4", // aire
            "fcea01ae-0054-4f3d-8f04-58d19dfcb9ec", // garage
            "81852875-e379-4728-aaa4-4f242e42ae6c", // luzdebar
            "642559f8-9860-436d-bcbb-f2c1e757cc22", // luzdecochera
            "1075f576-9050-4a10-8aaa-ea1d64f329b2", // luzdecocina
            "d96a05cc-3f23-4e74-affc-863b5c781712", // luzdehabitacion
            "548f7678-7924-4b0d-b235-9e8374c0e3c7", // luzdehall
            "8c9b2e93-2902-41b8-8c6c-ca4fae7a27b1", // luzdepasadizo
            "0fb121de-64b7-40d0-883d-833cd0dbf2be", // luzdesala
            "8323730c-1f02-4633-82a2-7fda6e7fe05f"  // teve
    );

    public static String getAccessToken() throws Exception {
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String body = "grant_type=client_credentials" +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&audience=https://api2.arduino.cc/iot";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
            os.flush();
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        JSONObject json = new JSONObject(content.toString());
        return json.getString("access_token");
    }

    private String makeGetRequest(String urlString, String accessToken) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        return content.toString();
    }

    public List<Variable> listarVariables() throws Exception {
        String accessToken = getAccessToken();
        String response = makeGetRequest(PROPERTIES_URL, accessToken);

        JSONArray jsonArray = new JSONArray(response);
        List<Variable> variables = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonVariable = jsonArray.getJSONObject(i);
            variables.add(convertirAVariable(jsonVariable));
        }

        return variables;
    }

    @Scheduled(cron = "0 * * * * *")
    public void verificarEstadoLuces() {
        HorarioLucesExterior horario = horarioLucesExteriorRepository.findById(5L).orElse(null);
        System.out.println("horario = " + horario);
        if (horario == null) {
            return;
        }

        LocalTime horaActual = LocalTime.now();
        boolean activarLuces = shouldActivateLights(horario.getHoraEncendido(), horario.getHoraApagado(), horaActual);

        actualizarEstadoLucesExterior(activarLuces);
    }

    public void guardarHorarioLuces(HorarioLucesExterior horarioLucesExterior) {
        Long variableId = horarioLucesExterior.getVariable().getId();
        HorarioLucesExterior horarioExistente = horarioLucesExteriorRepository.findByVariableId(variableId);

        if (horarioExistente != null) {
            horarioExistente.setHoraEncendido(horarioLucesExterior.getHoraEncendido());
            horarioExistente.setHoraApagado(horarioLucesExterior.getHoraApagado());
            horarioExistente.setActivo(true);
            horarioLucesExteriorRepository.save(horarioExistente);
        } else {
            horarioLucesExteriorRepository.save(horarioLucesExterior);
        }
    }

    private boolean shouldActivateLights(LocalTime horaEncendido, LocalTime horaApagado, LocalTime horaActual) {
        if (horaEncendido == null && horaApagado == null) {
            return false;
        }
        if (horaEncendido != null && horaApagado == null) {
            return horaActual.isAfter(horaEncendido);
        }
        if (horaEncendido == null && horaApagado != null) {
            return horaActual.isBefore(horaApagado);
        }
        if (horaEncendido.isBefore(horaApagado)) {
            return horaActual.isAfter(horaEncendido) && horaActual.isBefore(horaApagado);
        } else {
            return horaActual.isAfter(horaEncendido) || horaActual.isBefore(horaApagado);
        }
    }

    public void actualizarEstadoLucesExterior(boolean encender) {
        System.out.println("activarLuces = " + encender);
        try {
            String accessToken = getAccessToken();
            String propertyId = "d9d3b2fc-bfaa-41a3-b5df-b5bb232fa9a1";
            String url = "https://api2.arduino.cc/iot/v2/things/" + THING_ID + "/properties/" + propertyId + "/publish";
            hacerPutRequest(url, accessToken, encender);
        } catch (Exception e) {
        }
    }

    private void hacerPutRequest(String urlString, String accessToken, boolean valor) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{\"value\": " + valor + "}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            System.out.println("Solicitud PUT exitosa: valor actualizado.");
        } else {
            System.out.println("Error en la solicitud PUT: Código de respuesta " + responseCode);
        }
    }

    public HorarioLucesExterior buscarHorarioLucesExteriorId(Long variableId) {
        return horarioLucesExteriorRepository.findByVariableId(variableId);
    }


    @Scheduled(cron = "0 * * * * *")
    public void verificarEstadoAlarma() {
        Long idVariableAlarma = 15L;

        HorarioAlarma horarioAlarma = horarioAlarmaRepository.findByVariableId(idVariableAlarma);

        if (horarioAlarma == null) {
            return;
        }

        LocalTime horaActual = LocalTime.now().withSecond(0).withNano(0);

        if (horaActual.isAfter(horarioAlarma.getHoraInicio()) && horaActual.isBefore(horarioAlarma.getHoraFin())) {
            boolean todosApagados = verificarTodosDispositivosApagados();
            if (todosApagados && !horarioAlarma.getActivo()) {
                desactivarAlarma();
            } else if (todosApagados && horarioAlarma.getActivo()) {
                activarAlarma();
            }
        } else {
            System.out.println("No está dentro del rango de tiempo");
        }
    }

    private void desactivarAlarma() {
        actualizarEstadoAlarmaEnArduino(false);
    }

    private void activarAlarma() {
        actualizarEstadoAlarmaEnArduino(true);
    }

    private boolean verificarTodosDispositivosApagados() {
        for (String dispositivoId : dispositivosIds) {
            boolean estadoDispositivo = obtenerEstadoDispositivo(dispositivoId);
            System.out.println("estado Dispositivo = " + estadoDispositivo);
            if (estadoDispositivo) {
                return true;
            }
        }
        return false;
    }

    private boolean obtenerEstadoDispositivo(String dispositivoId) {
        try {
            String accessToken = getAccessToken();
            String url = PROPERTIES_URL_TEMPLATE + dispositivoId;
            String response = makeGetRequest(url, accessToken);

            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getBoolean("last_value");
        } catch (Exception e) {
            System.out.println("Error al obtener el estado del dispositivo " + dispositivoId + ": " + e.getMessage());
            return false;
        }
    }

    private void actualizarEstadoAlarmaEnArduino(boolean estado) {
        try {
            String accessToken = getAccessToken();
            String alarmaPropertyId = "9771edc0-bbd8-412a-9bc9-dd2921dcc409";
            String url = "https://api2.arduino.cc/iot/v2/things/" + THING_ID + "/properties/" + alarmaPropertyId + "/publish";
            hacerPutRequest(url, accessToken, estado);
        } catch (Exception e) {
            System.out.println("Error al actualizar el estado de la alarma en Arduino: " + e.getMessage());
        }
    }

    public void guardarOActualizarHorarioAlarma(Variable variable, LocalTime horaInicio, LocalTime horaFin, Boolean activo) {

        HorarioAlarma horarioAlarmaExistente = horarioAlarmaRepository.findByVariableId(variable.getId());

        if (horarioAlarmaExistente != null) {
            horarioAlarmaExistente.setHoraInicio(horaInicio);
            horarioAlarmaExistente.setHoraFin(horaFin);
            horarioAlarmaExistente.setActivo(activo);
            horarioAlarmaExistente.setFechaActualizacion(LocalDateTime.now());
            horarioAlarmaRepository.save(horarioAlarmaExistente);
        } else {
            HorarioAlarma nuevoHorarioAlarma = HorarioAlarma.builder()
                    .variable(variable)
                    .horaInicio(horaInicio)
                    .horaFin(horaFin)
                    .activo(activo)
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .build();
            horarioAlarmaRepository.save(nuevoHorarioAlarma);
        }
    }

    public HorarioAlarma buscarHorarioAlarmaPorVariableId(Long variableId) {
        return horarioAlarmaRepository.findByVariableId(variableId);
    }

    public Variable obtenerEstadoLuzDirectamente(String luzId) throws Exception {
        String accessToken = getAccessToken();
        String url = PROPERTIES_URL_TEMPLATE + luzId;

        String response = makeGetRequest(url, accessToken);
        JSONObject jsonResponse = new JSONObject(response);
        return convertirAVariable(jsonResponse);
    }

    public void cambiarEstadoLuz(String luzId, boolean encender) throws Exception {
        System.out.println("encender = " + encender);
        String accessToken = getAccessToken();
        String url = PROPERTIES_URL_TEMPLATE + luzId + "/publish";
        hacerPutRequest(url, accessToken, encender);
    }

    public void cambiarEstadoPuertaMetodo(String luzId, boolean encender) throws Exception {
        System.out.println("estado puerta = " + encender);
        String accessToken = getAccessToken();
        String url = PROPERTIES_URL_TEMPLATE + luzId + "/publish";
        hacerPutRequest(url, accessToken, encender);
    }

    @PostConstruct
    public void iniciarMonitoreo() {
        iniciarMonitoreoGaraje();
    }


    public void cambiarEstadoGaraje(boolean nuevoEstado) throws Exception {
        String accessToken = getAccessToken();
        String url = "https://api2.arduino.cc/iot/v2/things/" + THING_ID + "/properties/fcea01ae-0054-4f3d-8f04-58d19dfcb9ec/publish";

        hacerPutRequest(url, accessToken, nuevoEstado);
        String estadoActual = nuevoEstado ? "abierto" : "cerrado";

        if (estadoGarajeAnterior == null || !estadoGarajeAnterior.equals(estadoActual)) {
            estadoGarajeAnterior = estadoActual;
            messagingTemplate.convertAndSend("/topic/estadoGaraje", estadoActual);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void iniciarMonitoreoGaraje() {
        System.out.println("garajeeeeee ");
        try {

            Variable estadoGarajeVariable = obtenerEstadoLuzDirectamente("fcea01ae-0054-4f3d-8f04-58d19dfcb9ec");
            String estadoActual = estadoGarajeVariable.getValorActual().equals("true") ? "abierto" : "cerrado";

            if (estadoGarajeAnterior == null || !estadoGarajeAnterior.equals(estadoActual)) {
                estadoGarajeAnterior = estadoActual;

                messagingTemplate.convertAndSend("/topic/estadoGaraje", estadoActual);
            }
        } catch (Exception e) {
            System.out.println("Error al verificar el estado del garaje: " + e.getMessage());
        }
    }

    public void cambiarModoNoche(boolean activar) {
        String luzHallId = "548f7678-7924-4b0d-b235-9e8374c0e3c7"; // ID de luzdehall
        String luzPasadizoId = "8c9b2e93-2902-41b8-8c6c-ca4fae7a27b1"; // ID de luzdepasadizo

        try {
            cambiarEstadoLuz(luzHallId, activar);
            cambiarEstadoLuz(luzPasadizoId, activar);
        } catch (Exception e) {
            System.out.println("Error en Modo Noche: alguna luz no está disponible." + e.getMessage());
        }
    }


    @Scheduled(fixedRate = 10000)
    public void verificarNivelGas() throws Exception {
        try {
            Variable nivelGas = obtenerEstadoLuzDirectamente(SENSOR_GAS_ID);
            double valorGas = Double.parseDouble(nivelGas.getValorActual());
            if (valorGas > UMBRAL_SEGURIDAD_GAS) {
                enviarNotificacionAlertaGas("Alerta: Nivel de gas peligroso detectado.");
            }
        } catch (Exception e) {
            throw new Exception("Error en Modo Noche: alguna luz no está disponible.");
        }
    }

    public void enviarNotificacionAlertaGas(String mensaje) {
        messagingTemplate.convertAndSend("/topic/alertaGas", mensaje);
        System.out.println(mensaje);
    }


    @Scheduled(cron = "0 * * * * *")
    public void verificarPuertaAbierta() {
        try {
            Variable puertaAbierta = obtenerEstadoLuzDirectamente(SENSOR_PUERTA_ID);
            boolean estadoPuerta = Boolean.parseBoolean(puertaAbierta.getValorActual());

            if (estadoPuerta) {
                enviarNotificacionAlertaPuerta("La puerta está abierta.");
            }
            enviarEstadoPuerta(estadoPuerta ? "abierta" : "cerrada");

        } catch (Exception e) {
            System.out.println("Error al verificar el estado de la puerta: " + e.getMessage());
        }
    }

    public void enviarEstadoPuerta(String estado) {
        messagingTemplate.convertAndSend("/topic/estadoPuerta", estado);
    }

    public void enviarNotificacionAlertaPuerta(String mensaje) {
        messagingTemplate.convertAndSend("/topic/alertaPuerta", mensaje);
        System.out.println("Notificación enviada: " + mensaje);
    }

    @Scheduled(cron = "0 * * * * *")
    public void verificarCierrePuerta()  {
        try {
            Variable estadoPuerta = obtenerEstadoLuzDirectamente(SENSOR_PUERTA_ID);
            boolean puertaCerrada = !Boolean.parseBoolean(estadoPuerta.getValorActual());

            if (puertaCerrada) {
                enviarNotificacionCierrePuerta("La puerta está cerrada y asegurada.", LocalDateTime.now());
            }
        } catch (Exception e) {
            System.out.println("Error al verificar el estado de la puerta: " + e.getMessage());
        }
    }

    public void enviarNotificacionCierrePuerta(String mensaje, LocalDateTime horaCierre) {
        String mensajeConHora = mensaje + " Hora de cierre: " + horaCierre.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        messagingTemplate.convertAndSend("/topic/notificacionCierrePuerta", mensajeConHora);
    }

    public Variable convertirAVariable(JSONObject jsonVariable) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        LocalDateTime updatedAt = LocalDateTime.parse(jsonVariable.getString("updated_at"), formatter);
        LocalDateTime valueUpdatedAt = LocalDateTime.parse(jsonVariable.getString("value_updated_at"), formatter);

        LocalDateTime fechaActualizacion = updatedAt.isAfter(valueUpdatedAt) ? updatedAt : valueUpdatedAt;

        return Variable.builder()
                .nombre(jsonVariable.getString("name"))
                .idArduino(jsonVariable.getString("id"))
                .descripcion(jsonVariable.optString("description", ""))
                .valorActual(String.valueOf(jsonVariable.get("last_value")))
                .fechaActualizacion(fechaActualizacion)
                .build();
    }
}

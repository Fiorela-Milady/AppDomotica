package com.app.domoticaweb;

import com.app.domoticaweb.controller.CasaDomoticaController;
import com.app.domoticaweb.model.HorarioLucesExterior;
import com.app.domoticaweb.model.Variable;
import com.app.domoticaweb.service.impl.CasaDomoticaService;
import com.app.domoticaweb.repository.VariableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CasaDomoticaControllerTest {

    @InjectMocks
    private CasaDomoticaController casaDomoticaController;

    @Mock
    private CasaDomoticaService casaDomoticaService;

    @Mock
    private VariableRepository variableRepository;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks antes de cada prueba
    }

    /**
     * Prueba para verificar que el formulario de configuración de luces
     * agrega correctamente los atributos al modelo y retorna la vista adecuada.
     */
    @Test
    void testMostrarConfiguracionLuces() {
        HorarioLucesExterior horarioMock = HorarioLucesExterior.builder()
                .horaEncendido(LocalTime.of(18, 0))
                .horaApagado(LocalTime.of(6, 0))
                .build();

        when(casaDomoticaService.buscarHorarioLucesExteriorId(3L)).thenReturn(horarioMock);

        String viewName = casaDomoticaController.mostrarConfiguracionLuces(model);

        verify(model).addAttribute(eq("horario"), any(HorarioLucesExterior.class));
        verify(model).addAttribute(eq("horaEncendidoFormatted"), eq("18:00"));
        verify(model).addAttribute(eq("horaApagadoFormatted"), eq("06:00"));
        assertEquals("configurar_luces_ADMIN", viewName);
    }

    /**
     * Prueba para guardar los horarios de luces.
     */
    @Test
    void testGuardarHorarioLuces() {
        Variable variableMock = new Variable();
        when(variableRepository.findById(3L)).thenReturn(Optional.of(variableMock));

        String viewName = casaDomoticaController.guardarHorarioLuces(
                true,
                true,
                "18:00",
                "06:00"
        );

        verify(casaDomoticaService).guardarHorarioLuces(any(HorarioLucesExterior.class));
        assertEquals("redirect:/domotica/verConfigurarLuces", viewName);
    }

    /**
     * Prueba para listar las variables.
     */
    @Test
    void testListarVariables() {
        String viewName = casaDomoticaController.listarVariables(model);

        verify(model).addAttribute(eq("title"), eq("Configurar Horario de Luces Exteriores"));
        assertEquals("variables_listado_ADMIN", viewName);
    }

    /**
     * Prueba para cambiar el estado de una luz.
     */
    @Test
    void testCambiarEstadoLuz() throws Exception {
        String luzId = "test-luz-id";
        boolean encender = true;

        var response = casaDomoticaController.cambiarEstadoLuz(luzId, encender);

        verify(casaDomoticaService).cambiarEstadoLuz(eq(luzId), eq(encender));
        assertEquals("true", response.get("nuevoEstado"));
        assertEquals("El estado de la luz ha sido actualizado correctamente.", response.get("message"));
        assertEquals(true, response.get("success"));
    }

    /**
     * Prueba para cambiar el estado de la puerta.
     */
    @Test
    void testCambiarEstadoPuerta() throws Exception {
        boolean abrir = true;

        var response = casaDomoticaController.cambiarEstadoPuerta(abrir);

        verify(casaDomoticaService).cambiarEstadoLuz(eq("eedc0c64-c8e4-49c2-8778-a3c7f88268e5"), eq(abrir));
        assertEquals("Abierta", response.get("estado"));
        assertEquals("La puerta ha sido abierta correctamente.", response.get("message"));
        assertEquals(true, response.get("success"));
    }
}

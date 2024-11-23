package com.app.domoticaweb;

import com.app.domoticaweb.controller.PermisoController;
import com.app.domoticaweb.model.Permiso;
import com.app.domoticaweb.model.TipoPermiso;
import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.model.Variable;
import com.app.domoticaweb.service.PermisoService;
import com.app.domoticaweb.service.UsuarioService;
import com.app.domoticaweb.service.VariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PermisoControllerTest {

    @InjectMocks
    private PermisoController permisoController;

    @Mock
    private PermisoService permisoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private VariableService variableService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verifica que el método listarPermisos agrega los atributos necesarios al modelo
     * y retorna la vista "gestionar_permisos".
     */
    @Test
    void testListarPermisos() {
        List<Permiso> permisos = new ArrayList<>();
        List<Usuario> usuarios = new ArrayList<>();
        List<Variable> variables = new ArrayList<>();

        when(permisoService.obtenerTodosPermisos()).thenReturn(permisos);
        when(usuarioService.listarUsuarios()).thenReturn(usuarios);
        when(variableService.obtenerTodasLasVariables()).thenReturn(variables);

        String viewName = permisoController.listarPermisos(model);

        verify(model).addAttribute("permisos", permisos);
        verify(model).addAttribute("usuarios", usuarios);
        verify(model).addAttribute("variables", variables);
        assertEquals("gestionar_permisos", viewName);
    }

    /**
     * Verifica que el método obtenerPermisos retorna la lista de permisos correctamente.
     */
    @Test
    void testObtenerPermisos() {
        List<Permiso> permisos = new ArrayList<>();
        when(permisoService.obtenerTodosPermisos()).thenReturn(permisos);

        List<Permiso> resultado = permisoController.obtenerPermisos();

        assertEquals(permisos, resultado);
        verify(permisoService).obtenerTodosPermisos();
    }

    /**
     * Verifica que el método asignarPermiso asigna correctamente un permiso
     * cuando no existe conflicto.
     */
    @Test
    void testAsignarPermisoSuccess() {
        Long usuarioId = 1L;
        Long variableId = 1L;
        TipoPermiso tipoPermiso = TipoPermiso.LECTURA;

        Usuario usuarioMock = new Usuario();
        Variable variableMock = new Variable();

        when(permisoService.existePermiso(usuarioId, variableId)).thenReturn(false);
        when(usuarioService.obtenerUsuarioPorId(usuarioId)).thenReturn(usuarioMock);
        when(variableService.obtenerVariablePorId(variableId)).thenReturn(Optional.of(variableMock));

        ResponseEntity<String> response = permisoController.asignarPermiso(usuarioId, variableId, tipoPermiso);

        verify(permisoService).crearPermiso(any(Permiso.class));
        assertEquals("Permiso asignado correctamente", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }

    /**
     * Verifica que el método asignarPermiso retorna un conflicto HTTP (409)
     * cuando ya existe el permiso.
     */
    @Test
    void testAsignarPermisoConflict() {
        Long usuarioId = 1L;
        Long variableId = 1L;
        TipoPermiso tipoPermiso = TipoPermiso.LECTURA;

        when(permisoService.existePermiso(usuarioId, variableId)).thenReturn(true);

        ResponseEntity<String> response = permisoController.asignarPermiso(usuarioId, variableId, tipoPermiso);

        verify(permisoService, never()).crearPermiso(any(Permiso.class));
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("El usuario ya tiene asignado este permiso para la variable seleccionada.", response.getBody());
    }

    /**
     * Verifica que el método asignarPermiso lanza una excepción cuando la variable no se encuentra.
     */
    @Test
    void testAsignarPermisoVariableNotFound() {
        Long usuarioId = 1L;
        Long variableId = 1L;
        TipoPermiso tipoPermiso = TipoPermiso.LECTURA;

        when(permisoService.existePermiso(usuarioId, variableId)).thenReturn(false);
        when(variableService.obtenerVariablePorId(variableId)).thenReturn(Optional.empty());

        try {
            permisoController.asignarPermiso(usuarioId, variableId, tipoPermiso);
        } catch (RuntimeException e) {
            assertEquals("Variable no encontrada", e.getMessage());
        }

        verify(permisoService, never()).crearPermiso(any(Permiso.class));
    }

    /**
     * Verifica que el método eliminarPermiso elimina correctamente un permiso.
     */
    @Test
    void testEliminarPermiso() {
        Long permisoId = 1L;

        ResponseEntity<String> response = permisoController.eliminarPermiso(permisoId);

        verify(permisoService).eliminarPermiso(permisoId);
        assertEquals("Permiso eliminado correctamente", response.getBody());
        assertEquals(200, response.getStatusCodeValue());
    }
}

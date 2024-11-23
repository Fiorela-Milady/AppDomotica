package com.app.domoticaweb;

import com.app.domoticaweb.controller.UsuarioController;
import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UsuarioControllerTest {

    @InjectMocks
    private UsuarioController usuarioController;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
    }

    /**
     * Verifica que el formulario para crear un usuario retorna la vista correcta
     * y añade los atributos necesarios al modelo.
     */
    @Test
    void testCrearUsuarioForm() {
        String viewName = usuarioController.crearUsuarioForm(model);

        verify(model).addAttribute(eq("usuario"), any(Usuario.class));
        verify(model).addAttribute("titulo", "Crear Usuario");
        verify(model).addAttribute("nombre_boton", "Guardar Usuario");

        assertEquals("crear_usuario", viewName);
    }

    /**
     * Verifica que un usuario nuevo se guarda correctamente.
     */
    @Test
    void testGuardarUsuarioNuevo() {
        Usuario usuario = new Usuario();
        usuario.setId(null); // Simula que es un nuevo usuario

        String viewName = usuarioController.guardarUsuario(usuario);

        verify(usuarioService).crearUsuario(usuario);
        assertEquals("redirect:/admin/gestionUsuarios", viewName);
    }

    /**
     * Verifica que un usuario existente se actualiza correctamente.
     */
    @Test
    void testGuardarUsuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Nuevo Nombre");

        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(1L);
        usuarioExistente.setNombre("Nombre Antiguo");

        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuarioExistente);

        String viewName = usuarioController.guardarUsuario(usuario);

        verify(usuarioService).actualizarUsuario(usuarioExistente);
        assertEquals("redirect:/admin/gestionUsuarios", viewName);
    }

    /**
     * Verifica que el formulario para editar un usuario retorna la vista correcta
     * y añade los atributos necesarios al modelo.
     */
    @Test
    void testEditarUsuarioForm() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);

        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(usuarioMock);

        String viewName = usuarioController.editarUsuarioForm(1L, model);

        verify(model).addAttribute("usuario", usuarioMock);
        verify(model).addAttribute("titulo", "Editar Usuario");
        verify(model).addAttribute("nombre_boton", "Actualizar Usuario");

        assertEquals("crear_usuario", viewName);
    }

    /**
     * Verifica que un usuario se elimina correctamente.
     */
    @Test
    void testEliminarUsuario() {
        String viewName = usuarioController.eliminarUsuario(1L);

        verify(usuarioService).eliminarUsuario(1L);
        assertEquals("redirect:/admin/gestionUsuarios", viewName);
    }

    /**
     * Verifica que la vista de gestión de usuarios retorna la lista de usuarios
     * correctamente y la vista adecuada.
     */
    @Test
    void testGestionUsuarios() {
        String viewName = usuarioController.gestionUsuarios(model);

        verify(model).addAttribute(eq("usuarios"), anyList());
        assertEquals("gestionar_usuarios", viewName);
    }
}

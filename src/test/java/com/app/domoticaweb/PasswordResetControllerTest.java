package com.app.domoticaweb;

import com.app.domoticaweb.controller.PasswordResetController;
import com.app.domoticaweb.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {

    @InjectMocks
    private PasswordResetController passwordResetController;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Inicializa los mocks
    }

    /**
     * Verifica que el formulario de restablecimiento de contraseña retorna la vista "reset".
     */
    @Test
    void testResetPasswordForm() {
        String viewName = passwordResetController.resetPasswordForm();
        assertEquals("reset", viewName);
    }

    /**
     * Verifica que, cuando el correo es válido, se muestra un mensaje de éxito en el modelo
     * y se retorna la vista "reset".
     */
    @Test
    void testResetPasswordSuccess() {
        String correo = "test@correo.com";
        String mensajeExito = "Correo de recuperación enviado exitosamente.";

        when(usuarioService.enviarCorreoRecuperacion(correo)).thenReturn(mensajeExito);

        String viewName = passwordResetController.resetPassword(correo, model);

        verify(usuarioService).enviarCorreoRecuperacion(correo);
        verify(model).addAttribute("correcto", mensajeExito);
        assertEquals("reset", viewName);
    }

    /**
     * Verifica que, cuando el correo no es válido, se muestra un mensaje de error en el modelo
     * y se retorna la vista "reset".
     */
    @Test
    void testResetPasswordFailure() {
        String correo = "invalid@correo.com";
        String mensajeError = "El correo no está registrado.";

        when(usuarioService.enviarCorreoRecuperacion(correo)).thenReturn(mensajeError);

        String viewName = passwordResetController.resetPassword(correo, model);

        verify(usuarioService).enviarCorreoRecuperacion(correo);
        verify(model).addAttribute("error", mensajeError);
        assertEquals("reset", viewName);
    }
}

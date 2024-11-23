package com.app.domoticaweb;

import com.app.domoticaweb.controller.AuthController;
import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private SessionStatus sessionStatus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Verifica que el formulario de login devuelva la vista "login".
     */
    @Test
    void testLoginForm() {
        String viewName = authController.loginForm();
        assertEquals("login", viewName);
    }

    /**
     * Verifica que un usuario autenticado correctamente se redirija al panel
     * y se agregue a la sesión.
     */
    @Test
    void testAuthenticateSuccess() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo("test@correo.com");

        when(usuarioService.autenticar("test@correo.com", "password123")).thenReturn(usuarioMock);

        String viewName = authController.authenticate("test@correo.com", "password123", model, session);

        verify(session).setAttribute("usuario", usuarioMock);
        assertEquals("panel", viewName);
    }

    /**
     * Verifica que, con credenciales inválidas, se regrese a la vista "login"
     * y se muestre un mensaje de error.
     */
    @Test
    void testAuthenticateFailure() {
        when(usuarioService.autenticar("test@correo.com", "wrongpassword")).thenReturn(null);

        String viewName = authController.authenticate("test@correo.com", "wrongpassword", model, session);

        verify(model).addAttribute("error", "Credenciales inválidas");
        assertEquals("login", viewName);
    }

    /**
     * Verifica que si un usuario está presente en la sesión,
     * se retorne la vista "panel".
     */
    @Test
    void testPanelWithUserInSession() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo("test@correo.com");

        when(session.getAttribute("usuario")).thenReturn(usuarioMock);

        String viewName = authController.panel(session);

        assertEquals("panel", viewName);
    }

    /**
     * Verifica que si no hay un usuario en la sesión,
     * se redirija a la página de inicio de sesión.
     */
    @Test
    void testPanelWithoutUserInSession() {
        when(session.getAttribute("usuario")).thenReturn(null);

        String viewName = authController.panel(session);

        assertEquals("redirect:/", viewName);
    }

    /**
     * Verifica que el método logout invalide la sesión
     * y redirija a la página de inicio de sesión.
     */
    @Test
    void testLogout() {
        String viewName = authController.logout(sessionStatus, session);

        verify(session).invalidate();
        assertEquals("redirect:/", viewName);
    }
}

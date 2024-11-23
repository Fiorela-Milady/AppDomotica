package com.app.domoticaweb.controller;

import com.app.domoticaweb.model.Rol;
import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @ModelAttribute("usuario")
    public Usuario usuario() {
        return new Usuario();
    }

    @GetMapping("/")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/panel")
    public String panel(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            System.out.println("Usuario no encontrado en la sesión");
            return "redirect:/";
        }
        System.out.println("Usuario en sesión: " + usuario.getCorreo());
        return "panel";
    }


    @PostMapping("/login")
    public String authenticate(@RequestParam String correo, @RequestParam String password, Model model, HttpSession session) {
        Usuario usuario = usuarioService.autenticar(correo, password);

        if (usuario == null) {
            model.addAttribute("error", "Credenciales inválidas");
            return "login";
        }
        session.setAttribute("usuario", usuario);
        return "panel";
    }


    @GetMapping("/logout")
    public String logout(SessionStatus status, HttpSession session) {

        session.invalidate();
        return "redirect:/";
    }
}

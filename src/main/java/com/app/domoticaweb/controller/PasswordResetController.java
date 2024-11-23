package com.app.domoticaweb.controller;

import com.app.domoticaweb.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/password")
public class PasswordResetController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/reset")
    public String resetPasswordForm() {
        return "reset";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String correo, Model model) {
        String resultado = usuarioService.enviarCorreoRecuperacion(correo);

        if (resultado.equals("Correo de recuperación enviado exitosamente.")) {
            model.addAttribute("correcto", resultado);
        } else {
            model.addAttribute("error", resultado);
        }

        return "reset";
    }
}

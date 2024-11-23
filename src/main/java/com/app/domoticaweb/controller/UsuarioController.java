package com.app.domoticaweb.controller;

import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    @GetMapping("/crear")
    public String crearUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("titulo", "Crear Usuario");
        model.addAttribute("nombre_boton", "Guardar Usuario");
        return "crear_usuario";
    }

    @PostMapping("/usuario/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {

        if (usuario != null && usuario.getId() != null) {
            Usuario usuarioExistente = usuarioService.obtenerUsuarioPorId(usuario.getId());

            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                usuarioExistente.setPassword(usuario.getPassword());
            }

            usuarioExistente.setNombre(usuario.getNombre());
            usuarioExistente.setCorreo(usuario.getCorreo());
            usuarioExistente.setTelefono(usuario.getTelefono());
            usuarioExistente.setRol(usuario.getRol());
            usuarioExistente.setStatus(usuario.getStatus());

            usuarioService.actualizarUsuario(usuarioExistente);

        } else {
            usuarioService.crearUsuario(usuario);
        }

        return "redirect:/admin/gestionUsuarios";
    }


    @GetMapping("/usuario/editar/{id}")
    public String editarUsuarioForm(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.obtenerUsuarioPorId(id));
        model.addAttribute("titulo", "Editar Usuario");
        model.addAttribute("nombre_boton", "Actualizar Usuario");
        return "crear_usuario";
    }

    @GetMapping("/usuario/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/admin/gestionUsuarios";
    }

    @GetMapping("/gestionUsuarios")
    public String gestionUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "gestionar_usuarios";
    }
}

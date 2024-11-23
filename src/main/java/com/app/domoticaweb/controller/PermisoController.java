package com.app.domoticaweb.controller;


import com.app.domoticaweb.model.Permiso;
import com.app.domoticaweb.model.TipoPermiso;
import com.app.domoticaweb.service.PermisoService;
import com.app.domoticaweb.service.UsuarioService;
import com.app.domoticaweb.service.VariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;
    private final UsuarioService usuarioService;
    private final VariableService variableService;

    @GetMapping("/gestionPermisos")
    public String listarPermisos(Model model) {
        model.addAttribute("permisos", permisoService.obtenerTodosPermisos());
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("variables", variableService.obtenerTodasLasVariables());
        return "gestionar_permisos";
    }

    @GetMapping("/permisos")
    @ResponseBody
    public List<Permiso> obtenerPermisos() {
        return permisoService.obtenerTodosPermisos();
    }


    @PostMapping("/asignar")
    public ResponseEntity<String> asignarPermiso(@RequestParam Long usuarioId, @RequestParam Long variableId, @RequestParam TipoPermiso tipoPermiso) {
        if (permisoService.existePermiso(usuarioId, variableId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya tiene asignado este permiso para la variable seleccionada.");
        }
        Permiso nuevoPermiso = Permiso.builder()
                .usuario(usuarioService.obtenerUsuarioPorId(usuarioId))
                .variable(variableService.obtenerVariablePorId(variableId).orElseThrow(() -> new RuntimeException("Variable no encontrada")))
                .tipoPermiso(tipoPermiso)
                .permiso(true)
                .build();

        permisoService.crearPermiso(nuevoPermiso);
        return ResponseEntity.ok("Permiso asignado correctamente");
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarPermiso(@PathVariable Long id) {
        permisoService.eliminarPermiso(id);
        return ResponseEntity.ok("Permiso eliminado correctamente");
    }


}

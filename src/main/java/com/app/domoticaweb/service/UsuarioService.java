package com.app.domoticaweb.service;


import com.app.domoticaweb.model.Rol;
import com.app.domoticaweb.model.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario crearUsuario(Usuario usuario);
    Usuario actualizarUsuario(Usuario usuario);
    void eliminarUsuario(Long id);
    Usuario obtenerUsuarioPorId(Long id);
    List<Usuario> listarUsuarios();
    Usuario buscarPorCorreo(String correo);
    Usuario cambiarRol(Long id, Rol nuevoRol);
    Usuario autenticar(String correo, String password);
    String enviarCorreoRecuperacion(String correo);
}

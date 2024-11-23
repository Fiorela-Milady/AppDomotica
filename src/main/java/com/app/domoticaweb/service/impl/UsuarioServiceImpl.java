package com.app.domoticaweb.service.impl;

import com.app.domoticaweb.model.Rol;
import com.app.domoticaweb.model.Status;
import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.repository.UsuarioRepository;
import com.app.domoticaweb.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado.");
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizarUsuario(Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new RuntimeException("Usuario no encontrado.");
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el correo: " + correo));
    }

    @Override
    @Transactional
    public Usuario cambiarRol(Long id, Rol nuevoRol) {
        Usuario usuario = obtenerUsuarioPorId(id);
        usuario.setRol(nuevoRol);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario autenticar(String correo, String password) {
        return usuarioRepository.findByCorreo(correo)
                .filter(usuario -> usuario.getPassword().equals(password))
                .orElse(null);
    }


    @Override
    @Transactional
    public String enviarCorreoRecuperacion(String correo) {

        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            return "Usuario no encontrado con el correo: " + correo;
        }

        if (usuario.getStatus() != Status.ACTIVO) {
            return "La cuenta del usuario con correo " + correo + " no está activa.";
        }

        String contrasena = usuario.getPassword();
        try {
            enviarCorreo(correo, contrasena);
            System.out.println("Correo de recuperación enviado a: " + correo);
            return "Correo de recuperación enviado exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al enviar el correo de recuperación.";
        }
    }


    private void enviarCorreo(String correo, String contrasena) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(correo);
        helper.setSubject("Recuperación de Contraseña");

        String contenidoHtml = "<!DOCTYPE html>" +
                "<html lang='en'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }" +
                "        .header { background-color: #007bff; padding: 20px; color: #ffffff; border-radius: 8px 8px 0 0; text-align: center; }" +
                "        .header h1 { margin: 0; font-size: 24px; }" +
                "        .content { padding: 20px; font-size: 16px; color: #333333; }" +
                "        .content p { margin: 10px 0; }" +
                "        .button { display: inline-block; padding: 10px 20px; margin-top: 20px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 5px; }" +
                "        .footer { text-align: center; font-size: 12px; color: #777777; margin-top: 20px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>Recuperación de Contraseña</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Hola,</p>" +
                "            <p>Recibimos una solicitud para recuperar tu contraseña. A continuación, te proporcionamos tu contraseña actual:</p>" +
                "            <p style='font-weight: bold; color: #007bff;'>Contraseña: " + contrasena + "</p>" +
                "            <p>Por razones de seguridad, te recomendamos cambiar tu contraseña una vez que ingreses al sistema.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>Si no solicitaste este correo, por favor ignóralo.</p>" +
                "            <p>&copy; 2024 Tu Empresa. Todos los derechos reservados.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        helper.setText(contenidoHtml, true); // El segundo parámetro true indica que es HTML

        mailSender.send(mensaje);
    }


}

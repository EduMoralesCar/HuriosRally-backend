package com.hurios.huriosbackend.service;

import com.hurios.huriosbackend.config.JwtUtil;
import com.hurios.huriosbackend.entity.EmailVerificationCode;
import com.hurios.huriosbackend.entity.PasswordReset;
import com.hurios.huriosbackend.entity.User;
import com.hurios.huriosbackend.repository.EmailVerificationCodeRepository;
import com.hurios.huriosbackend.repository.PasswordResetRepository;
import com.hurios.huriosbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/*
 * AuthService: encapsula la logica de registro, login, verificacion por email
 * y reset de contrasena. Genera un JWT tras login exitoso.
 */
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final EmailVerificationCodeRepository codeRepo;
    private final PasswordResetRepository resetRepo;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepo,
                       EmailVerificationCodeRepository codeRepo,
                       PasswordResetRepository resetRepo,
                       EmailService emailService,
                       JwtUtil jwtUtil,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.codeRepo = codeRepo;
        this.resetRepo = resetRepo;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String register(String email, String password, String fullName, String phone) throws Exception {
        Optional<User> existing = userRepo.findByEmail(email);
        if (existing.isPresent()) {
            return "Correo ya registrado.Regístrese con otro correo.";
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole("CLIENTE");
        user.setVerified(false);
        userRepo.save(user);

        String code = String.valueOf((int) (100000 + Math.random() * 900000));
        EmailVerificationCode ev = new EmailVerificationCode();
        ev.setUser(user);
        ev.setCode(code);
        ev.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        codeRepo.save(ev);

        String html = buildVerificationEmail(fullName, code);
        emailService.sendHtml(email, "Codigo de verificación - Hurios Rally", html, true);

        return "Usuario creado. Revisa tu email para verificar.";
    }

    public Map<String, Object> login(String email, String password, String expectedRole) {
        Optional<User> op = userRepo.findByEmail(email);
        if (op.isEmpty()) {
            return Map.of("ok", false, "message", "Usuario no encontrado.Recargue la página par volver a intentar el login");
        }

        User user = op.get();
        String actualRole = normalizeRole(user.getRole());

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            return Map.of("ok", false, "message", "La cuenta no tiene una contraseña configurada.Recargue la página par volver a intentar el login");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Map.of("ok", false, "message", "Contraseña incorrecta. Recargue la página par volver a intentar el login");
        }

        if (expectedRole != null && !expectedRole.isBlank() && !expectedRole.equals(actualRole)) {
            return Map.of("ok", false, "message", "Su correo es de perfil " + actualRole);
        }

        if (!user.isVerified()) {
            return Map.of("ok", false, "message", "El usuario no está verificado.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("message", "Login exitoso");
        response.put("token", token);
        response.put("role", actualRole);
        return response;
    }

    public String sendVerificationCode(String email) throws Exception {
        Optional<User> op = userRepo.findByEmail(email);
        if (op.isEmpty()) {
            return "Usuario no encontrado";
        }

        User user = op.get();
        String code = String.valueOf((int) (100000 + Math.random() * 900000));
        EmailVerificationCode ev = new EmailVerificationCode();
        ev.setUser(user);
        ev.setCode(code);
        ev.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        codeRepo.save(ev);

        String html = buildVerificationEmail(user.getFullName(), code);
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendHtml(email, "Código de verificación - Hurios Rally", html, true);
            } catch (Exception e) {
                System.err.println("Error sending verification email: " + e.getMessage());
                System.out.println("Verification code for " + email + ": " + code);
            }
        });
        return "Código enviado";
    }

    @Transactional
    public String verifyCode(String email, String code) {
        Optional<User> op = userRepo.findByEmail(email);
        if (op.isEmpty()) {
            return "Usuario no encontrado";
        }

        User user = op.get();
        Optional<EmailVerificationCode> rec =
                codeRepo.findTopByUserAndCodeAndExpiresAtAfterOrderByCreatedAtDesc(
                        user,
                        code,
                        LocalDateTime.now()
                );

        if (rec.isEmpty()) {
            return "Código inválido o expirado";
        }

        user.setVerified(true);
        userRepo.save(user);
        codeRepo.deleteByUser(user);
        return "Email verificado";
    }

    @Transactional
    public Map<String, Object> verifyCodeAndLogin(String email, String code) {
        String result = verifyCode(email, code);
        if (!"Email verificado".equals(result)) {
            return Map.of("ok", false, "message", result);
        }

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Map.of("ok", false, "message", "Usuario no encontrado");
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("message", "Email verificado");
        response.put("token", token);
        response.put("role", normalizeRole(user.getRole()));
        return response;
    }

    public Map<String, Object> requestPasswordReset(String email) throws Exception {
        Optional<User> op = userRepo.findByEmail(email);
        if (op.isEmpty()) {
            return Map.of("message", "Si el email existe, se le envió un correo a esa dirección");
        }

        User user = op.get();
        String token = UUID.randomUUID().toString();
        PasswordReset pr = new PasswordReset();
        pr.setUser(user);
        pr.setToken(token);
        pr.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetRepo.save(pr);

        String link = frontendUrl() + "/new-password?token=" + token + "&email="
                + java.net.URLEncoder.encode(email, StandardCharsets.UTF_8);
        String html = "<p>Haz clic para restablecer tu contraseña: <a href=\"" + link
                + "\">Restablecer contraseña</a></p>";

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendHtml(email, "Reiniciar contraseña", html);
            } catch (Exception e) {
                System.err.println("Error sending email: " + e.getMessage());
                System.out.println("Reset link (for development): " + link);
            }
        });

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Si el email existe, se le envió un correo a esa dirección");
        res.put("resetLink", link);
        res.put("token", token);
        return res;
    }


    @Transactional
    public String resetPassword(String email, String token, String newPassword) {
        Optional<User> op = userRepo.findByEmail(email);
        if (op.isEmpty()) {
            return "Usuario no encontrado.";
        }

        User user = op.get();
        Optional<PasswordReset> rec =
                resetRepo.findTopByUserAndTokenAndExpiresAtAfterAndUsedFalseOrderByCreatedAtDesc(
                        user,
                        token,
                        LocalDateTime.now()
                );

        if (rec.isEmpty()) {
            return "Token inválido o expirado.";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        PasswordReset pr = rec.get();
        pr.setUsed(true);
        resetRepo.save(pr);

        return "Contraseña actualizada.";
    }

    private String frontendUrl() {
        if (this.frontendUrl == null || this.frontendUrl.isBlank()) {
            return "http://localhost:5173";
        }
        return this.frontendUrl;
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "CLIENTE";
        }
        return role;
    }

    private String buildVerificationEmail(String userName, String code) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "body { margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #1a1a1a; }" +
            ".container { max-width: 600px; margin: 0 auto; background-color: #1a1a1a; padding: 40px 20px; }" +
            ".content { color: #ffffff; text-align: center; }" +
            "h1 { color: #4a9eff; font-size: 32px; margin-bottom: 20px; }" +
            "p { color: #cccccc; font-size: 16px; line-height: 1.6; margin: 15px 0; }" +
            ".code-box { background-color: transparent; border: 3px dashed #ff4444; border-radius: 10px; padding: 30px; margin: 30px 0; display: inline-block; }" +
            ".code { color: #ff4444; font-size: 48px; font-weight: bold; letter-spacing: 8px; font-family: monospace; }" +
            ".warning { color: #cccccc; font-size: 14px; margin-top: 20px; }" +
            ".logo { margin: 30px 0; }" +
            ".logo img { width: 200px; height: auto; }" +
            ".footer { color: #888888; font-size: 14px; margin-top: 40px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class='container'>" +
            "<div class='content'>" +
            "<h1>Bienvenido, " + userName + "!</h1>" +
            "<p>Gracias por registrarte en nuestra aplicación. Para activar tu cuenta, utiliza el siguiente código OTP:</p>" +
            "<div class='code-box'>" +
            "<div class='code'>" + code + "</div>" +
            "</div>" +
            "<p class='warning'>Este código expira en 15 minutos. Por favor no lo compartas con nadie.</p>" +
            "<div class='logo'>" +
            "<img src='cid:logo' alt='Hurios Rally' />" +
            "</div>" +
            "<div class='footer'>" +
            "<p>© 2025 Hurios Rally. Todos los derechos reservados.</p>" +
            "</div>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}

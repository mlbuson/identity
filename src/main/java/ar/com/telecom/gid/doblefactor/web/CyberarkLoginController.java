
package ar.com.telecom.gid.doblefactor.web;

import ar.com.telecom.gid.doblefactor.configuration.SessionCache;
import jakarta.servlet.http.HttpServletResponse;
import ar.com.telecom.gid.doblefactor.service.ConexionService;
import ar.com.telecom.gid.doblefactor.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class CyberarkLoginController {

    Logger log = LoggerFactory.getLogger(CyberarkLoginController.class);

    @Value("${redirectFM}")
    String urlFM;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private ConexionService conexion;

    @CrossOrigin
    @GetMapping("/authorizeOauth")
    public ResponseEntity<Void> getAuthorization(){
        String cyberArkUrl = conexion.getUrlConexion();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(cyberArkUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/oauth2/code/idp")
    public void handleCallback(HttpServletResponse response, @RequestParam Map<String, String> queryParams) throws IOException {
        String token = "";
        Map<String,Object> claims = new HashMap<>();
        Usuario usuario = new Usuario();
        String authorizationCode = queryParams.get("code");
        String error = queryParams.get("error");
        if(error == null) {
            if (authorizationCode != null) {
                token = conexion.getToken(authorizationCode);
                if (token != null && !token.isEmpty()) {
                    claims = conexion.getClaims(token);
                }
                if (!claims.isEmpty()) {
                    usuario = conexion.getUsuario(claims);
                }
                log.info("claims: " + claims);
                String jwt = jwtUtil.createToken(token, usuario);
                String sessionId = UUID.randomUUID().toString();
                SessionCache.storeSession(sessionId, jwt);
                log.info("Redirigiendo a FieldManager");
                response.sendRedirect(urlFM + "?sessionId=" + sessionId);
            } else {
                response.sendRedirect("/error/noCode.html");
            }
        } else {
            if(error.equals("access_denied")){
                response.sendRedirect("/error/unauthorized.html");
            } else {
                log.info("error: {}", error);
            }
        }
    }

    @GetMapping("/jwt")
    public ResponseEntity<String> getJwt(@RequestParam("sessionId") String sessionId) {
        String jwtToken = null;
        try {
            jwtToken = SessionCache.getSession(sessionId);
        } catch (Exception e) {
            log.error("Error al obtener el token del cache: " + e.getMessage());
        }
        if (jwtToken != null) {
            log.info("Se obtuvo el token del cache.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sesión inválida o expirada");
        }
        SessionCache.removeSession(sessionId);
        return ResponseEntity.ok(jwtToken);
    }

    @GetMapping("/logoutCyberArk")
    public ResponseEntity<Void> logout(@RequestHeader("X-Custom-JWT") String token) {
        log.info("Invalidar sesión de CyberArk");
        if (!token.isEmpty()) {
            log.info("Tamaño del token recibido: {}", token.length());
        }
        conexion.cerrarSesionCyberArk(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}

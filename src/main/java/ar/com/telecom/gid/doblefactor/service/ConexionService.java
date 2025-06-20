package ar.com.telecom.gid.doblefactor.service;

import ar.com.telecom.gid.doblefactor.web.Usuario;
import java.io.IOException;
import java.util.Map;

public interface ConexionService {
    String getUrlConexion();
    String getToken(String authorizationCode) throws IOException;
    Map<String,Object> getClaims(String token);
    Usuario getUsuario(Map<String, Object> claims) throws IOException;
    void cerrarSesionCyberArk(String token);
}

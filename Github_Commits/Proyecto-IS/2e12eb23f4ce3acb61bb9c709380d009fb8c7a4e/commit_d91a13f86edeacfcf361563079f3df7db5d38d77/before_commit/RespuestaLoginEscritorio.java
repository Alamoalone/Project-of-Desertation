/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.pojo;

/**
 *
 * @author mateo
 */
public class RespuestaLoginEscritorio {
    
    private boolean error;
    private String contenido;
    private Usuario usuarioSession;

    public RespuestaLoginEscritorio() {
    }

    public RespuestaLoginEscritorio(boolean error, String contenido, Usuario usuarioSession) {
        this.error = error;
        this.contenido = contenido;
        this.usuarioSession = usuarioSession;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Usuario getUsuarioSession() {
        return usuarioSession;
    }

    public void setUsuarioSession(Usuario usuarioSession) {
        this.usuarioSession = usuarioSession;
    }
    
    
    
}

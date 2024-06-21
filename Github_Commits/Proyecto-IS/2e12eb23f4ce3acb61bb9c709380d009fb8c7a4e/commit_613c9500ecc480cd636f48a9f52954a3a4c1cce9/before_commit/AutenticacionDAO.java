/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.util.HashMap;
import modelo.pojo.RespuestaLoginEscritorio;
import modelo.pojo.Usuario;
import mybatis.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

/**
 *
 * @author mateo
 */
public class AutenticacionDAO {
    
    public static RespuestaLoginEscritorio verificarSesionEscritorio(String username, String password) {
        RespuestaLoginEscritorio respuesta = new RespuestaLoginEscritorio();
        respuesta.setError(true);
        
        SqlSession dbSqlSession = MyBatisUtil.getSession();
        
        if(dbSqlSession != null){
             
            try {
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("username", username);
                parametros.put("password", password);
                
                Usuario usuario = dbSqlSession.selectOne("autenticacion.loginEscritorio", parametros);
                
                if(usuario != null){
                    respuesta.setError(false);
                    respuesta.setContenido("Bienvenido al sistema "+ usuario.getNombre());
                    respuesta.setUsuarioSession(usuario);
                }else{
                    respuesta.setContenido("Email y/o contraseña incorrectos");
                }
                
                
            } catch (Exception e) {
                respuesta.setContenido("Error: "+ e.getMessage());
            }finally{
                dbSqlSession.close();
            }
        }else{
            respuesta.setContenido("Por el momento no hay conexión");
        }
        
        
        return  respuesta;
    }
    
}

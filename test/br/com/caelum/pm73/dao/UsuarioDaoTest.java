package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {
	
	private Session session;
    private UsuarioDao usuarioDao;
    
    @Before
    public void antes() {
        // criamos a sessao e a passamos para o dao
        session = new CriadorDeSessao().getSession();
        usuarioDao = new UsuarioDao(session);
        
        // inicia transacao
        session.beginTransaction();
    }

    @After
    public void depois() {
    	// faz o rollback
        session.getTransaction().rollback();
        // fechamos a sessao
        session.close();
    }
	
	@Test
    public void deveEncontrarPeloNomeEEmail() {
       
        // criando um usuario e salvando antes
        // de invocar o porNomeEEmail
        Usuario novoUsuario = new Usuario("Jo�o da Silva", "joao@dasilva.com.br");
        usuarioDao.salvar(novoUsuario);

        // agora buscamos no banco
        Usuario usuarioDoBanco = usuarioDao
                .porNomeEEmail("Jo�o da Silva", "joao@dasilva.com.br");

        assertEquals("Jo�o da Silva", usuarioDoBanco.getNome());
        assertEquals("joao@dasilva.com.br", usuarioDoBanco.getEmail());
    }
	
	@Test
    public void deveRetornarNuloSeNaoEncontrarUsuario() {
        Usuario usuarioDoBanco = usuarioDao
                .porNomeEEmail("Jo�o Joaquim", "joao@joaquim.com.br");

        assertNull(usuarioDoBanco);
    }
	
	@Test
    public void deveDeletarUmUsuario() {
        Usuario usuario = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        usuarioDao.salvar(usuario);
        usuarioDao.deletar(usuario);

        session.flush();

        Usuario usuarioNoBanco = usuarioDao.porNomeEEmail("Mauricio Aniche", "mauricio@aniche.com.br");

        assertNull(usuarioNoBanco);

    }

}

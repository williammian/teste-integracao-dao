package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTest {
    private Session session;
    private LeilaoDao leilaoDao;
    private UsuarioDao usuarioDao;

    @Before
    public void antes() {
        session = new CriadorDeSessao().getSession();
        leilaoDao = new LeilaoDao(session);
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
    public void deveContarLeiloesNaoEncerrados() {
        // criamos um usuario
        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        // criamos os dois leiloes
        Leilao ativo = new Leilao("Geladeira", 1500.0, mauricio, false);
        Leilao encerrado = new Leilao("XBox", 700.0, mauricio, false);
        encerrado.encerra();

        // persistimos todos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        // invocamos a acao que queremos testar
        // pedimos o total para o DAO
        long total = leilaoDao.total();

        assertEquals(1L, total);
    }
    
    @Test
    public void deveRetornarZeroSeNaoHaLeiloesNovos() {
	    Usuario mauricio =  new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");
	
	    Leilao encerrado = new Leilao("XBox", 700.0, mauricio, false);
	    Leilao tambemEncerrado = new Leilao("Geladeira", 1500.0, mauricio, false);
	    encerrado.encerra();
	    tambemEncerrado.encerra();
	
	    usuarioDao.salvar(mauricio);
	    leilaoDao.salvar(encerrado);
	    leilaoDao.salvar(tambemEncerrado);
	
	    long total = leilaoDao.total();
	
	    assertEquals(0L, total);
	}
    
    @Test
    public void deveRetornarLeiloesDeProdutosNovos() {
        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        Leilao produtoNovo = new Leilao("XBox", 700.0, mauricio, false);
        Leilao produtoUsado = new Leilao("Geladeira", 1500.0, mauricio,true);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(produtoNovo);
        leilaoDao.salvar(produtoUsado);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertEquals("XBox", novos.get(0).getNome());
    }
    
    @Test
    public void deveTrazerSomenteLeiloesAntigos() {
        Usuario mauricio = new Usuario("Mauricio Aniche",
                "mauricio@aniche.com.br");

        Leilao recente = new Leilao("XBox", 700.0, mauricio, false);
        Leilao antigo = new Leilao("Geladeira", 1500.0, mauricio,true);

        Calendar dataRecente = Calendar.getInstance();
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);

        recente.setDataAbertura(dataRecente);
        antigo.setDataAbertura(dataAntiga);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(recente);
        leilaoDao.salvar(antigo);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
        assertEquals("Geladeira", antigos.get(0).getNome());
    }
    
    @Test
    public void deveTrazerSomenteLeiloesAntigosHaMaisDe7Dias() {
        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        Leilao noLimite = new Leilao("XBox", 700.0, mauricio, false);

        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -7);

        noLimite.setDataAbertura(dataAntiga);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(noLimite);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
    }
}

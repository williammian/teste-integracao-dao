package br.com.caelum.pm73.dao;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dao.builder.LeilaoBuilder;
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

        Leilao ativo = new LeilaoBuilder()
            .comDono(mauricio)
            .constroi();
        Leilao encerrado = new LeilaoBuilder()
            .comDono(mauricio)
            .encerrado()
            .constroi();

        // persistimos todos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        // pedimos o total para o DAO
        long total = leilaoDao.total();

        assertEquals(1L, total);
    }
    
    @Test
    public void deveRetornarZeroSeNaoHaLeiloesNovos() {
    	Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        Leilao produtoNovo = 
                new LeilaoBuilder()
                .comDono(mauricio)
                .comNome("XBox")
                .constroi(); 
        Leilao produtoUsado = 
                new LeilaoBuilder()
                .comDono(mauricio)
                .comNome("XBox")
                .usado()
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(produtoNovo);
        leilaoDao.salvar(produtoUsado);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertEquals("XBox", novos.get(0).getNome());
	}
    
    @Test
    public void deveRetornarLeiloesDeProdutosNovos() {
        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        Leilao produtoNovo = new LeilaoBuilder()
                .comNome("XBox")
                .comDono(mauricio)
                .comValor(700.0)
                .constroi();
        
        Leilao produtoUsado = new LeilaoBuilder()
                .comNome("Geladeira")
                .comDono(mauricio)
                .comValor(1500.0)
                .usado()
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(produtoNovo);
        leilaoDao.salvar(produtoUsado);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertEquals("XBox", novos.get(0).getNome());
    }
    
    @Test
    public void deveTrazerSomenteLeiloesAntigos() {
    	Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        Leilao recente = new LeilaoBuilder()
                .comNome("XBox")
                .comDono(mauricio)
                .constroi();
        Leilao antigo = new LeilaoBuilder()
                .comDono(mauricio)
                .comNome("Geladeira")
                .diasAtras(10)
                .constroi();

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

        Leilao noLimite = new LeilaoBuilder()
        		.comDono(mauricio)
        		.comNome("XBox")
        		.comValor(700.0)
        		.constroi();

        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -7);

        noLimite.setDataAbertura(dataAntiga);

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(noLimite);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
    }
    
    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {

    	// criando as datas
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        // criando os leiloes, cada um com uma data
        Leilao leilao1 = new LeilaoBuilder()
                .diasAtras(2)
                .comDono(mauricio)
                .comNome("XBox")
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .diasAtras(20)
                .comDono(mauricio)
                .comNome("XBox")
                .constroi();

        // persistindo os objetos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        // invocando o metodo para testar
        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // garantindo que a query funcionou
        assertEquals(1, leiloes.size());
        assertEquals("XBox", leiloes.get(0).getNome());
    }
    
    @Test
    public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {

    	// criando as datas
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();
        Calendar dataDoLeilao1 = Calendar.getInstance();
        dataDoLeilao1.add(Calendar.DAY_OF_MONTH, -2);

        Usuario mauricio = new Usuario("Mauricio Aniche", "mauricio@aniche.com.br");

        // criando os leiloes, cada um com uma data
        Leilao leilao1 = new LeilaoBuilder()
                .comDono(mauricio)
                .diasAtras(2)
                .comNome("XBox")
                .encerrado()
                .constroi();

        // persistindo os objetos no banco
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);

        // invocando o metodo para testar
        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // garantindo que a query funcionou
        assertEquals(0, leiloes.size());
    }
    
    @Test
    public void deveRetornarLeiloesDisputados() {
        Usuario mauricio = new Usuario("Mauricio", "mauricio@aniche.com.br");
        Usuario marcelo = new Usuario("Marcelo", "marcelo@aniche.com.br");

        Leilao leilao1 = new LeilaoBuilder()
                .comDono(marcelo)
                .comValor(3000.0)
                .comLance(Calendar.getInstance(), mauricio, 3000.0)
                .comLance(Calendar.getInstance(), marcelo, 3100.0)
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .comDono(mauricio)
                .comValor(3200.0)
                .comLance(Calendar.getInstance(), mauricio, 3000.0)
                .comLance(Calendar.getInstance(), marcelo, 3100.0)
                .comLance(Calendar.getInstance(), mauricio, 3200.0)
                .comLance(Calendar.getInstance(), marcelo, 3300.0)
                .comLance(Calendar.getInstance(), mauricio, 3400.0)
                .comLance(Calendar.getInstance(), marcelo, 3500.0)
                .constroi();

        usuarioDao.salvar(marcelo);
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

        assertEquals(1, leiloes.size());
        assertEquals(3200.0, leiloes.get(0).getValorInicial(), 0.00001);
    }
    
    @Test
    public void listaSomenteOsLeiloesDoUsuario() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Usuario comprador2 = new Usuario("Guilherme", "g@g.com");
        
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .comValor(50.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .comLance(Calendar.getInstance(), comprador2, 200.0)
            .constroi();
        
        Leilao leilao2 = new LeilaoBuilder()
            .comDono(dono)
            .comValor(250.0)
            .comLance(Calendar.getInstance(), comprador2, 100.0)
            .constroi();
        
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        usuarioDao.salvar(comprador2);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }
    
    @Test
    public void devolveAMediaDoValorInicialDosLeiloesQueOUsuarioParticipou(){
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .comValor(50.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .comLance(Calendar.getInstance(), comprador, 200.0)
            .constroi();
        
        Leilao leilao2 = new LeilaoBuilder()
            .comDono(dono)
            .comValor(250.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .constroi();
        
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        assertEquals(150.0, leilaoDao.getValorInicialMedioDoUsuario(comprador), 0.001);
    }
}

package com.oeirastechtour;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Autor: Equipe Oeiras Tech Tuor.
 * Activity responsável pelo menu inicial do aplicativo Oeiras Tech Tour.
 */
public class MainActivity extends Activity {

    // Constante responsável por guardar o nome do áudio de apresentação em res/raw.
    private static final String NOME_AUDIO_MENU = "audio_menu";

    // Variável responsável por controlar a narração da tela inicial.
    private NarradorAcessivel narradorAcessivel;

    // Variável responsável por identificar toque simples, toque duplo e rolagem na tela inicial.
    private GestureDetector detectorGestosTela;

    // Variável responsável por indicar que o toque começou em um botão e deve ignorar gestos da tela.
    private boolean toqueIniciadoEmControle;

    // Variável responsável por guardar o botão do modo acessibilidade da tela inicial.
    private Button botaoModoAcessibilidade;

    // Variável responsável por guardar o estado atual do modo acessibilidade.
    private boolean modoAcessibilidadeAtivado;

    // Variável responsável por evitar abertura duplicada da tela da praça.
    private boolean aberturaPracaSolicitada;

    // Variável responsável por evitar reinício duplicado do áudio na primeira abertura do menu.
    private boolean primeiraRetomadaMenu = true;

    /**
     * Método obrigatório responsável por criar a tela inicial e iniciar o áudio de apresentação.
     */
    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        ativarModoAcessibilidadeAoAbrirAplicativo();
        setContentView(R.layout.tela_menu_principal);

        configurarTelaInicial();
        iniciarAudioApresentacao();
    }

    /**
     * Método obrigatório responsável por liberar a trava de navegação quando o usuário retorna ao menu.
     */
    @Override
    protected void onResume() {
        super.onResume();
        aberturaPracaSolicitada = false;
        carregarModoAcessibilidade();
        atualizarBotaoModoAcessibilidade();
        retomarAudioApresentacaoAoVoltarParaMenu();
    }

    /**
     * Método obrigatório responsável por pausar o áudio quando o menu deixa de ficar em primeiro plano.
     */
    @Override
    protected void onPause() {
        if (narradorAcessivel != null) {
            narradorAcessivel.pausarAudio();
        }

        super.onPause();
    }

    /**
     * Método obrigatório responsável por liberar os recursos de áudio quando a tela for destruída.
     */
    @Override
    protected void onDestroy() {
        if (narradorAcessivel != null) {
            narradorAcessivel.liberarRecursos();
        }

        super.onDestroy();
    }

    /**
     * Método obrigatório responsável por enviar toques da tela para o detector de gestos.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent eventoToque) {
        if (eventoToque.getActionMasked() == MotionEvent.ACTION_DOWN) {
            toqueIniciadoEmControle = toqueIniciadoEmControle(eventoToque);
        }

        if (!toqueIniciadoEmControle && detectorGestosTela != null) {
            detectorGestosTela.onTouchEvent(eventoToque);
        }

        return super.dispatchTouchEvent(eventoToque);
    }

    /**
     * Método responsável por configurar botões, gestos e controles da tela inicial.
     */
    private void configurarTelaInicial() {
        Button botaoConhecerPraca = findViewById(R.id.botao_conhecer_praca_vitorias);
        botaoModoAcessibilidade = findViewById(R.id.botao_modo_acessibilidade_menu);

        carregarModoAcessibilidade();
        atualizarBotaoModoAcessibilidade();
        configurarGestosTelaInicial();
        botaoConhecerPraca.setOnClickListener(view -> abrirTelaPracaVitorias());
        botaoModoAcessibilidade.setOnClickListener(view -> alternarModoAcessibilidade());
    }

    /**
     * Método responsável por preparar e reproduzir o áudio de apresentação do menu quando o modo acessibilidade estiver ativo.
     */
    private void iniciarAudioApresentacao() {
        String textoApresentacao = montarTextoApresentacao();

        narradorAcessivel = new NarradorAcessivel(
                this,
                NOME_AUDIO_MENU,
                textoApresentacao,
                null
        );

        if (modoAcessibilidadeAtivado) {
            narradorAcessivel.iniciarAudio();
        }
    }

    /**
     * Método responsável por retomar o áudio de apresentação quando o usuário volta para o menu.
     */
    private void retomarAudioApresentacaoAoVoltarParaMenu() {
        if (primeiraRetomadaMenu) {
            primeiraRetomadaMenu = false;
            return;
        }

        if (modoAcessibilidadeAtivado && narradorAcessivel != null) {
            narradorAcessivel.reiniciarAudio();
        }
    }

    /**
     * Método responsável por alternar o modo acessibilidade e controlar o início automático do áudio.
     */
    private void alternarModoAcessibilidade() {
        modoAcessibilidadeAtivado = !modoAcessibilidadeAtivado;
        PreferenciasAcessibilidade.salvarModoAcessibilidade(this, modoAcessibilidadeAtivado);
        atualizarBotaoModoAcessibilidade();

        if (modoAcessibilidadeAtivado) {
            if (narradorAcessivel != null) {
                narradorAcessivel.iniciarAudio();
            }
            Toast.makeText(this, R.string.aviso_modo_acessibilidade_ativado, Toast.LENGTH_SHORT).show();
            return;
        }

        if (narradorAcessivel != null) {
            narradorAcessivel.pausarAudio();
        }
        Toast.makeText(this, R.string.aviso_modo_acessibilidade_desativado, Toast.LENGTH_SHORT).show();
    }

    /**
     * Método responsável por abrir a tela da Praça das Vitórias.
     */
    private void abrirTelaPracaVitorias() {
        if (aberturaPracaSolicitada) {
            return;
        }

        aberturaPracaSolicitada = true;

        if (narradorAcessivel != null) {
            narradorAcessivel.pausarAudio();
        }

        Intent intencaoTelaPraca = new Intent(this, TelaPracaVitorias.class);
        startActivity(intencaoTelaPraca);
    }

    /**
     * Método responsável por abrir a Praça das Vitórias com toque simples ou duplo sem confundir rolagem com toque.
     */
    private void configurarGestosTelaInicial() {
        detectorGestosTela = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            /**
             * Método responsável por abrir a tela da Praça das Vitórias após um toque simples confirmado.
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent eventoToque) {
                abrirTelaPracaVitorias();
                return true;
            }

            /**
             * Método responsável por abrir a tela da Praça das Vitórias após um toque duplo no menu.
             */
            @Override
            public boolean onDoubleTap(MotionEvent eventoToque) {
                abrirTelaPracaVitorias();
                return true;
            }

            /**
             * Método responsável por reconhecer rolagem sem transformar o deslize em toque simples.
             */
            @Override
            public boolean onScroll(MotionEvent eventoInicial, MotionEvent eventoAtual, float distanciaX, float distanciaY) {
                return true;
            }
        });
    }

    /**
     * Método responsável por carregar o estado salvo do modo acessibilidade.
     */
    private void carregarModoAcessibilidade() {
        modoAcessibilidadeAtivado = PreferenciasAcessibilidade.modoAcessibilidadeAtivado(this);
    }

    /**
     * Método responsável por garantir que o modo acessibilidade esteja ativo sempre que o aplicativo abrir pelo menu.
     */
    private void ativarModoAcessibilidadeAoAbrirAplicativo() {
        modoAcessibilidadeAtivado = true;
        PreferenciasAcessibilidade.salvarModoAcessibilidade(this, true);
    }

    /**
     * Método responsável por atualizar o texto do botão do modo acessibilidade.
     */
    private void atualizarBotaoModoAcessibilidade() {
        if (botaoModoAcessibilidade == null) {
            return;
        }

        int textoBotao = modoAcessibilidadeAtivado
                ? R.string.botao_desativar_modo_acessibilidade
                : R.string.botao_ativar_modo_acessibilidade;
        botaoModoAcessibilidade.setText(textoBotao);
        botaoModoAcessibilidade.setContentDescription(getString(textoBotao));
    }

    /**
     * Método responsável por verificar se o toque começou em um botão da tela.
     */
    private boolean toqueIniciadoEmControle(MotionEvent eventoToque) {
        return coordenadaDentroDoControle(findViewById(R.id.botao_conhecer_praca_vitorias), eventoToque)
                || coordenadaDentroDoControle(findViewById(R.id.botao_modo_acessibilidade_menu), eventoToque);
    }

    /**
     * Método responsável por verificar se a coordenada do toque está dentro de um controle interativo.
     */
    private boolean coordenadaDentroDoControle(View controle, MotionEvent eventoToque) {
        if (controle == null || controle.getVisibility() != View.VISIBLE) {
            return false;
        }

        int[] posicaoControle = new int[2];
        controle.getLocationInWindow(posicaoControle);

        float toqueX = eventoToque.getX();
        float toqueY = eventoToque.getY();

        return toqueX >= posicaoControle[0]
                && toqueX <= posicaoControle[0] + controle.getWidth()
                && toqueY >= posicaoControle[1]
                && toqueY <= posicaoControle[1] + controle.getHeight();
    }

    /**
     * Método responsável por montar o texto alternativo usado caso o áudio gravado do menu falhe.
     */
    private String montarTextoApresentacao() {
        return getString(R.string.menu_paragrafo_1)
                + " "
                + getString(R.string.menu_paragrafo_2)
                + " "
                + getString(R.string.menu_instrucao_toque);
    }
}

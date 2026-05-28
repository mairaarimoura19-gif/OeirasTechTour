package com.oeirastechtour;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Autor: Equipe Oeiras Tech Tuor.
 * Activity responsável por exibir o roteiro acessível da Praça das Vitórias.
 */
public class TelaPracaVitorias extends Activity {

    // Constante responsável por guardar o nome do áudio da praça em res/raw.
    private static final String NOME_AUDIO_PRACA = "audio_praca_vitorias";

    // Variável responsável por controlar a narração da tela da Praça das Vitórias.
    private NarradorAcessivel narradorAcessivel;

    // Variável responsável por identificar toque simples, toque duplo e rolagem na tela da praça.
    private GestureDetector detectorGestosTela;

    // Variável responsável por indicar que o toque começou em um botão e deve ignorar gestos da tela.
    private boolean toqueIniciadoEmControle;

    // Variável responsável por guardar o botão do modo acessibilidade da tela da praça.
    private Button botaoModoAcessibilidade;

    // Variável responsável por guardar o estado atual do modo acessibilidade.
    private boolean modoAcessibilidadeAtivado;

    /**
     * Método obrigatório responsável por criar a tela da praça e iniciar a narração.
     */
    @Override
    protected void onCreate(Bundle estadoSalvo) {
        super.onCreate(estadoSalvo);
        setContentView(R.layout.tela_praca_vitorias);

        configurarTelaPraca();
        iniciarAudioPraca();
    }

    /**
     * Método obrigatório responsável por atualizar o botão do modo acessibilidade ao retornar para a tela.
     */
    @Override
    protected void onResume() {
        super.onResume();
        carregarModoAcessibilidade();
        atualizarBotaoModoAcessibilidade();
    }

    /**
     * Método obrigatório responsável por pausar o áudio quando a tela deixa de ficar em primeiro plano.
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
     * Método responsável por configurar os botões, gestos e controles da tela da Praça das Vitórias.
     */
    private void configurarTelaPraca() {
        botaoModoAcessibilidade = findViewById(R.id.botao_modo_acessibilidade_praca);
        Button botaoVoltarMenu = findViewById(R.id.botao_voltar_menu);

        carregarModoAcessibilidade();
        atualizarBotaoModoAcessibilidade();
        configurarGestosTelaPraca();
        botaoModoAcessibilidade.setOnClickListener(view -> alternarModoAcessibilidade());
        botaoVoltarMenu.setOnClickListener(view -> voltarMenuPrincipal());
    }

    /**
     * Método responsável por preparar e reproduzir o áudio da Praça das Vitórias quando o modo acessibilidade estiver ativo.
     */
    private void iniciarAudioPraca() {
        String textoPraca = montarTextoNarracaoPraca();

        narradorAcessivel = new NarradorAcessivel(
                this,
                NOME_AUDIO_PRACA,
                textoPraca,
                null
        );

        if (modoAcessibilidadeAtivado) {
            narradorAcessivel.iniciarAudio();
        }
    }

    /**
     * Método responsável por alternar a reprodução do áudio da Praça das Vitórias.
     */
    private void alternarAudioPraca() {
        if (narradorAcessivel != null) {
            narradorAcessivel.alternarAudio();
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
     * Método responsável por voltar para a tela inicial do aplicativo.
     */
    private void voltarMenuPrincipal() {
        if (narradorAcessivel != null) {
            narradorAcessivel.pausarAudio();
        }

        finish();
    }

    /**
     * Método responsável por configurar toque simples, toque duplo e rolagem segura na tela da praça.
     */
    private void configurarGestosTelaPraca() {
        detectorGestosTela = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            /**
             * Método responsável por pausar ou reproduzir o áudio após um toque simples confirmado.
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent eventoToque) {
                alternarAudioPraca();
                return true;
            }

            /**
             * Método responsável por voltar para o menu após um toque duplo.
             */
            @Override
            public boolean onDoubleTap(MotionEvent eventoToque) {
                voltarMenuPrincipal();
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
        return coordenadaDentroDoControle(findViewById(R.id.botao_modo_acessibilidade_praca), eventoToque)
                || coordenadaDentroDoControle(findViewById(R.id.botao_voltar_menu), eventoToque);
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
     * Método responsável por montar o texto alternativo usado caso o áudio gravado da praça falhe.
     */
    private String montarTextoNarracaoPraca() {
        return unirTextos(
                R.string.praca_titulo,
                R.string.praca_subtitulo,
                R.string.praca_paragrafo_1,
                R.string.praca_paragrafo_2,
                R.string.praca_paragrafo_3,
                R.string.praca_paragrafo_4,
                R.string.praca_paragrafo_5,
                R.string.praca_paragrafo_6,
                R.string.praca_paragrafo_7,
                R.string.praca_paragrafo_8,
                R.string.praca_paragrafo_9,
                R.string.praca_paragrafo_10,
                R.string.praca_paragrafo_11,
                R.string.praca_paragrafo_12,
                R.string.praca_paragrafo_13,
                R.string.ponto_museu_arte_sacra,
                R.string.ponto_igreja_matriz,
                R.string.ponto_prefeitura,
                R.string.ponto_solar_doze_janelas,
                R.string.ponto_museu_doca_nunes,
                R.string.ponto_pousada_conego
        );
    }

    /**
     * Método responsável por unir várias strings em um único texto de narração.
     */
    private String unirTextos(int... identificadoresTexto) {
        StringBuilder textoCompleto = new StringBuilder();

        for (int identificadorTexto : identificadoresTexto) {
            textoCompleto.append(getString(identificadorTexto)).append(" ");
        }

        return textoCompleto.toString().trim();
    }
}

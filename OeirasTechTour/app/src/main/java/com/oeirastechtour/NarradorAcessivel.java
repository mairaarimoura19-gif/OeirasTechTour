package com.oeirastechtour;

import android.app.Activity;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;

/**
 * Autor: Equipe Oeiras Tech Tuor.
 * Classe responsável por controlar áudio gravado e leitura acessível por voz.
 */
public class NarradorAcessivel implements TextToSpeech.OnInitListener {

    // Constante responsável por identificar a fala enviada ao TextToSpeech.
    private static final String IDENTIFICADOR_NARRACAO = "narracao_oeiras_tech_tour";

    // Variável responsável por guardar a Activity que usa o narrador.
    private final Activity atividade;

    // Variável responsável por guardar o nome do arquivo de áudio em res/raw.
    private final String nomeArquivoAudio;

    // Variável responsável por guardar o texto usado como alternativa acessível ao áudio gravado.
    private final String textoNarracao;

    // Variável opcional responsável por guardar um botão legado de controle da reprodução do áudio.
    private final Button botaoControleAudio;

    // Variável responsável por reproduzir arquivos de áudio gravados.
    private MediaPlayer reprodutorAudio;

    // Variável responsável por ler o texto quando não houver áudio gravado disponível.
    private TextToSpeech leitorTexto;

    // Variável responsável por indicar se o leitor de texto está pronto para uso.
    private boolean leitorTextoPronto;

    // Variável responsável por impedir reprodução após a liberação dos recursos.
    private boolean recursosLiberados;

    /**
     * Construtor responsável por receber a tela, o áudio, o texto alternativo e o botão opcional de controle.
     */
    public NarradorAcessivel(Activity atividade, String nomeArquivoAudio, String textoNarracao, Button botaoControleAudio) {
        this.atividade = atividade;
        this.nomeArquivoAudio = nomeArquivoAudio;
        this.textoNarracao = textoNarracao;
        this.botaoControleAudio = botaoControleAudio;
    }

    /**
     * Método responsável por iniciar a narração usando áudio gravado ou leitura de texto como alternativa.
     */
    public void iniciarAudio() {
        recursosLiberados = false;

        if (reprodutorAudio != null) {
            reproduzirAudioGravado();
            return;
        }

        if (leitorTexto != null) {
            iniciarLeituraTexto();
            return;
        }

        int identificadorAudio = buscarIdentificadorAudioRaw();
        if (identificadorAudio != 0) {
            iniciarAudioGravado(identificadorAudio);
            return;
        }

        iniciarLeituraTexto();
    }

    /**
     * Método responsável por pausar ou reproduzir novamente o áudio atual.
     */
    public void alternarAudio() {
        if (reprodutorAudio == null && leitorTexto == null) {
            iniciarAudio();
            return;
        }

        if (reprodutorAudio != null) {
            alternarAudioGravado();
            return;
        }

        alternarLeituraTexto();
    }

    /**
     * Método responsável por reiniciar a narração sempre a partir do começo.
     */
    public void reiniciarAudio() {
        recursosLiberados = false;

        if (reprodutorAudio != null) {
            reprodutorAudio.seekTo(0);
            reproduzirAudioGravado();
            return;
        }

        if (leitorTexto != null) {
            leitorTexto.stop();
            falarTextoCompleto();
            return;
        }

        iniciarAudio();
    }

    /**
     * Método responsável por pausar a narração sem liberar os recursos da tela.
     */
    public void pausarAudio() {
        if (reprodutorAudio != null && reprodutorAudio.isPlaying()) {
            reprodutorAudio.pause();
        }

        if (leitorTexto != null && leitorTexto.isSpeaking()) {
            leitorTexto.stop();
        }

        atualizarBotaoControle(false);
    }

    /**
     * Método responsável por parar a narração e liberar recursos de áudio ao fechar a tela.
     */
    public void liberarRecursos() {
        recursosLiberados = true;

        if (reprodutorAudio != null) {
            reprodutorAudio.release();
            reprodutorAudio = null;
        }

        if (leitorTexto != null) {
            leitorTexto.stop();
            leitorTexto.shutdown();
            leitorTexto = null;
            leitorTextoPronto = false;
        }
    }

    /**
     * Método obrigatório responsável por receber o resultado da inicialização do leitor de texto.
     */
    @Override
    public void onInit(int estadoInicializacao) {
        if (estadoInicializacao != TextToSpeech.SUCCESS || leitorTexto == null) {
            informarAudioIndisponivel();
            return;
        }

        Locale idiomaPortuguesBrasil = new Locale("pt", "BR");
        int resultadoIdioma = leitorTexto.setLanguage(idiomaPortuguesBrasil);
        leitorTextoPronto = resultadoIdioma != TextToSpeech.LANG_MISSING_DATA
                && resultadoIdioma != TextToSpeech.LANG_NOT_SUPPORTED;

        if (!leitorTextoPronto) {
            informarAudioIndisponivel();
            return;
        }

        configurarEventoFimLeitura();
        falarTextoCompleto();
    }

    /**
     * Método responsável por localizar o identificador do áudio na pasta res/raw.
     */
    private int buscarIdentificadorAudioRaw() {
        return atividade.getResources().getIdentifier(nomeArquivoAudio, "raw", atividade.getPackageName());
    }

    /**
     * Método responsável por preparar e reproduzir um arquivo de áudio gravado.
     */
    private void iniciarAudioGravado(int identificadorAudio) {
        reprodutorAudio = MediaPlayer.create(atividade, identificadorAudio);

        if (reprodutorAudio == null) {
            iniciarLeituraTexto();
            return;
        }

        // Ao finalizar o áudio gravado, o botão volta a permitir uma nova reprodução.
        reprodutorAudio.setOnCompletionListener(mediaPlayer -> atualizarBotaoControle(false));
        reproduzirAudioGravado();
    }

    /**
     * Método responsável por iniciar o leitor de texto usado quando não houver áudio gravado.
     */
    private void iniciarLeituraTexto() {
        if (recursosLiberados) {
            return;
        }

        if (leitorTexto == null) {
            leitorTexto = new TextToSpeech(atividade.getApplicationContext(), this);
            return;
        }

        if (leitorTextoPronto) {
            falarTextoCompleto();
        }
    }

    /**
     * Método responsável por alternar entre pausar e reproduzir o áudio gravado.
     */
    private void alternarAudioGravado() {
        if (reprodutorAudio.isPlaying()) {
            reprodutorAudio.pause();
            atualizarBotaoControle(false);
            return;
        }

        reproduzirAudioGravado();
    }

    /**
     * Método responsável por alternar entre parar e reiniciar a leitura de texto.
     */
    private void alternarLeituraTexto() {
        if (leitorTexto == null || !leitorTextoPronto) {
            iniciarLeituraTexto();
            return;
        }

        if (leitorTexto.isSpeaking()) {
            leitorTexto.stop();
            atualizarBotaoControle(false);
            return;
        }

        falarTextoCompleto();
    }

    /**
     * Método responsável por reproduzir o áudio gravado desde o ponto atual ou reiniciar quando ele terminou.
     */
    private void reproduzirAudioGravado() {
        if (reprodutorAudio == null) {
            return;
        }

        if (reprodutorAudio.getDuration() > 0
                && reprodutorAudio.getCurrentPosition() >= reprodutorAudio.getDuration()) {
            reprodutorAudio.seekTo(0);
        }

        reprodutorAudio.start();
        atualizarBotaoControle(true);
    }

    /**
     * Método responsável por falar o texto completo quando o leitor de texto estiver disponível.
     */
    private void falarTextoCompleto() {
        if (leitorTexto == null || !leitorTextoPronto || recursosLiberados) {
            return;
        }

        leitorTexto.speak(textoNarracao, TextToSpeech.QUEUE_FLUSH, null, IDENTIFICADOR_NARRACAO);
        atualizarBotaoControle(true);
    }

    /**
     * Método responsável por atualizar o botão quando a leitura de texto terminar.
     */
    private void configurarEventoFimLeitura() {
        leitorTexto.setOnUtteranceProgressListener(new UtteranceProgressListener() {

            /**
             * Método responsável por manter o botão em estado de pausa quando a leitura começar.
             */
            @Override
            public void onStart(String identificadorFala) {
                atividade.runOnUiThread(() -> atualizarBotaoControle(true));
            }

            /**
             * Método responsável por retornar o botão ao estado de reprodução quando a leitura terminar.
             */
            @Override
            public void onDone(String identificadorFala) {
                atividade.runOnUiThread(() -> atualizarBotaoControle(false));
            }

            /**
             * Método responsável por informar falha caso o leitor de texto não consiga reproduzir a fala.
             */
            @Override
            public void onError(String identificadorFala) {
                atividade.runOnUiThread(() -> informarAudioIndisponivel());
            }
        });
    }

    /**
     * Método responsável por atualizar o texto e a descrição acessível do botão de áudio.
     */
    private void atualizarBotaoControle(boolean audioEmExecucao) {
        if (botaoControleAudio == null) {
            return;
        }

        int textoBotao = audioEmExecucao ? R.string.botao_pausar_audio : R.string.botao_reproduzir_audio;
        botaoControleAudio.setText(textoBotao);
        botaoControleAudio.setContentDescription(atividade.getString(textoBotao));
    }

    /**
     * Método responsável por avisar o usuário quando não for possível reproduzir áudio.
     */
    private void informarAudioIndisponivel() {
        atualizarBotaoControle(false);
        Toast.makeText(atividade, R.string.audio_indisponivel, Toast.LENGTH_SHORT).show();
    }
}

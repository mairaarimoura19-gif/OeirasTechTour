package com.oeirastechtour;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Autor: Equipe Oeiras Tech Tuor.
 * Classe responsável por salvar e recuperar as preferências do modo acessibilidade.
 */
public class PreferenciasAcessibilidade {

    // Constante responsável por identificar o arquivo local de preferências do aplicativo.
    private static final String NOME_PREFERENCIAS = "preferencias_acessibilidade_oeiras";

    // Constante responsável por identificar o estado do modo acessibilidade.
    private static final String CHAVE_MODO_ACESSIBILIDADE = "modo_acessibilidade_ativado";

    /**
     * Método construtor privado responsável por impedir criação desnecessária desta classe utilitária.
     */
    private PreferenciasAcessibilidade() {
    }

    /**
     * Método responsável por verificar se o modo acessibilidade está ativado.
     */
    public static boolean modoAcessibilidadeAtivado(Context contexto) {
        return obterPreferencias(contexto).getBoolean(CHAVE_MODO_ACESSIBILIDADE, true);
    }

    /**
     * Método responsável por salvar o estado atual do modo acessibilidade.
     */
    public static void salvarModoAcessibilidade(Context contexto, boolean modoAcessibilidadeAtivado) {
        obterPreferencias(contexto)
                .edit()
                .putBoolean(CHAVE_MODO_ACESSIBILIDADE, modoAcessibilidadeAtivado)
                .apply();
    }

    /**
     * Método responsável por acessar o arquivo local de preferências do aplicativo.
     */
    private static SharedPreferences obterPreferencias(Context contexto) {
        return contexto.getSharedPreferences(NOME_PREFERENCIAS, Context.MODE_PRIVATE);
    }
}

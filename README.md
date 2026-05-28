Oeiras Tech Tour
Oeiras foi a primeira capital do Piauí. Uma cidade do século XVIII com igrejas coloniais, casarões históricos e uma praça que foi palco da Proclamação da Independência do Piauí em 1823. Um patrimônio que existe há séculos — mas sem nenhuma ferramenta digital para quem chega na cidade e quer conhecê-la. Especialmente para quem tem deficiência visual.
O Oeiras Tech Tour nasceu pra mudar isso. É um aplicativo Android que funciona como guia turístico guiado por voz: quando você abre o app, a narração já começa. Não precisa configurar nada, não precisa de internet, e dá pra navegar pelo app inteiro sem olhar pra tela.

O que o app faz
Quando você abre o Oeiras Tech Tour, o áudio inicia automaticamente. A narração apresenta a cidade e convida o usuário a explorar a Praça das Vitórias — o primeiro ponto turístico coberto pelo app.
Na tela da praça, o conteúdo vai além de informações históricas. O roteiro descreve o espaço físico: os sons do ambiente, a textura do piso, os pontos de referência ao redor. Pensado pra que uma pessoa com deficiência visual consiga se orientar no espaço real enquanto ouve.
A navegação é feita por toque. Toque simples pausa ou retoma o áudio. Toque duplo volta pro menu. Se o usuário deslizar a tela pra rolar o conteúdo, o app reconhece que é uma rolagem e não confunde com um toque de navegação.
Tem também um botão pra ativar ou desativar o modo acessibilidade. O estado fica salvo entre sessões — se você desativou, na próxima abertura ainda está desativado.
E se o arquivo de áudio gravado falhar no dispositivo por algum motivo, o app detecta isso sozinho e usa a síntese de voz do Android pra continuar narrando em português. O usuário nunca fica sem conteúdo.

Tecnologias
O app foi desenvolvido em Android nativo com Java. Sem frameworks externos — só as APIs oficiais do Android.
O que usamosPra que serve no projetoJavaLógica de todas as telas e módulosXMLLayouts e identidade visualMediaPlayerReprodução dos áudios gravadosTextToSpeechLeitura automática quando o áudio falhaGestureDetectorDistinguir toque, toque duplo e rolagemSharedPreferencesSalvar o modo acessibilidade entre sessõesIntentNavegar entre telas

Estrutura do projeto
app/src/main/
├── java/com/oeirastechtour/
│   ├── MainActivity.java               # Tela inicial
│   ├── TelaPracaVitorias.java          # Tela da Praça das Vitórias
│   ├── NarradorAcessivel.java          # Sistema de áudio e acessibilidade
│   └── PreferenciasAcessibilidade.java # Persistência do modo acessibilidade
├── res/
│   ├── layout/
│   │   ├── tela_menu_principal.xml
│   │   └── tela_praca_vitorias.xml
│   ├── raw/
│   │   ├── audio_menu.mp3
│   │   └── audio_praca_vitorias.mp3
│   ├── drawable/                       # Imagens e backgrounds
│   └── values/
│       ├── strings.xml                 # Todos os textos
│       ├── colors.xml                  # Paleta de cores
│       └── themes.xml                  # Estilos reutilizáveis
└── AndroidManifest.xml

Como cada parte foi construída
MainActivity
É o ponto de entrada do app. Ela garante que o modo acessibilidade está ativo antes de carregar qualquer coisa na tela — isso foi uma decisão intencional pra que qualquer pessoa que abra o app pela primeira vez já receba a narração sem precisar configurar nada.
Ela também implementa uma trava simples: a variável aberturaPracaSolicitada impede que a tela da praça abra duas vezes se o usuário tocar muito rápido. E o GestureDetector foi configurado pra verificar se o toque começou em cima de um botão — se sim, ignora o detector e deixa o botão tratar normalmente. Se não, aí o gesto da tela entra em ação.
NarradorAcessivel
É o módulo de acessibilidade que todas as telas usam. Recebe o contexto, o nome do arquivo de áudio e um texto alternativo. Tenta reproduzir o áudio gravado via MediaPlayer. Se o arquivo não existir, cria um TextToSpeech configurado em português do Brasil e lê o texto.
A flag recursosLiberados existe por um motivo específico: quando a tela é destruída, o Android pode ainda estar carregando o áudio de forma assíncrona. Sem essa flag, o sistema tentaria reproduzir em uma Activity que já não existe mais — o que causaria um crash. Com ela, qualquer tentativa de reprodução depois que os recursos foram liberados é simplesmente ignorada.
TelaPracaVitorias
Os gestos aqui são diferentes da tela inicial. Toque simples pausa ou retoma o áudio — porque o usuário pode precisar parar pra prestar atenção ao ambiente ao redor. Toque duplo volta pro menu.
O texto de narração é montado pelo método montarTextoNarracaoPraca, que concatena 21 strings do strings.xml: título, subtítulo, 13 parágrafos descritivos e históricos, e as descrições dos 6 pontos turísticos ao redor da praça.
PreferenciasAcessibilidade
Classe utilitária que usa SharedPreferences pra salvar o estado do modo acessibilidade. O construtor é privado — foi uma decisão de design pra evitar que alguém instancie a classe desnecessariamente, já que todos os métodos são estáticos. O valor padrão é true, então qualquer usuário que abra o app pela primeira vez já recebe o áudio ativo.

Gestos por tela
Tela inicial
GestoO que fazToque simplesAbre a Praça das VitóriasToque duploAbre a Praça das VitóriasDeslizeRola o conteúdo normalmente
Tela da Praça das Vitórias
GestoO que fazToque simplesPausa ou retoma o áudioToque duploVolta pro menuDeslizeRola o conteúdo normalmente

Fluxo do áudio
Abre a tela
    ↓
Verifica modo acessibilidade
    ↓
Ativo → NarradorAcessivel.iniciarAudio()
    ↓
Procura o arquivo em res/raw/
    ↓
┌─────────────────────┬─────────────────────────┐
│  Arquivo encontrado │  Arquivo não encontrado  │
│         ↓           │           ↓              │
│   MediaPlayer toca  │  TextToSpeech lê o texto │
│   o áudio gravado   │  em português do Brasil  │
└─────────────────────┴─────────────────────────┘
    ↓
Tela vai pra segundo plano → pausarAudio()
Tela é destruída → liberarRecursos()

Pontos turísticos cobertos
PontoSobrePraça das VitóriasPalco da Proclamação da Independência do Piauí em 1823Igreja Matriz de Nossa Senhora das VitóriasPrincipal ponto religioso e histórico da cidadeMuseu de Arte Sacra de OeirasPrédio colonial com acervo de peças religiosas históricasPrefeitura MunicipalInstalada no Sobrado dos Ferraz, casarão colonialSolar das Doze JanelasConstrução histórica com fachada simétrica de doze janelasMuseu Os Doca NunesEspaço de preservação da memória e história localPousada do CônegoHospedagem colonial próxima à praça

Como rodar

Clone o repositório:

bashgit clone https://github.com/mairaarimoura19-gif/OeirasTechTour.git

Abra no Android Studio
Conecte um dispositivo Android ou inicie um emulador com API 24 ou superior
Clique em Run ou pressione Shift + F10

Não é necessário configurar nada — o app não depende de internet nem de serviços externos.

Como adicionar novos pontos turísticos
A estrutura foi pensada pra crescer. Pra adicionar um novo ponto:

Cria uma nova Activity Java
Cria o layout XML da tela
Adiciona o áudio em res/raw/
Adiciona as imagens em res/drawable/
Adiciona os textos em strings.xml
Instancia o NarradorAcessivel passando o nome do áudio e o texto alternativo

Nenhuma classe existente precisa ser modificada.

Requisitos

Android 7.0 (API 24) ou superior
Sem necessidade de internet
Sem permissões especiais


Desenvolvido durante Hackathon por equipe de Oeiras — PI.

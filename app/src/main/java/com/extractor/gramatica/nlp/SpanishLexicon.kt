package com.extractor.gramatica.nlp

/** Datos léxicos del español: palabras funcionales, adverbios, adjetivos frecuentes y verbos. */
object SpanishLexicon {

    private fun set(s: String): Set<String> =
        s.split(' ', '\n').filter { it.isNotBlank() }.toHashSet()

    /** Artículos, posesivos, demostrativos y cuantificadores (determinantes). */
    val determiners: Set<String> = set(
        "el la los las un una unos unas lo al del este esta estos estas ese esa esos esas " +
            "aquel aquella aquellos aquellas mi mis tu tus su sus nuestro nuestra nuestros nuestras " +
            "vuestro vuestra vuestros vuestras cada todo toda todos todas otro otra otros otras " +
            "mucho mucha muchos muchas poco poca pocos pocas varios varias algún alguno alguna " +
            "algunos algunas ningún ninguno ninguna ningunos ningunas cualquier cualesquiera tanto tanta " +
            "tantos tantas demás mismo misma mismos mismas ambos ambas cierto cierta ciertos ciertas " +
            "sendos sendas bastante bastantes demasiado demasiada demasiados demasiadas"
    )

    val pronouns: Set<String> = set(
        "yo tú vos él ella ello nosotros nosotras vosotros vosotras ellos ellas usted ustedes " +
            "me te se nos os le les mí ti sí conmigo contigo consigo que quien quienes cuyo cuya " +
            "cuyos cuyas cual cuales cuál cuáles qué quién quiénes cómo cuándo dónde cuánto cuánta " +
            "cuántos cuántas donde cuando como cuanto algo alguien nada nadie esto eso aquello " +
            "mío mía míos mías tuyo tuya tuyos tuyas suyo suya suyos suyas"
    )

    val prepositions: Set<String> = set(
        "a ante bajo cabe con contra de desde durante en entre hacia hasta mediante para por " +
            "según sin so sobre tras versus vía excepto salvo incluso"
    )

    val conjunctions: Set<String> = set(
        "y e o u ni pero sino aunque porque pues si mas ya luego mientras además entonces"
    )

    /** Palabras funcionales que nunca se extraen. */
    val stopwords: Set<String> = determiners + pronouns + prepositions + conjunctions + set(
        "no sí tampoco también ni etc aquí allí allá ahí acá hoy ayer mañana ahora antes después " +
            "siempre nunca jamás casi solo sólo apenas aún todavía así tal tales cual"
    )

    /** Adverbios frecuentes (los terminados en -mente se detectan por sufijo). */
    val adverbs: Set<String> = set(
        "muy más menos tan bien mal mejor peor aquí allí allá ahí acá hoy ayer mañana ahora antes " +
            "después luego pronto tarde temprano siempre nunca jamás casi apenas aún todavía ya " +
            "ahí cerca lejos arriba abajo adelante atrás dentro fuera encima debajo delante detrás " +
            "además incluso quizá quizás acaso tampoco también sólo solo bastante demasiado " +
            "mientras entonces enseguida anoche anteayer despacio deprisa aprisa mucho poco " +
            "así tal vez ojalá sí no"
    )

    /** Adverbios que se usan como cuantificadores de grado: la palabra siguiente suele ser adjetivo. */
    val degreeAdverbs: Set<String> = set("muy más menos tan bastante demasiado sumamente poco tan")

    /** Adjetivos frecuentes en masculino singular (el etiquetador prueba también las otras formas). */
    val adjectives: Set<String> = set(
        "grande pequeño nuevo viejo bueno malo mejor peor importante principal diferente igual " +
            "fácil difícil posible imposible necesario básico simple complejo claro oscuro largo " +
            "corto alto bajo ancho estrecho fuerte débil rápido lento cercano lejano antiguo " +
            "moderno actual futuro anterior posterior último primero segundo tercero siguiente " +
            "general particular especial común raro normal natural artificial digital real virtual " +
            "libre gratis único propio distinto mayor menor máximo mínimo total parcial completo " +
            "vacío lleno joven feliz triste contento seguro peligroso útil inútil capaz joven " +
            "blanco negro rojo azul verde amarillo gris rico pobre caro barato limpio sucio " +
            "frío caliente cálido tibio dulce amargo duro blando suave profundo superficial " +
            "público privado social político económico técnico científico histórico cultural " +
            "global local nacional internacional personal profesional humano animal mental " +
            "interesante interno externo medio sencillo preciso exacto correcto incorrecto " +
            "efectivo eficiente eficaz óptimo ideal ágil flexible estable inestable robusto " +
            "intuitivo visual manual automático dinámico estático lógico físico químico " +
            "biológico matemático numérico textual verbal oral escrito abierto cerrado " +
            "lindo bonito hermoso feo agradable cómodo incómodo tranquilo ruidoso silencioso " +
            "cansado ocupado listo preparado dispuesto disponible gran buen mal primer tercer san"
    )

    /** Sustantivos que terminan igual que adjetivos/verbos y se confundirían. */
    val nounExceptions: Set<String> = set(
        "lugar hogar azar par bar mar altar mujer poder placer taller alfiler carácter líder " +
            "cáncer porvenir elixir cable mueble pilar collar avatar ajedrez papel cualidad " +
            "señal animal canal hospital capital material general original oral metal cristal " +
            "mineral festival arsenal pedal portal terminal manual sistema problema tema programa " +
            "idioma esquema diagrama dilema poema teorema drama clima mapa día mano foto moto " +
            "radio video vídeo agua águila hacha arma alma área aula ala hambre"
    )

    /** Adjetivos terminados en -ar / -al etc. que no deben tomarse por infinitivos. */
    val adjectiveAr: Set<String> = set(
        "popular regular particular similar singular familiar militar titular espectacular " +
            "circular peculiar muscular escolar celular molecular polar solar lunar vulgar " +
            "angular rectangular secular ejemplar exemplar vascular nuclear lineal ordinal"
    )

    /** Verbos infinitivos de uso frecuente. "verbo:raíz" indica cambio de raíz (e→ie, o→ue, e→i). */
    val verbList: List<String> = (
        "hablar llevar llamar trabajar usar utilizar buscar encontrar:encuentr empezar:empiez " +
            "pensar:piens pasar dejar quedar esperar entrar mirar mostrar:muestr cambiar crear " +
            "generar analizar contar:cuent cerrar:cierr acabar ayudar necesitar tomar comprar pagar " +
            "jugar:jueg tratar considerar presentar practicar preguntar organizar recordar:recuerd " +
            "formar lograr terminar caminar viajar escuchar cantar bailar cocinar estudiar enseñar " +
            "desarrollar programar aplicar procesar almacenar calcular configurar instalar ejecutar " +
            "compilar guardar copiar pegar identificar clasificar mejorar evitar determinar " +
            "representar implementar diseñar publicar comunicar explicar indicar significar tocar " +
            "ganar gastar lavar limpiar preparar probar:prueb llegar mandar enviar cuidar andar " +
            "amar odiar apoyar actuar adaptar agregar alcanzar almorzar:almuerz aparecer apagar " +
            "arreglar asegurar atender:atiend averiguar bajar buscar celebrar cenar colocar " +
            "comenzar:comienz compartir completar comprobar:comprueb concentrar conectar consultar " +
            "continuar controlar convertir:conviert cortar costar:cuest cumplir dibujar durar " +
            "elegir:elij emplear encender:enciend entender:entiend escribir explorar extraer " +
            "filtrar fijar funcionar habitar imaginar importar incluir indicar insertar integrar " +
            "intentar invitar lanzar leer levantar llorar mantener marcar medir:mid mencionar " +
            "merecer modificar molestar mover:muev nacer nadar negar:nieg notar observar obtener " +
            "ocupar ofrecer olvidar operar ordenar organizar parecer partir pedir:pid pelear perder:pierd " +
            "permitir pertenecer pintar plantear poseer preferir:prefier prender producir " +
            "prometer proponer proteger proyectar quitar realizar rechazar recibir reconocer " +
            "recorrer recuperar reducir referir:refier regalar registrar relacionar repetir:repit " +
            "resolver:resuelv responder respetar resultar revisar rodear romper saltar salvar " +
            "seguir:sig sentar:sient sentir:sient servir:sirv sostener subir sufrir sugerir:sugier " +
            "temer terminar tirar tocar trabajar traducir traer transformar transmitir unir usar " +
            "valer vender vivir:viv volar:vuel volver:vuelv votar sacar dormir:duerm morir:muer " +
            "abrir aceptar acordar:acuerd acercar acompañar actualizar agregar añadir aprender " +
            "aprobar:aprueb asistir bailar beber buscar caer cargar casar cobrar coger comer comparar " +
            "conducir confiar conseguir:consig construir contener contestar crecer criticar dividir " +
            "dudar eliminar entregar escoger esperar establecer evaluar exigir existir expresar " +
            "faltar ganar gustar impedir:impid informar iniciar interesar investigar juntar " +
            "juzgar ocurrir pegar pretender procurar provocar reunir sembrar:siembr situar " +
            "soñar:sueñ suponer suceder tener tardar tender usar vencer viajar visitar " +
            "recibir leer escribir resumir extraer contar sumar restar dividir multiplicar " +
            "traducir validar verificar visualizar exportar importar compartir descargar cargar " +
            "navegar buscar encontrar seleccionar marcar resaltar subrayar copiar pegar borrar " +
            "arrastrar abrir cerrar guardar editar renombrar mover ordenar agrupar"
        ).split(' ').filter { it.isNotBlank() }.distinct()

    /** Formas de verbos irregulares: "infinitivo: formas...". */
    val irregulars: List<String> = listOf(
        "ser: soy eres es somos sois son era eras éramos erais eran fui fuiste fue fuimos fuisteis fueron " +
            "seré serás será seremos seréis serán sería serías seríamos serían sea seas seamos seáis sean " +
            "fuera fueras fuéramos fueran sido siendo sé",
        "estar: estoy estás está estamos estáis están estaba estabas estábamos estaban estuve estuviste " +
            "estuvo estuvimos estuvieron estaré estarás estará estarán estaría estarían esté estés estemos " +
            "estén estuviera estuvieran estado estando",
        "ir: voy vas va vamos vais van iba ibas íbamos ibais iban iré irás irá iremos irán iría irían " +
            "vaya vayas vayamos vayan ido yendo",
        "haber: he has ha hemos habéis han había habías habíamos habían hube hubo hubieron habré habrás " +
            "habrá habremos habrán habría habrían haya hayas hayamos hayan hubiera hubieran habido habiendo hay",
        "tener: tengo tienes tiene tenemos tenéis tienen tenía tenías teníamos tenían tuve tuviste tuvo " +
            "tuvimos tuvieron tendré tendrás tendrá tendremos tendrán tendría tendrían tenga tengas " +
            "tengamos tengan tuviera tuvieran tenido teniendo",
        "hacer: hago haces hace hacemos hacéis hacen hacía hacías hacíamos hacían hice hiciste hizo " +
            "hicimos hicieron haré harás hará haremos harán haría harían haga hagas hagamos hagan " +
            "hiciera hicieran hecho haciendo",
        "poder: puedo puedes puede podemos podéis pueden podía podías podíamos podían pude pudiste pudo " +
            "pudimos pudieron podré podrás podrá podremos podrán podría podrían pueda puedas podamos " +
            "puedan pudiera pudieran podido pudiendo",
        "decir: digo dices dice decimos decís dicen decía decías decíamos decían dije dijiste dijo " +
            "dijimos dijeron diré dirás dirá diremos dirán diría dirían diga digas digamos digan " +
            "dijera dijeran dicho diciendo",
        "ver: veo ves ve vemos veis ven veía veías veíamos veían vi viste vio vimos vieron veré verás " +
            "verá veremos verán vería verían vea veas veamos vean viera vieran visto viendo",
        "dar: doy das da damos dais dan daba dabas dábamos daban di diste dio dimos dieron daré darás " +
            "dará daremos darán daría darían dé des demos den diera dieran dado dando",
        "saber: sé sabes sabe sabemos sabéis saben sabía sabías sabíamos sabían supe supiste supo " +
            "supimos supieron sabré sabrás sabrá sabremos sabrán sabría sabrían sepa sepas sepamos " +
            "sepan supiera supieran sabido sabiendo",
        "querer: quiero quieres quiere queremos queréis quieren quería querías queríamos querían " +
            "quise quisiste quiso quisimos quisieron querré querrás querrá querremos querrán querría " +
            "querrían quiera quieras queramos quieran quisiera quisieran querido queriendo",
        "venir: vengo vienes viene venimos venís vienen venía venías veníamos venían vine viniste " +
            "vino vinimos vinieron vendré vendrás vendrá vendremos vendrán vendría vendrían venga " +
            "vengas vengamos vengan viniera vinieran venido viniendo",
        "poner: pongo pones pone ponemos ponéis ponen ponía ponías poníamos ponían puse pusiste puso " +
            "pusimos pusieron pondré pondrás pondrá pondremos pondrán pondría pondrían ponga pongas " +
            "pongamos pongan pusiera pusieran puesto poniendo",
        "salir: salgo sales sale salimos salís salen salía salías salíamos salían salí saliste salió " +
            "salieron saldré saldrás saldrá saldremos saldrán saldría saldrían salga salgas salgamos " +
            "salgan saliera salieran salido saliendo",
        "deber: debo debes debe debemos debéis deben debía debías debíamos debían debí debiste debió " +
            "debimos debieron deberé deberás deberá deberemos deberán debería deberían deba debas " +
            "debamos deban debiera debieran debido debiendo",
        "conocer: conozco conoces conoce conocemos conocéis conocen conocía conocían conocí conoció " +
            "conocieron conoceré conocerá conocerán conocería conozca conozcan conocido conociendo",
        "parecer: parezco pareces parece parecemos parecen parecía parecían parecí pareció parecieron " +
            "parecerá parecerán parecería parezca parezcan parecido pareciendo",
        "creer: creo crees cree creemos creéis creen creía creían creí creíste creyó creyeron creeré " +
            "creerá creerán creería crea crean creído creyendo",
        "seguir: sigo sigues sigue seguimos seguís siguen seguía seguían seguí siguió siguieron " +
            "seguiré seguirá seguirán seguiría siga sigan seguido siguiendo",
        "traer: traigo traes trae traemos traéis traen traía traían traje trajiste trajo trajeron " +
            "traeré traerá traerán traería traiga traigan traído trayendo",
        "oír: oigo oyes oye oímos oís oyen oía oían oí oyó oyeron oiré oirá oirán oiga oigan oído oyendo",
        "caer: caigo caes cae caemos caéis caen caía caían caí cayó cayeron caeré caerá caerán caiga " +
            "caigan caído cayendo",
        "producir: produzco produces produce producimos producen producía producían produje produjo " +
            "produjeron producirá producirán produzca produzcan producido produciendo",
        "traducir: traduzco traduces traduce traducimos traducen traducía traducían tradujo " +
            "tradujeron traducirá traduzca traducido traduciendo",
        "reducir: reduzco reduces reduce reducimos reducen reducía reducían redujo redujeron " +
            "reducirá reduzca reducido reduciendo",
        "obtener: obtengo obtienes obtiene obtenemos obtienen obtenía obtenían obtuve obtuvo " +
            "obtuvieron obtendrá obtendrán obtenga obtengan obtenido obteniendo",
        "mantener: mantengo mantienes mantiene mantenemos mantienen mantenía mantenían mantuve " +
            "mantuvo mantuvieron mantendrá mantendrán mantenga mantengan mantenido manteniendo",
        "contener: contengo contienes contiene contenemos contienen contenía contenían contuvo " +
            "contuvieron contendrá contendrán contenga contengan contenido conteniendo",
        "proponer: propongo propones propone proponemos proponen proponía proponían propuso " +
            "propusieron propondrá propondrán proponga propongan propuesto proponiendo",
        "suponer: supongo supones supone suponemos suponen suponía suponían supuso supusieron " +
            "supondrá supondrán suponga supongan supuesto suponiendo",
        "reír: río ríes ríe reímos ríen reía reían rio rieron reirá ría rían reído riendo",
        "morir: muero mueres muere morimos mueren moría morían murió murieron morirá morirán muera " +
            "mueran muerto muriendo",
        "volver: vuelvo vuelves vuelve volvemos vuelven volvía volvían volví volvió volvieron " +
            "volverá volverán vuelva vuelvan vuelto volviendo",
        "abrir: abro abres abre abrimos abren abría abrían abrí abrió abrieron abrirá abrirán abra " +
            "abran abierto abriendo",
        "escribir: escribo escribes escribe escribimos escriben escribía escribían escribí escribió " +
            "escribieron escribirá escribirán escriba escriban escrito escribiendo",
        "romper: rompo rompes rompe rompemos rompen rompía rompían rompí rompió rompieron romperá " +
            "rompa rompan roto rompiendo",
        "resolver: resuelvo resuelves resuelve resolvemos resuelven resolvía resolví resolvió " +
            "resolvieron resolverá resuelva resuelto resolviendo"
    )
}

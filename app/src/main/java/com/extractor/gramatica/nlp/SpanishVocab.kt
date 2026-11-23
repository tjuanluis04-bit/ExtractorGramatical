package com.extractor.gramatica.nlp

/** Vocabulario ampliado del español. Se fusiona con [SpanishLexicon]. */
object SpanishVocab {

    private fun words(s: String): List<String> =
        s.split(' ', '\n').filter { it.isNotBlank() }

    /** Verbos (infinitivo; "inf:raíz" marca el cambio de raíz en presente). */
    val verbs: List<String> = words(
        "contaminar encantar elogiar proteger? planear madrugar atardecer? polucionar reciclar " +
            "abandonar abordar abusar acabar acelerar acentuar acompañar aconsejar acostumbrar activar " +
            "actuar acumular adaptar adjuntar administrar admirar adoptar adquirir:adquier advertir:adviert " +
            "afectar afirmar agotar agradecer agrupar aguantar ahorrar alcanzar alegrar alentar:alient " +
            "alimentar aliviar alquilar alterar ampliar anotar anticipar anunciar apagar apartar " +
            "apostar:apuest apreciar apuntar arrancar arrojar asociar aspirar asumir atacar atar " +
            "atravesar:atravies atribuir aumentar autorizar avanzar avisar bastar besar botar brillar " +
            "burlar cancelar cansar capturar caracterizar causar ceder cesar charlar chocar circular " +
            "citar cobrar colaborar coleccionar colgar:cuelg combatir combinar comentar cometer " +
            "compensar competir:compit complementar complicar comprender concebir:concib concluir " +
            "concretar condenar confesar:confies confirmar conformar confundir congelar conseguir:consig " +
            "consistir constar constituir consumir contactar contemplar contratar contribuir convencer " +
            "convivir cooperar corregir:corrig correr cruzar cubrir culminar cultivar curar dañar " +
            "declarar dedicar defender:defiend definir delegar demostrar:demuestr denominar depender " +
            "depositar derivar derrotar desaparecer descansar descartar describir descubrir desear " +
            "desempeñar deshacer designar desplazar destacar destinar destruir detallar detectar " +
            "detener devolver dictar difundir digitalizar dirigir disculpar disfrutar disminuir " +
            "disponer distinguir distribuir divertir:diviert doblar documentar dominar duplicar " +
            "editar educar efectuar ejercer elaborar elevar emitir emocionar empaquetar emparejar " +
            "emprender encabezar encajar encargar encender:enciend encerrar:encierr encuestar enfocar " +
            "enfrentar engañar enlazar enriquecer ensuciar enterar entrenar entretener enumerar " +
            "envolver equilibrar equipar equivocar escapar esforzar:esfuerz esquivar estimar " +
            "estimular estructurar evaluar evolucionar exagerar examinar exceder excluir exhibir " +
            "exigir experimentar explicar extender:extiend fabricar facilitar fallar fallecer " +
            "favorecer felicitar figurar financiar firmar flotar fluir formalizar formular fortalecer " +
            "fracasar frenar frecuentar fundar garantizar gestionar girar gobernar:gobiern gozar " +
            "grabar graduar gritar guiar habilitar hallar heredar herir:hier hervir:hierv huir " +
            "ignorar iluminar ilustrar imitar impactar imprimir impulsar incentivar incorporar " +
            "incrementar inducir influir ingresar innovar inspeccionar inspirar instruir interactuar " +
            "interpretar interrumpir intervenir introducir invadir inventar invertir:inviert " +
            "involucrar jugar:jueg jurar justificar lamentar lanzar limitar localizar lograr " +
            "luchar manejar manifestar:manifiest mantener maravillar marchar mejorar memorizar " +
            "mencionar mentir:mient merecer mezclar migrar mirar modelar modificar mojar montar " +
            "mostrar:muestr motivar mudar multiplicar nombrar notar notificar obedecer objetar " +
            "obligar observar obtener ocasionar ocultar ocurrir ofender ofrecer operar oponer " +
            "optar optimizar ordenar organizar orientar originar pactar padecer parar participar " +
            "pasear patrocinar pecar pegar peinar penetrar percibir perdonar perfeccionar permanecer " +
            "perseguir:persig persistir persuadir pertenecer pesar picar planear planificar plantar " +
            "plantear poblar:pueblo? poseer posponer practicar precisar predecir preferir:prefier " +
            "preguntar premiar prender preocupar prescindir presenciar presentar presionar prestar " +
            "presumir pretender prevenir prever probar:prueb proceder procesar proclamar procurar " +
            "producir programar progresar prohibir prolongar promover:promuev pronunciar propagar " +
            "proporcionar protagonizar proteger provenir provocar proyectar publicar quebrar:quiebr " +
            "quejar quemar quitar rascar reaccionar realizar rebajar recaudar recetar rechazar " +
            "recibir reclamar recoger recomendar:recomiend reconocer reconstruir recopilar recordar:recuerd " +
            "recorrer recuperar redactar redondear reducir reemplazar referir:refier reflejar " +
            "reflexionar reforzar:refuerz regalar regir:rig registrar regresar regular rehacer " +
            "reinar relacionar relajar rellenar remitir remover:remuev rendir:rind renovar:renuev " +
            "renunciar reparar repartir repasar repetir:repit replicar reportar reposar representar " +
            "reproducir requerir:requier reservar resguardar residir resistir resolver respaldar " +
            "respetar respirar responder responsabilizar restar restaurar restringir resultar " +
            "resumir retener retirar retomar retornar revelar revisar revolver rezar robar rodar " +
            "rogar:rueg rondar saborear sacar sacudir saludar salvar sancionar satisfacer secar " +
            "segmentar seleccionar sembrar:siembr sentar:sient sentir:sient señalar separar sepultar " +
            "simbolizar simplificar simular sintetizar situar sobrar sobresalir sobrevivir socializar " +
            "solicitar soler:suel soltar:suelt solucionar someter sonar:suen sonreír sopesar " +
            "sorprender sospechar sostener subir subrayar suceder sufrir sugerir:sugier sumar " +
            "superar supervisar suplicar suponer suprimir surgir suscribir suspender sustituir " +
            "sustentar tapar tardar teclear tejer temer tender:tiend tentar:tient terminar tirar " +
            "titular tocar tolerar tomar torcer:tuerc tornar trabajar tramitar transcribir " +
            "transferir:transfier transformar transitar transmitir transportar tratar trazar " +
            "triunfar tropezar:tropiez unificar unir usar utilizar vaciar validar valorar variar " +
            "vencer vender verificar viajar vibrar vigilar vincular visitar visualizar vivir " +
            "volar:vuel votar vulnerar zanjar zarpar " +
            "abrazar absorber abstraer acceder accionar aclarar acoger acordar:acuerd acortar " +
            "acostar:acuest acreditar actualizar acudir acusar adelantar adivinar adjudicar admitir " +
            "advertir agarrar agendar agilizar agregar aislar ajustar alabar alargar alcanzar " +
            "aligerar almacenar almorzar:almuerz amanecer amenazar andar anular añadir aparecer " +
            "aplazar aplicar aplaudir aportar apostar apoyar aprender aprobar aprovechar aproximar " +
            "archivar argumentar armar arreglar arriesgar articular asegurar asesorar asignar " +
            "asimilar asistir atender atrapar aunar autenticar automatizar averiguar bendecir " +
            "bloquear bombardear bordar borrar buscar calcular calificar calmar cambiar caminar " +
            "cantar captar cargar casar catalogar celebrar cenar cepillar chequear cifrar citar " +
            "clasificar codificar cocinar coincidir colar colocar comenzar:comienz comparar " +
            "compartir compilar completar componer comportar comprar comprobar:comprueb comunicar " +
            "concentrar concordar:concuerd condicionar conectar conectarse confiar configurar " +
            "conjugar conmover:conmuev conocer consagrar conservar considerar consolidar " +
            "constatar consultar contabilizar contar:cuent contener contestar continuar contradecir " +
            "contrastar controlar convertir:conviert convocar copiar coordinar corresponder " +
            "cortar costar:cuest crear crecer creer criar criticar cuantificar cuestionar cuidar " +
            "cumplir dar deber decidir decir declinar decorar dedicar deducir defraudar dejar " +
            "demandar demorar denunciar depurar derivar desactivar desafiar desarrollar desayunar " +
            "descargar descomponer desconectar desconocer describir descuidar desechar desgastar " +
            "deshabilitar designar desmontar despedir:despid despertar:despiert despegar desplegar:desplieg " +
            "desviar detallar detectar determinar dibujar dictar diferenciar dificultar diluir " +
            "diseñar disfrazar disimular disolver disparar dispersar distar distanciar divulgar " +
            "dotar durar ejecutar elegir emplear enfermar enfriar enseñar ensayar entender:entiend " +
            "entrar entregar enviar envejecer erradicar escalar escanear escoger escribir escuchar " +
            "especificar especular esperar esquematizar establecer estacionar estallar estar " +
            "estudiar evidenciar evocar exclamar exhortar expandir expedir:expid explotar exponer " +
            "exportar expresar extraer extrapolar fechar filmar filtrar fingir fomentar forjar " +
            "formar fotocopiar fraccionar fundamentar fusionar generalizar generar gestar guardar " +
            "gustar hablar hacer hospedar hospitalizar idear identificar igualar imaginar impartir " +
            "implantar implementar implicar imponer importar impresionar improvisar impugnar " +
            "inaugurar incidir inclinar incluir incomodar indagar indicar indignar infectar " +
            "inferir:infier infiltrar informatizar infringir ingerir:ingier inhibir inicializar " +
            "iniciar injertar inmovilizar inscribir insertar insistir instalar instaurar " +
            "instituir integrar intensificar intentar intercambiar interferir:interfier internar " +
            "interrogar intimar intuir invocar ironizar irritar jerarquizar juntar lavar leer " +
            "legalizar legitimar levantar liberar librar licenciar liderar limpiar liquidar " +
            "listar llamar llegar llenar llevar llorar llover:lluev maltratar mandar manipular " +
            "mantener maquetar marcar mediar medir:mid meditar mejorar mentalizar mercantilizar " +
            "merendar:meriend mover:muev nacer nadar navegar necesitar negar:nieg negociar nevar:niev " +
            "nivelar normalizar nutrir obsequiar ocupar odiar oler:huel olvidar omitir operar " +
            "ordenar oscilar otorgar pagar palpar parecer partir pasar patinar pedir:pid pelear " +
            "pensar:piens perder:pierd perfilar perfumar perjudicar permitir pesar pintar pisar " +
            "planchar plasmar platicar poder poner portar posicionar postular precipitar predominar " +
            "preferir prepararse preparar presidir prestar prevalecer privar privatizar probar " +
            "proferir profundizar prometer pronosticar proponer prosperar proteger proveer " +
            "provocar publicitar pulir pulsar puntuar rastrear razonar reaccionar reactivar " +
            "realizar reanudar rebotar recalcar recapitular recargar reclutar recomponer " +
            "reconciliar reconfigurar recordar recortar recrear rectificar recurrir redefinir " +
            "redimir reducir reeditar reelegir:reelij reembolsar reenviar referenciar reformar " +
            "reformular refugiar refutar regenerar regocijar reiniciar reiterar relatar relevar " +
            "rellenar remarcar remontar remunerar renombrar reordenar reorganizar repasar " +
            "reprobar:reprueb reprimir reprogramar reprochar repudiar requerir resaltar " +
            "rescatar resentir:resient reseñar resguardar resignar respaldar restablecer " +
            "restituir retar retrasar retratar retroceder reunificar reunir revalidar " +
            "revertir:reviert revestir:revist revocar rotar rozar rubricar rugir saber sabotear " +
            "salar salir salpicar sanar sanear satisfacer secuenciar sectorizar sedimentar " +
            "seducir seguir:sig sellar sensibilizar sentenciar señalizar sincronizar sintonizar " +
            "sistematizar sobrecargar sobrellevar sobrepasar sobreponer sobrevolar:sobrevuel " +
            "sofocar solapar soldar:sueld solventar sombrear soportar sorprender sortear " +
            "sosegar:sosieg subcontratar subestimar sublevar subvencionar sugestionar " +
            "sujetar sumergir sumir suministrar superponer suprimir surtir suspirar sustraer " +
            "tabular tachar tallar tantear tasar tatuar telefonear tematizar temblar:tiembl " +
            "tipificar tolerar tonificar topar torear tostar:tuest totalizar trabar traducir " +
            "traer tramar trascender:trasciend trasladar traspasar trastornar tributar trillar " +
            "trocar:truec turnar ubicar ultimar unificar untar urbanizar urgir usurpar " +
            "vacunar vagar valer variar vaticinar vedar vegetar velar vendar ventilar ver " +
            "verbalizar versar vestir:vist vetar viabilizar vigorizar violar visibilizar " +
            "vislumbrar vitorear vocalizar vociferar voltear vomitar yacer zambullir zumbar " +
            "pulverizar compaginar compilar renderizar parametrizar encriptar desencriptar " +
            "parsear depurar desplegar escalar monitorear iterar instanciar serializar " +
            "tokenizar indexar enrutar empaquetar desempaquetar compactar comprimir descomprimir " +
            "sincronizar replicar respaldar restaurar migrar cachear emular virtualizar " +
            "anonimizar etiquetar clusterizar clasificar segmentar normalizar entrenar inferir " +
            "predecir optimizar generalizar sobreajustar ajustar validar testear probar"
    ).filter { !it.endsWith("?") && !it.contains("?") }

    /** Adjetivos (forma masculina singular o invariable; se reconocen también sus flexiones). */
    val adjectives: List<String> = words(
        "abierto absoluto abstracto absurdo abundante académico accesible activo actual adaptativo " +
            "adecuado adicional adjunto administrativo admirable adulto afectivo afortunado agradable " +
            "agresivo agrícola agudo ajeno alegre alemán alérgico alimenticio alternativo amable amarillo " +
            "ambiental ambicioso ambiguo amplio analítico anciano angosto anónimo ansioso antiguo " +
            "anual apropiado arbitrario argentino armónico aromático arquitectónico artesanal artificial " +
            "artístico asequible asombroso atento atractivo audaz audiovisual auténtico automático " +
            "autónomo auxiliar avanzado básico beneficioso bilingüe biológico blando brillante " +
            "brusco burocrático cálido capaz cariñoso cartesiano casual categórico católico causal " +
            "célebre central cercano cerrado cierto cívico claro clásico climático clínico cómico " +
            "cómodo compacto comparable compatible competente competitivo complejo complementario " +
            "completo complicado comprensible comprometido computacional común comunitario conceptual " +
            "concreto concurrente conectado confiable confidencial confortable confuso congruente " +
            "conocido consciente consecutivo conservador considerable consistente constante " +
            "constructivo contemporáneo contento continuo contrario controvertido conveniente " +
            "convencional cooperativo coordinado corporativo correcto cortés cósmico costoso creativo " +
            "creciente crítico crónico crucial cruel cualitativo cuantitativo cuidadoso cultural " +
            "curioso curricular curvo cotidiano cuidadosa débil decisivo decorativo deficiente " +
            "definitivo delicado demasiado democrático demográfico denso dependiente deportivo " +
            "derecho descriptivo desconocido desigual despejado destacado detallado determinado " +
            "devastador diario dichoso diestro diferente difícil digno dilatado diminuto directo " +
            "disciplinario discreto diseñado disperso disponible distante distinto diverso divertido " +
            "divino doloroso doméstico dominante dorado dramático dudoso dulce duradero económico " +
            "educativo efectivo eficaz eficiente eléctrico electrónico elegante elemental elevado " +
            "eliminado elocuente emocional emocionante empírico emprendedor enérgico enorme " +
            "entero entusiasta equilibrado equivalente erróneo esencial especializado específico " +
            "espectacular espeso espiritual espontáneo estable estadístico estándar estatal estético " +
            "estimado estratégico estrecho estricto estructural estructurado estupendo ético " +
            "europeo evidente evolutivo exacto excelente excepcional excesivo exclusivo exhaustivo " +
            "exitoso exótico experimental experto explícito exterior externo extraordinario extranjero " +
            "extraño extremo fabuloso fácil factible falso familiar famoso fantástico fascinante " +
            "fatal favorable favorito federal feliz femenino feroz fértil fiable fiel figurativo " +
            "fijo filosófico final financiero fino firme fiscal físico flexible flojo florido " +
            "fluido folclórico formal formativo fortuito fraccionario frágil francés franco frecuente " +
            "fresco frío frondoso frustrante fuerte fundamental funcional furioso futuro gaseoso " +
            "generoso genético genial gigante global glorioso gradual gramatical grande gratuito " +
            "grave griego grosero grueso gubernamental habitual hábil hermoso heterogéneo histórico " +
            "hondo honesto horizontal hostil huidizo humano húmedo humilde idéntico ideal ideológico " +
            "igual ilegal ilimitado ilustre imaginario imborrable imparcial impecable imperativo " +
            "implícito imponente importante imposible impredecible impresionante improbable " +
            "inadecuado inaccesible incansable incierto incluido incompleto incorrecto increíble " +
            "independiente indicado indígena indirecto indispensable individual industrial inesperado " +
            "inestable inevitable infantil inferior infinito inflexible influyente informal " +
            "informático informativo ingenioso inglés inicial injusto inmediato inmenso innecesario " +
            "innovador inocente inquietante insaciable inseguro insignificante insistente " +
            "insuficiente integral íntegro inteligente intenso interactivo interesante interior " +
            "intermedio interno interpersonal intuitivo inútil inválido inventivo inverso " +
            "invisible irregular irreal irrelevante irreversible italiano izquierdo japonés joven " +
            "judicial jurídico justo juvenil lateral latino legal legítimo lejano lento letal " +
            "liberal libre ligero limitado limpio lineal líquido listo literal literario llamativo " +
            "lleno lógico lúcido lujoso luminoso lunar maduro magnífico majestuoso malo manual " +
            "marcado marino marítimo masculino masivo material matemático maternal máximo mayor " +
            "mecánico mediano medible medio mediterráneo mejor melancólico memorable menor mensual " +
            "mental mercantil meticuloso metódico mexicano mezclado miedoso mínimo minoritario " +
            "misterioso mítico mixto moderado moderno modesto molecular molesto monótono moral " +
            "motivado móvil movedizo mudo multicultural múltiple mundial municipal musical mutuo " +
            "nacional natal natural necesario negativo nervioso neto neutro nítido nocivo nocturno " +
            "normal nórdico notable notorio novedoso nuclear nuevo nulo numérico numeroso nutritivo " +
            "obligatorio obvio occidental oceánico ocioso oculto ocupado oficial ofensivo olímpico " +
            "operativo oportuno óptimo oral ordinario orgánico organizado original originario " +
            "oscuro paciente pacífico paralelo parcial particular pasado pasajero pasivo patente " +
            "patrimonial pedagógico peligroso penoso pequeño perceptible perdido perenne perezoso " +
            "perfecto periódico permanente perpetuo persistente personal persuasivo pertinente " +
            "perverso pesado pesimista pintoresco plano plástico plausible pleno plural pobre " +
            "poderoso poético polémico policial político polivalente popular portátil posible " +
            "positivo posterior potencial práctico preciso predecible predominante preferente " +
            "preferido preliminar premiado presente presidencial prestigioso previo primario " +
            "primero principal prioritario privado probable problemático procedimental productivo " +
            "profesional profundo programado progresivo prohibido prolongado prominente promedio " +
            "pronto propicio propio proporcional prospectivo protector provechoso provisional " +
            "próximo prudente psicológico público puntual puro químico racional radical rápido " +
            "raro razonable real realista reciente recíproco reconocido recto recurrente redondo " +
            "reducido reflexivo regional registrado regular relacionado relativo relevante " +
            "religioso remoto renovable repetitivo representativo reservado residencial resistente " +
            "respetable respetuoso responsable restante restringido resultante retórico reversible " +
            "revolucionario rico ridículo riguroso rígido rítmico robusto rojo romántico rudo " +
            "rural sabio sagrado sagaz sano satisfactorio saturado secreto secundario seco sectorial " +
            "seguro selectivo semanal semejante sencillo sensato sensible sensorial sentimental " +
            "sereno serio significativo silencioso similar simple simultáneo sincero singular " +
            "siniestro sintético sistemático situado sobrio social sofisticado solar solemne " +
            "solidario sólido solitario soltero sonoro sospechoso suave subjetivo sublime " +
            "subsiguiente sucesivo sucio suficiente superficial superior supremo sutil tácito " +
            "tangible tardío técnico tecnológico telefónico temático temeroso temporal tenaz " +
            "tendencioso teórico terapéutico terrible territorial tibio tierno tímido típico " +
            "tolerante tonto torpe total tóxico tradicional trágico tranquilo transparente " +
            "transversal trivial tropical turístico último único unido unitario universal " +
            "universitario urbano urgente usual útil vacío vago válido valioso variable variado " +
            "varios vasto vecino vegetal veloz vencido verbal verdadero verde vertical viable " +
            "vial vibrante victorioso vigente vigoroso viral virtual visible visual vital vivo " +
            "voluminoso voluntario vulnerable zonal inalámbrico binario booleano cifrado " +
            "dinámico estático lógico paralelo recursivo secuencial genérico homogéneo híbrido " +
            "nativo remoto local distribuido escalable modular concurrente asíncrono síncrono " +
            "seguro vulnerable cuántico neuronal profundo supervisado inteligente adaptativo " +
            "predictivo generativo automatizado autónomo bajo alto largo corto ancho gordo delgado " +
            "pesado ligero joven viejo mayor menor bueno malo gran buen primer tercer san " +
            "mismo propio ajeno cada cualquiera sumo nimio mero cierto dicho citado mencionado " +
            "siguiente anterior actual presente futuro pasado venidero entrante saliente " +
            "español mexicano colombiano argentino chileno peruano venezolano cubano brasileño " +
            "portugués chino ruso americano africano asiático árabe coreano canadiense " +
            "estadounidense británico holandés sueco noruego turco danés finlandés polaco " +
            "irlandés escocés galés catalán vasco gallego andaluz madrileño parisino " +
            "ocho"
    ).filter { it != "ocho" && it != "cada" && it != "cualquiera" }

    /** Adverbios que no terminan en -mente. */
    val adverbs: List<String> = words(
        "aparte apenas aquí allí allá ahí acá arriba abajo adelante atrás adentro afuera alrededor " +
            "además ahora anoche anteanoche anteayer antes aún aun ayer bien casi cerca claro " +
            "como cuando debajo delante demasiado dentro después detrás enfrente encima entonces " +
            "enseguida generalmente hoy incluso jamás lejos luego mal mañana más menos mientras " +
            "mucho muy nunca ojalá peor poco pronto quizá quizás realmente recién siempre sobre " +
            "tampoco tan tarde temprano todavía ya despacio deprisa aprisa regular mejor " +
            "también tal así asimismo acaso aproximadamente exactamente simplemente sólo solo " +
            "justo junto bastante harto alguna cuanto cuán donde adonde apenas mayormente " +
            "casualmente finalmente inicialmente primeramente seguidamente sucesivamente"
    )

    /** Sustantivos deverbales o de uso muy frecuente que coinciden con formas verbales. Gana la lectura nominal. */
    val nounFirst: List<String> = words(
        "trabajo trabajos cambio cambios uso usos cuenta cuentas control controles apoyo apoyos " +
            "análisis estudio estudios desarrollo desarrollos proceso procesos registro registros " +
            "resumen resúmenes cargo cargos paso pasos interés intereses acceso accesos gasto gastos " +
            "aporte aportes cultivo cultivos diseño diseños ahorro ahorros pago pagos cobro cobros " +
            "envío envíos ingreso ingresos consumo consumos descarga descargas carga cargas " +
            "búsqueda búsquedas compra compras venta ventas prueba pruebas muestra muestras " +
            "ayuda ayudas llamada llamadas salida salidas entrada entradas bajada subida respuesta " +
            "respuestas pregunta preguntas propuesta propuestas vuelta vueltas queja quejas lucha " +
            "marcha firma firmas forma formas visita visitas cita citas crítica críticas práctica " +
            "prácticas teoría teorías cuento cuentos canto cantos juego juegos baile bailes " +
            "cocina cocinas termino términos estimación estimaciones cálculo cálculos " +
            "método métodos modelo modelos grado grados ejemplo ejemplos resultado resultados " +
            "objetivo objetivos sistema sistemas problema problemas programa programas " +
            "tema temas sentido sentidos fin fines medida medidas libro libros motivo motivos " +
            "nombre nombres base bases mando mandos lanzamiento lanzamientos riesgo riesgos " +
            "ritmo ritmos sueño sueños deseo deseos cierre cierres comienzo comienzos " +
            "abrigo abrigos amparo apunte apuntes aviso avisos banco bancos beso besos bloque " +
            "bloques borde bordes brillo broma calco canje censo cerco choque choques cobro " +
            "cómputo conteo corte cortes costo costos cruce cruces culto debate debates " +
            "defensa defensas deporte descanso descuento descuentos desvío despido destino " +
            "destinos detalle detalles dibujo dibujos dominio dominios drama dudas duda ejercicio " +
            "ejercicios elogio embargo empleo empleos empuje encargo encargos encuentro encuentros " +
            "enfoque enfoques enlace enlaces ensayo ensayos entorno entrenamiento envase " +
            "equipo equipos error errores escrito escritos esfuerzo esfuerzos espera esperas " +
            "estima estudio examen exámenes exceso exigencia explicación exploración exporte " +
            "fallo fallos filtro filtros formato formatos fruto frutos gira giro giros goce " +
            "gobierno gobiernos golpe golpes grito gritos guía guías hallazgo hallazgos hecho " +
            "hechos huella ingreso intento intentos invento juicio juicios labor labores lanza " +
            "lectura lecturas lista listas llegada logro logros lugar mejora mejoras memoria " +
            "mención mezcla mezclas mira mirada miradas monto movimiento movimientos nota notas " +
            "ocupación oferta ofertas olvido operación orden órdenes pausa pausas pedido pedidos " +
            "pega pesa peso pesos plan planes planta plantas plazo plazos pliegue polvo premio " +
            "premios presenta presentación presión producto productos promedio promesa promesas " +
            "puesto puestos quiebra rastro rastros reclamo reclamos recorrido recorridos recurso " +
            "recursos reparto repaso reporte reportes repuesto reserva reservas retiro retiros " +
            "retorno reto retos reunión reuniones revisión riesgo rodeo rueda saldo salto saltos " +
            "salud sello señal señales sondeo sorteo sostén suma sumas surtido tala tapa " +
            "tarea tareas tarda tiro tiros toque toques trato tratos trazo traslado trueque " +
            "turno turnos validez valor valores vestido viaje viajes vista vistas voto votos " +
            "uso unión usuario usuarios dato datos clave claves código códigos texto textos " +
            "parte partes música física química práctica política lógica matemática gramática " +
            "informática electrónica mecánica estética ética óptica clínica técnica estadística " +
            "dinámica genética botánica semántica sintaxis fonética lingüística retórica hipótesis " +
            "frío mañana tarde veces vez corte arte artes sede sedes fuente fuentes puente puentes cumbre cumbres " +
            "palabra palabras término frase frases oración oraciones párrafo párrafos"
    )

    /** Sustantivos demasiado genéricos para considerarse terminología. */
    val genericNouns: List<String> = words(
        "cosa cosas vez veces parte partes forma formas tipo tipos manera maneras caso casos " +
            "modo modos lado lados punto puntos momento momentos tiempo tiempos día días año años " +
            "hora horas lugar lugares persona personas gente gentes hombre hombres mujer mujeres " +
            "niño niños vida vidas mundo mundos país países ciudad ciudades casa casas número " +
            "números cantidad cantidades grupo grupos conjunto conjuntos serie series idea ideas " +
            "tema temas hecho hechos razón razones sentido sentidos ejemplo ejemplos nombre nombres " +
            "problema problemas pregunta preguntas respuesta respuestas trabajo trabajos aspecto " +
            "aspectos elemento elementos factor factores situación situaciones condición condiciones " +
            "nivel niveles medio medios fin fines final finales principio principios inicio inicios " +
            "resto lista listas texto textos palabra palabras frase frases información informaciones " +
            "dato datos uso usos función funciones objeto objetos tarea tareas acción acciones " +
            "opción opciones posibilidad posibilidades capacidad capacidades necesidad necesidades " +
            "importancia diferencia diferencias relación relaciones tipo desafío desafíos reto retos " +
            "mejora mejoras resultado resultados cambio cambios proceso procesos forma sistema " +
            "sistemas área áreas campo campos zona zonas espacio espacios lado orden diversos " +
            "cuestión cuestiones tendencia tendencias etapa etapas fase fases paso pasos camino " +
            "caminos línea líneas clase clases categoría categorías base bases estado estados " +
            "valor valores ámbito ámbitos contexto contextos propósito propósitos medida medidas " +
            "manera ocasión ocasiones motivo motivos causa causas efecto efectos consecuencia " +
            "consecuencias aunque etc ello algo nada todo vez mayoría minoría total mitad " +
            "alumno alumnos estudiante estudiantes profesor profesores usuario usuarios " +
            "empresa empresas persona gobierno equipo equipos miembro miembros"
    )
}

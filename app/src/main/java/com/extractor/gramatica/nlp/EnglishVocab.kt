package com.extractor.gramatica.nlp

/** Vocabulario del inglés: verbos, adjetivos, adverbios y formas irregulares. */
object EnglishVocab {

    private fun words(s: String): List<String> = s.split(' ', '\n').filter { it.isNotBlank() }

    /** Verbos regulares (forma base). */
    val verbs: List<String> = words(
        "accept access achieve acquire act adapt add address adjust admit adopt advise affect afford agree " +
            "aim allow analyze announce answer apply approach approve argue arrange arrive ask assess assign " +
            "assist assume attach attack attempt attend attract avoid back base bear beat become begin believe " +
            "belong benefit bind block boost borrow bother break bring broadcast build burn buy calculate call " +
            "cancel care carry cause celebrate change charge check choose claim clean clear click climb close " +
            "collect combine come comment commit communicate compare compete complain complete compute " +
            "concentrate conclude conduct confirm connect consider consist contact contain continue contribute " +
            "control convert cook copy correct cost count cover crash create cross cut deal debug decide " +
            "declare decline decrease define deliver demand demonstrate deny depend deploy describe deserve " +
            "design destroy detect determine develop differ direct discover discuss display distribute divide " +
            "download draw dream drive drop earn eat edit educate effect elect eliminate embrace emerge " +
            "emphasize employ enable encounter encourage end enforce engage enhance enjoy ensure enter " +
            "establish estimate evaluate examine exceed exchange execute exercise exist expand expect " +
            "experience explain explore export express extend extract face fail fall feed feel fight fill " +
            "filter find finish fit fix focus follow force forget form found frame function gain gather " +
            "generate get give grant grow guarantee guide handle happen hate head help hide highlight hire " +
            "hold hope host identify ignore illustrate imagine implement imply import impose improve include " +
            "increase indicate influence inform initiate insert install integrate intend interact introduce " +
            "diagnose invest investigate invite involve join judge jump keep kill know label lack land launch lead " +
            "learn leave lend let lie lift like limit link list listen live load locate lock log look lose " +
            "love maintain make manage map mark match matter mean measure meet mention merge migrate mind " +
            "miss mix modify monitor move name navigate need negotiate note notice obtain occur offer open " +
            "operate optimize order organize overcome own pack pass pay perform permit pick place plan play " +
            "point post predict prefer prepare present preserve press prevent print process produce program " +
            "progress promise promote protect prove provide publish pull purchase push put qualify question " +
            "raise reach read realize receive recognize recommend record recover reduce refer reflect refuse " +
            "regard register reject relate release rely remain remember remove render repair repeat replace " +
            "reply report represent request require research reserve resolve respond restore restrict result " +
            "retain retrieve return reveal review revise reward ride ring rise roll run sail save say scan " +
            "schedule score search seek select sell send separate serve set settle shake share shift shine " +
            "shoot shop show shut sign simplify sing sit skip sleep slide smile solve sort sound speak " +
            "specify speed spend split spread stand start state stay steal step stick stop store stream " +
            "stress strike study submit succeed suffer suggest suit supply support suppose survive suspect " +
            "switch take talk target teach tell test thank think threaten throw touch track trade train " +
            "transfer transform translate travel treat trigger trust try turn type understand unite update " +
            "upgrade upload use utilize validate value view visit wait wake walk want warn wash watch wear " +
            "welcome win wish wonder work worry write yield visualize repair paint cook jump swim bake " +
            "dance sing visit protect fix wander dig plant harvest tackle hurt kick pour sweep shout whisper " +
            "contaminate pollute recycle erupt flood damage rescue vote debate negotiate arrest sentence"
    )

    /** Verbos irregulares: "base: forma3sg pasado participio gerundio". */
    val irregular: List<String> = listOf(
        "be: is am are was were been being",
        "have: has had having",
        "do: does did done doing",
        "go: goes went gone going",
        "say: says said saying",
        "make: makes made making",
        "know: knows knew known knowing",
        "think: thinks thought thinking",
        "take: takes took taken taking",
        "see: sees saw seen seeing",
        "come: comes came coming",
        "give: gives gave given giving",
        "find: finds found finding",
        "tell: tells told telling",
        "become: becomes became becoming",
        "leave: leaves left leaving",
        "feel: feels felt feeling",
        "bring: brings brought bringing",
        "begin: begins began begun beginning",
        "keep: keeps kept keeping",
        "hold: holds held holding",
        "write: writes wrote written writing",
        "stand: stands stood standing",
        "hear: hears heard hearing",
        "let: lets letting",
        "mean: means meant meaning",
        "set: sets setting",
        "meet: meets met meeting",
        "run: runs ran running",
        "pay: pays paid paying",
        "sit: sits sat sitting",
        "speak: speaks spoke spoken speaking",
        "lie: lies lay lain lying",
        "lead: leads led leading",
        "read: reads reading",
        "grow: grows grew grown growing",
        "lose: loses lost losing",
        "fall: falls fell fallen falling",
        "send: sends sent sending",
        "build: builds built building",
        "understand: understands understood understanding",
        "draw: draws drew drawn drawing",
        "break: breaks broke broken breaking",
        "spend: spends spent spending",
        "cut: cuts cutting",
        "rise: rises rose risen rising",
        "drive: drives drove driven driving",
        "buy: buys bought buying",
        "wear: wears wore worn wearing",
        "choose: chooses chose chosen choosing",
        "eat: eats ate eaten eating",
        "sell: sells sold selling",
        "win: wins won winning",
        "throw: throws threw thrown throwing",
        "fly: flies flew flown flying",
        "sing: sings sang sung singing",
        "swim: swims swam swum swimming",
        "teach: teaches taught teaching",
        "catch: catches caught catching",
        "fight: fights fought fighting",
        "forget: forgets forgot forgotten forgetting",
        "get: gets got gotten getting",
        "put: puts putting",
        "show: shows showed shown showing",
        "hide: hides hid hidden hiding",
        "shake: shakes shook shaken shaking",
        "steal: steals stole stolen stealing",
        "wake: wakes woke woken waking",
        "sleep: sleeps slept sleeping",
        "deal: deals dealt dealing",
        "lend: lends lent lending",
        "bear: bears bore born borne bearing",
        "beat: beats beaten beating",
        "bind: binds bound binding",
        "bite: bites bit bitten biting",
        "blow: blows blew blown blowing",
        "burn: burns burnt burning",
        "dig: digs dug digging",
        "feed: feeds fed feeding",
        "hurt: hurts hurting",
        "ride: rides rode ridden riding",
        "ring: rings rang rung ringing",
        "shoot: shoots shot shooting",
        "shut: shuts shutting",
        "slide: slides slid sliding",
        "split: splits splitting",
        "spread: spreads spreading",
        "strike: strikes struck striking",
        "sweep: sweeps swept sweeping",
        "swing: swings swung swinging",
        "tear: tears tore torn tearing",
        "undertake: undertakes undertook undertaken undertaking",
        "withdraw: withdraws withdrew withdrawn withdrawing",
        "overcome: overcomes overcame overcoming",
        "foresee: foresees foresaw foreseen foreseeing",
        "broadcast: broadcasts broadcasting",
        "quit: quits quitting",
        "seek: seeks sought seeking",
        "wind: winds wound winding",
        "light: lights lit lighting",
        "lay: lays laid laying",
        "can: could", "will: would", "shall: should", "may: might", "must: must"
    )

    /** Palabras que son a la vez nombre y verbo (ganan la lectura nominal tras determinante). */
    val nounVerb: Set<String> = words(
        "judge work study use change plan design process control test support report result request order offer " +
            "answer need look love place point post record research return review watch walk talk trade " +
            "train transfer travel treat trust try turn type update upgrade view visit vote way wish " +
            "cost count cover cross damage dance deal drop effect end face fall field focus form frame " +
            "function guide hope host increase influence issue label lack land launch limit link list load " +
            "lock log map mark match matter mind miss mix move name note offer paint pass pay plant play " +
            "present press print produce program progress promise purchase question rate reach reply " +
            "report rest rise roll run sale save scan schedule score search set share shift show sign " +
            "sound speed spend stage start state step stock stop store stream stress study supply " +
            "switch target taste test touch track treat trial use value visit wait walk wave wish " +
            "bank code file cloud update download flood fix hit hunt kick lift paste patch pick pitch " +
            "plot pool pump rush sail seal sketch smell spot stand swim tap tour"
    ).toHashSet()

    /** Adjetivos frecuentes. */
    val adjectives: Set<String> = words(
        "next fragile able abstract academic accurate active actual additional adequate advanced afraid aggressive " +
            "alive alternative amazing ancient angry annual anonymous apparent appropriate artificial " +
            "automatic available average aware bad basic beautiful best better big binary bitter black " +
            "blind blue bold brave brief bright broad broken brown busy calm capable careful central " +
            "certain cheap chemical civil classic clean clear clever close cold common comfortable " +
            "complete complex complicated comprehensive concrete conscious constant conventional cool " +
            "correct critical cruel cultural curious current daily dangerous dark dead dear decent deep " +
            "delicious dense dependent different difficult digital direct dirty distinct diverse domestic " +
            "double dry dull dynamic eager early easy economic effective efficient electric electronic " +
            "elegant emotional empty enormous entire environmental equal essential eventual exact " +
            "excellent exciting expensive experimental explicit external extra extreme fair false " +
            "familiar famous fantastic fast fat favorite federal final financial fine firm first fit " +
            "flat flexible foreign formal former fortunate free frequent fresh friendly full fun " +
            "functional fundamental funny future general generous gentle genuine giant glad global good " +
            "grand grateful great green grey gross guilty happy hard harmful healthy heavy helpful high " +
            "historic holy honest horizontal hot huge human humble hungry ideal ill illegal imaginary " +
            "immediate immense important impossible impressive inadequate independent industrial " +
            "inevitable informal initial inner innocent innovative intelligent intense interactive " +
            "interesting internal international joint junior key kind large last late latest lazy " +
            "leading legal light likely limited little live local logical lonely long loose lost loud " +
            "low loyal lucky mad magic main major male many marine massive mature maximum mean medical " +
            "mental mere minimum minor missing mobile modern modest moral mutual narrow national " +
            "native natural necessary negative nervous net neutral new nice noble normal northern " +
            "novel nuclear numerous obvious odd official old only open operational opposite optimal " +
            "oral ordinary organic original other outer outstanding overall own pale parallel partial " +
            "particular passive past patient perfect permanent personal physical plain pleasant poor " +
            "popular positive possible potential powerful practical precise predictable pregnant " +
            "present pretty previous primary prime principal prior private probable productive " +
            "professional profound proper proud public pure quick quiet random rapid rare raw ready " +
            "real realistic reasonable recent regional regular relevant reliable religious remote " +
            "representative responsible rich right rigid ripe rough round royal rural sad safe scientific " +
            "secondary secret secure senior sensitive separate serious several severe sharp short " +
            "shy sick significant silent silly similar simple sincere single skilled slight slow small " +
            "smart smooth social soft solar solid sophisticated sorry southern special specific " +
            "spiritual stable standard steady steep sticky still straight strange strategic strict " +
            "strong stupid subtle successful sudden sufficient suitable super superior sure " +
            "surprising sweet tall technical technological temporary terrible thick thin tight tiny " +
            "tired top total tough traditional tremendous tropical true typical ugly ultimate unable " +
            "unexpected unfair unique unknown unlikely unusual upper urban urgent useful useless usual " +
            "valid valuable various vast verbal vertical viable virtual visible visual vital vivid " +
            "voluntary vulnerable warm weak wealthy weekly weird welcome western wet white whole wide " +
            "wild willing wise wonderful wooden worldwide worried worse worst worth wrong young " +
            "supervised unsupervised neural deep convolutional recurrent generative predictive " +
            "statistical algorithmic quantum cognitive molecular genetic cellular biological " +
            "chemical electrical mechanical thermal optical magnetic digital analog scalable " +
            "distributed concurrent asynchronous synchronous relational structured unstructured " +
            "northern southern eastern western heavy cheap lazy brown quick fresh"
    ).toHashSet()

    val adverbs: Set<String> = words(
        "better very quite always never often still already soon well too now just really almost rather " +
            "perhaps maybe however therefore thus together away today tomorrow yesterday later early " +
            "far near ever even much more most less least here there again once twice then also " +
            "else instead meanwhile otherwise anyway somehow somewhat sometimes usually yet " +
            "tonight afterwards forward backward upward downward apart ahead abroad indoors outdoors " +
            "downtown upstairs downstairs overseas hardly barely nearly merely simply only deep " +
            "fast hard late long straight daily weekly monthly yearly nowhere everywhere anywhere " +
            "somewhere nonetheless moreover furthermore consequently accordingly alike " +
            "indeed certainly obviously clearly exactly"
    ).toHashSet()

    /** Palabras en -ly que no son adverbios. */
    val notAdverbLy: Set<String> = words(
        "family assembly supply reply apply fly rely comply imply multiply july italy jelly belly " +
            "bully rally ally anomaly monopoly butterfly friendly lovely lonely ugly silly elderly " +
            "likely unlikely costly orderly holy only early daily weekly monthly yearly lively " +
            "deadly lonely curly bubbly chilly hilly sly ply fly"
    ).toHashSet()

    val stopwords: Set<String> = words(
        "the a an this that these those my your his her its our their some any each every all both " +
            "no neither either and or but nor so yet if because although though while as than then which " +
            "who whom whose what when where why how of in on at by for with about against between into " +
            "through during before after above below to from up down out off over under again further " +
            "i me you he him she it we us they them myself yourself himself herself itself ourselves " +
            "themselves not there here other another one two three four five six seven eight nine ten " +
            "twenty thirty hundred thousand million billion such per via whether whereas unless until " +
            "since upon within without toward towards among across along around behind beyond near " +
            "can could will would should may might must shall do does did am is are was were be been being " +
            "has have had having cannot"
    ).toHashSet()
}

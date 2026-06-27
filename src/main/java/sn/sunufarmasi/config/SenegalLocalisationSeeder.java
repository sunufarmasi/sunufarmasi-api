package sn.sunufarmasi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.entity.Departement;
import sn.sunufarmasi.localisation.entity.Pays;
import sn.sunufarmasi.localisation.entity.Region;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.localisation.repository.DepartementRepository;
import sn.sunufarmasi.localisation.repository.PaysRepository;
import sn.sunufarmasi.localisation.repository.RegionRepository;

import java.text.Normalizer;
import java.util.List;

/**
 * Seed complet des régions, départements et communes du Sénégal (RGPH-5 2023).
 * Idempotent : ne recrée pas les entités déjà existantes.
 * Skippé si communes >= 200 (données déjà chargées).
 */
@Component
@Order(5)
@RequiredArgsConstructor
@Slf4j
public class SenegalLocalisationSeeder implements ApplicationRunner {

    private final PaysRepository paysRepository;
    private final RegionRepository regionRepository;
    private final DepartementRepository departementRepository;
    private final CommuneRepository communeRepository;

    // ── helpers ──────────────────────────────────────────────────────────────

    private String slug(String nom) {
        String s = Normalizer.normalize(nom, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return s.length() > 15 ? s.substring(0, 15) : s;
    }

    private Region findOrCreateRegion(Pays pays, String code, String nom) {
        return regionRepository.findByCodeAndPaysId(code, pays.getId()).orElseGet(() ->
            regionRepository.findByPaysIdOrderByOrdreAscNomAsc(pays.getId()).stream()
                .filter(r -> r.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElseGet(() -> regionRepository.save(
                        Region.builder().pays(pays).code(code).nom(nom).actif(true).build()))
        );
    }

    private Departement findOrCreateDept(Region region, String code, String nom) {
        // Try by code first
        var byCode = departementRepository.findByCodeAndRegionId(code, region.getId());
        if (byCode.isPresent()) return byCode.get();
        // Try by name (handles manually created depts with different codes)
        return departementRepository.findByRegionIdOrderByOrdreAscNomAsc(region.getId()).stream()
                .filter(d -> d.getNom().equalsIgnoreCase(nom))
                .findFirst()
                .orElseGet(() -> departementRepository.save(
                        Departement.builder().region(region).code(code).nom(nom).actif(true).build()));
    }

    private void addCommune(Departement dept, String nom) {
        // Check by code (fast path)
        String code = slug(nom);
        if (communeRepository.existsByCodeAndDepartementId(code, dept.getId())) return;
        // Check by name — existing data may use accented codes (e.g. THIÈS_EST vs THIES_EST)
        boolean nameExists = communeRepository.findByDepartementIdOrderByOrdreAscNomAsc(dept.getId())
                .stream().anyMatch(c -> c.getNom().equalsIgnoreCase(nom));
        if (!nameExists) {
            communeRepository.save(Commune.builder()
                    .departement(dept).code(code).nom(nom).actif(true).build());
        }
    }

    // ── main entry ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long count = communeRepository.count();
        if (count >= 200) {
            log.info("🗺️  Géographie Sénégal déjà complète ({} communes)", count);
            return;
        }
        log.info("🌍 Seeding géographie Sénégal…");

        Pays sn = paysRepository.findByCode("SN").orElseGet(() -> paysRepository.save(
                Pays.builder().code("SN").codeIso2("SN").codeIso3("SEN")
                        .nom("Sénégal").nomEn("Senegal").capitale("Dakar")
                        .indicatifTelephonique("+221").devise("XOF")
                        .fuseauHoraire("Africa/Dakar").actif(true).build()));

        seedDakar(sn);
        seedZiguinchor(sn);
        seedDiourbel(sn);
        seedSaintLouis(sn);
        seedKaffrine(sn);
        seedKedougou(sn);
        seedKolda(sn);
        seedMatam(sn);
        seedSedhiou(sn);
        seedFatick(sn);
        seedKaolack(sn);
        seedLouga(sn);
        seedTambacounda(sn);
        seedThies(sn);

        log.info("✅ Géographie Sénégal: {} régions | {} départements | {} communes",
                regionRepository.count(), departementRepository.count(), communeRepository.count());
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 1. DAKAR
    // ═════════════════════════════════════════════════════════════════════════
    private void seedDakar(Pays sn) {
        Region r = findOrCreateRegion(sn, "DK", "Dakar");

        Departement dakar = findOrCreateDept(r, "DK-DAK", "Dakar");
        for (String c : List.of("Gorée", "Dakar Plateau", "Médina", "Gueule Tapée Fass Colobane",
                "Fann Point E Amitié", "Grand Dakar", "Biscuiterie", "HLM", "Hann Bel Air",
                "Sicap Liberté", "Dieuppeul Derkle", "Ouakam", "Ngor", "Yoff",
                "Mermoz Sacré Cœur", "Grand Yoff", "Patte d'Oie", "Parcelles Assainies", "Cambérène"))
            addCommune(dakar, c);

        Departement guediawaye = findOrCreateDept(r, "DK-GDW", "Guédiawaye");
        for (String c : List.of("Golf Sud", "Sam Notaire", "Ndiaréme Limamoulaye",
                "Wakhinane Nimzatt", "Médina Gounass"))
            addCommune(guediawaye, c);

        Departement pikine = findOrCreateDept(r, "DK-PIK", "Pikine");
        for (String c : List.of("Pikine Ouest", "Pikine Est", "Pikine Nord", "Dalifort",
                "Djidah Thiaroye Kao", "Guinaw Rail Nord", "Guinaw Rail Sud", "Thiaroye Sur Mer",
                "Tivaouane Diacksao", "Diamaguène Sicap Mbao", "Thiaroye Gare", "Mbao"))
            addCommune(pikine, c);

        Departement rufisque = findOrCreateDept(r, "DK-RUF", "Rufisque");
        for (String c : List.of("Bargny", "Sendou", "Rufisque Est", "Rufisque Nord", "Rufisque Ouest",
                "Bambilor", "Sangalkam", "Tivaouane Peulh Niagha", "Diamniadio", "Sébikotane", "Yène"))
            addCommune(rufisque, c);

        Departement keurMassar = findOrCreateDept(r, "DK-KMS", "Keur Massar");
        for (String c : List.of("Yeumbeul Nord", "Yeumbeul Sud", "Malika",
                "Keur Massar Nord", "Jaxaay Parcelles", "Keur Massar Sud"))
            addCommune(keurMassar, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 2. ZIGUINCHOR
    // ═════════════════════════════════════════════════════════════════════════
    private void seedZiguinchor(Pays sn) {
        Region r = findOrCreateRegion(sn, "ZG", "Ziguinchor");

        Departement bignona = findOrCreateDept(r, "ZG-BIG", "Bignona");
        for (String c : List.of("Bignona", "Thionck Essyl", "Djibidione", "Oulampane", "Sindian",
                "Suelle", "Balinghore", "Diégoune", "Kartiack", "Mangagoulack", "Mlomp",
                "Coubalan", "Niamone", "Ouonck", "Tenghori", "Djinaky",
                "Kafountine", "Kataba 1", "Diouloulou"))
            addCommune(bignona, c);

        Departement oussouye = findOrCreateDept(r, "ZG-OUS", "Oussouye");
        for (String c : List.of("Oussouye", "Diembering", "Santhiaba Manjack", "Oukout",
                "Mlomp"))
            addCommune(oussouye, c);

        Departement ziguinchor = findOrCreateDept(r, "ZG-ZIG", "Ziguinchor");
        for (String c : List.of("Ziguinchor", "Adéane", "Boutoupa Camaracounda",
                "Niaguis", "Enampore", "Niassia"))
            addCommune(ziguinchor, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 3. DIOURBEL
    // ═════════════════════════════════════════════════════════════════════════
    private void seedDiourbel(Pays sn) {
        Region r = findOrCreateRegion(sn, "DL", "Diourbel");

        Departement bambey = findOrCreateDept(r, "DL-BAM", "Bambey");
        for (String c : List.of("Bambey", "Baba Garage", "Dinguiraye", "Keur Samba Kane",
                "Gawane", "Lambaye", "Ngogom", "Refane", "Ndangalma", "Ndondol", "Ngoye",
                "Thiakhar", "Taiba Moutoupha", "Touba Lappe", "Ndoulo", "Ngohe",
                "Pattar", "Tocky Gare", "Toure Mbonde"))
            addCommune(bambey, c);

        Departement diourbel = findOrCreateDept(r, "DL-DIO", "Diourbel");
        for (String c : List.of("Diourbel", "Ndankh Sene", "Gade Escale", "Ndindy", "Keur Ngalgou"))
            addCommune(diourbel, c);

        Departement mbacke = findOrCreateDept(r, "DL-MBA", "Mbacké");
        for (String c : List.of("Mbacké", "Dendeye Gouy Gui", "Darou Salam Typ", "Kael", "Madina",
                "Ndioumane", "Touba Mboul", "Darou Nahim", "Taiba Thiekene", "Dalla Ngabou",
                "Missirah", "Nghaye", "Touba Fall", "Touba Mosquée", "Sadio", "Taif"))
            addCommune(mbacke, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 4. SAINT-LOUIS
    // ═════════════════════════════════════════════════════════════════════════
    private void seedSaintLouis(Pays sn) {
        Region r = findOrCreateRegion(sn, "SL", "Saint-Louis");

        Departement dagana = findOrCreateDept(r, "SL-DAG", "Dagana");
        for (String c : List.of("Dagana", "Richard Toll", "Mbane", "Bokhol", "Gae",
                "Ndombo Sandjiry", "Diama", "Ngnith", "Ronkh", "Rosso", "Ross Bethio"))
            addCommune(dagana, c);

        Departement podor = findOrCreateDept(r, "SL-POD", "Podor");
        for (String c : List.of("Podor", "Ndioum", "Madina Ndiathbe", "Doumga Lao", "Mery",
                "Gollere", "Mboumba", "Aere Lao", "Walalde", "Boke Dialloube", "Mbolo Birane",
                "Pete", "Galoya Toucouleur", "Fanaye", "Ndiayene Peindao", "Niandane",
                "Dodel", "Gamadji Sare", "Guede Village", "Bode Lao", "Demette", "Guede Chantier"))
            addCommune(podor, c);

        Departement stLouis = findOrCreateDept(r, "SL-STL", "Saint-Louis");
        for (String c : List.of("Saint Louis", "Gandon", "Fass Ngom", "Ndiebene Gandiole", "Mpal"))
            addCommune(stLouis, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 5. KAFFRINE
    // ═════════════════════════════════════════════════════════════════════════
    private void seedKaffrine(Pays sn) {
        Region r = findOrCreateRegion(sn, "KF", "Kaffrine");

        Departement kaffrine = findOrCreateDept(r, "KF-KAF", "Kaffrine");
        for (String c : List.of("Kaffrine", "Boulel", "Gniby", "Kahi", "Diokoul Belbouck",
                "Kathiotte", "Medinatoul Salam 2", "Diamagadio", "Nganda"))
            addCommune(kaffrine, c);

        Departement birkelane = findOrCreateDept(r, "KF-BIR", "Birkelane");
        for (String c : List.of("Birkelane", "Keur Mboucki", "Touba Mbella", "Diamal",
                "Mabo", "Ndiognick", "Mbeuleup"))
            addCommune(birkelane, c);

        Departement koungheul = findOrCreateDept(r, "KF-KOU", "Koungheul");
        for (String c : List.of("Koungheul", "Saly Escale", "Fass Thiekene", "Ida Mouride",
                "Lour Escale", "Ribot Escale", "Ngainthe Pathe", "Maka Yop", "Missirah Wadene"))
            addCommune(koungheul, c);

        Departement malemHoddar = findOrCreateDept(r, "KF-MAL", "Malem Hoddar");
        for (String c : List.of("Malem Hoddar", "Darou Minam II", "Ndioum Ngainth", "Khelcom",
                "Ndiobene Samba Lamo", "Dianke Souf", "Sagna"))
            addCommune(malemHoddar, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 6. KÉDOUGOU
    // ═════════════════════════════════════════════════════════════════════════
    private void seedKedougou(Pays sn) {
        Region r = findOrCreateRegion(sn, "KG", "Kédougou");

        Departement kedougou = findOrCreateDept(r, "KG-KED", "Kédougou");
        for (String c : List.of("Kédougou", "Bandafassi", "Tomboroncoto", "Dindefelo",
                "Ninefecha", "Dimboli", "Fongolimbi"))
            addCommune(kedougou, c);

        Departement salemata = findOrCreateDept(r, "KG-SAL", "Salemata");
        for (String c : List.of("Salemata", "Dakately", "Kevoye", "Dar Salam", "Ethiolo", "Oubadji"))
            addCommune(salemata, c);

        Departement saraya = findOrCreateDept(r, "KG-SAR", "Saraya");
        for (String c : List.of("Saraya", "Medina Baffe", "Bembou", "Khossanto",
                "Missirah Sirimana", "Sabodala"))
            addCommune(saraya, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 7. KOLDA
    // ═════════════════════════════════════════════════════════════════════════
    private void seedKolda(Pays sn) {
        Region r = findOrCreateRegion(sn, "KD", "Kolda");

        Departement kolda = findOrCreateDept(r, "KD-KOL", "Kolda");
        for (String c : List.of("Kolda", "Dioulacolon", "Medina El Hadj", "Tankanto Escale",
                "Guiro Yero Bocar", "Salikegne", "Sare Yoba Diega", "Bagadadji",
                "Coumbacara", "Mampatim", "Dialambere", "Medina Cherif", "Dabo",
                "Sare Bidji", "Thietty"))
            addCommune(kolda, c);

        Departement velingara = findOrCreateDept(r, "KD-VEL", "Vélingara");
        for (String c : List.of("Velingara", "Bonconto", "Linkering", "Medina Gounass",
                "Sinthiang Koundara", "Ouassadou", "Pakour", "Paroumba", "Kandia",
                "Sare Coly Salle", "Nemataba", "Kandiaye", "Diaobe Kabendou", "Kounkane"))
            addCommune(velingara, c);

        Departement medinaYoroFoulah = findOrCreateDept(r, "KD-MYF", "Médina Yoro Foulah");
        for (String c : List.of("Medina Yoro Foulah", "Fafacourou", "Badion", "Ndorna",
                "Bignarabe", "Bourouco", "Koulinto"))
            addCommune(medinaYoroFoulah, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 8. MATAM
    // ═════════════════════════════════════════════════════════════════════════
    private void seedMatam(Pays sn) {
        Region r = findOrCreateRegion(sn, "MT", "Matam");

        Departement matam = findOrCreateDept(r, "MT-MAT", "Matam");
        for (String c : List.of("Matam", "Ourossogui", "Agnam Civol", "Orefonde", "Dabia",
                "Thilogne", "Nguidjilone", "Bokidiawe", "Nabadji Civol", "Ogo"))
            addCommune(matam, c);

        Departement kanel = findOrCreateDept(r, "MT-KAN", "Kanel");
        for (String c : List.of("Kanel", "Ouaounde", "Dembancane", "Odobere", "Ndendory",
                "Wouro Sidy", "Hamady Hounare", "Sinthiou Bamambe Banadji",
                "Bokiladji", "Orkadiere", "Aoure", "Semme"))
            addCommune(kanel, c);

        Departement ranerou = findOrCreateDept(r, "MT-RAN", "Ranerou");
        for (String c : List.of("Ranerou", "Lougre Thioly", "Velingara", "Oudalaye"))
            addCommune(ranerou, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 9. SÉDHIOU
    // ═════════════════════════════════════════════════════════════════════════
    private void seedSedhiou(Pays sn) {
        Region r = findOrCreateRegion(sn, "SD", "Sédhiou");

        Departement sedhiou = findOrCreateDept(r, "SD-SED", "Sédhiou");
        for (String c : List.of("Sedhiou", "Marsassoum", "Diende", "Sakar", "Diannah Ba",
                "Koussy", "Oudoucar", "Sama Kanta Peulh", "Diannah Malary", "Bemet Bidjini",
                "San Samba", "Djibabouya", "Bambaly", "Djiredji"))
            addCommune(sedhiou, c);

        Departement bounkiling = findOrCreateDept(r, "SD-BOU", "Bounkiling");
        for (String c : List.of("Bounkiling", "Ndiamalathiel", "Boghal", "Tankon", "Djinany",
                "Ndiamacouta", "Bona", "Diacounda", "Inor", "Kandion Mangana",
                "Diaroume", "Diambati", "Faoune", "Madina Wandifa"))
            addCommune(bounkiling, c);

        Departement goudomp = findOrCreateDept(r, "SD-GOU", "Goudomp");
        for (String c : List.of("Goudomp", "Djibanar", "Kaour", "Mangaroungou Santo",
                "Simbandi Balante", "Yarang Balante", "Diattacounda", "Samine",
                "Karantaba", "Kolibantang", "Niagha", "Simbandi Brassou",
                "Baghere", "Diouboudou", "Tanaff"))
            addCommune(goudomp, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 10. FATICK
    // ═════════════════════════════════════════════════════════════════════════
    private void seedFatick(Pays sn) {
        Region r = findOrCreateRegion(sn, "FK", "Fatick");

        Departement fatick = findOrCreateDept(r, "FK-FAT", "Fatick");
        for (String c : List.of("Fatick", "Dioffior", "Diaoul", "Mbellacadiao", "Ndiop",
                "Thiare Ndialgui", "Diakhao", "Fimela", "Loul Sessene", "Palmarin Facao",
                "Djilasse", "Ngayokheme", "Niakhar", "Patar", "Diarrere",
                "Diouroup", "Tattaguine"))
            addCommune(fatick, c);

        Departement foundiougne = findOrCreateDept(r, "FK-FOU", "Foundiougne");
        for (String c : List.of("Foundiougne", "Sokone", "Djilor", "Diossong", "Diagane Barka",
                "Mbam", "Niassene", "Passy", "Soum", "Bassoul", "Dionewar", "Djirnda",
                "Keur Saloum Diane", "Keur Samba Gueye", "Nioro Alassane Tall",
                "Toubacouta", "Karang Poste"))
            addCommune(foundiougne, c);

        Departement gossas = findOrCreateDept(r, "FK-GOS", "Gossas");
        for (String c : List.of("Gossas", "Colobane", "Mbar", "Ndiene Lagane", "Ouadiour", "Patar Lia"))
            addCommune(gossas, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 11. KAOLACK
    // ═════════════════════════════════════════════════════════════════════════
    private void seedKaolack(Pays sn) {
        Region r = findOrCreateRegion(sn, "KL", "Kaolack");

        Departement kaolack = findOrCreateDept(r, "KL-KAO", "Kaolack");
        for (String c : List.of("Kaolack", "Kahone", "Keur Soce", "Ndiaffate", "Ndiedieng",
                "Latmingue", "Thiare", "Keur Baka", "Ndoffane", "Dya", "Ndiebel",
                "Thiomby", "Gandiaye", "Sibassor"))
            addCommune(kaolack, c);

        Departement nioro = findOrCreateDept(r, "KL-NIO", "Nioro du Rip");
        for (String c : List.of("Nioro", "Kayemor", "Medina Sabakh", "Ngayene", "Paoskoto",
                "Gainthe Kaye", "Porokhane", "Taiba Niassene", "Dabaly", "Darou Salam",
                "Keur Maba Diakhou", "Ndrawe Escale", "Wack Ngouna",
                "Keur Mandongo", "Keur Madiabel"))
            addCommune(nioro, c);

        Departement guinguineo = findOrCreateDept(r, "KL-GUI", "Guinguinéo");
        for (String c : List.of("Guinguineo", "Mbadakhoune", "Ndiago", "Ngathie Naoude",
                "Fass", "Nguelou", "Gagnick", "Ourour", "Dara Mboss",
                "Panal Ouolof", "Mboss"))
            addCommune(guinguineo, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 12. LOUGA
    // ═════════════════════════════════════════════════════════════════════════
    private void seedLouga(Pays sn) {
        Region r = findOrCreateRegion(sn, "LG", "Louga");

        Departement kebemer = findOrCreateDept(r, "LG-KEB", "Kébémer");
        for (String c : List.of("Kebemer", "Darou Marnane", "Darou Mouhty", "Mbadiane",
                "Ndoyene", "Sam Yabal", "Touba Merina", "Mbacke Cajor", "Badegne Ouolof",
                "Diokoul Diawrigne", "Kab Gaye", "Ndande", "Thieppe", "Gueoul",
                "Kanene Ndiob", "Loro", "Sagatta Gueth", "Thiolom Fall", "Ngourane Ouolof"))
            addCommune(kebemer, c);

        Departement linguere = findOrCreateDept(r, "LG-LIN", "Linguère");
        for (String c : List.of("Linguere", "Dahra", "Barkedji", "Gassane", "Thiarny",
                "Thiel", "Dodji", "Labgar", "Ouarkhokh", "Kamb", "Mboula",
                "Tessekere Forage", "Yang Yang", "Mbeuleukhe", "Boulal", "Dealy",
                "Sagatta Djolof", "Thiamene Passe", "Affe Djolof"))
            addCommune(linguere, c);

        Departement louga = findOrCreateDept(r, "LG-LOU", "Louga");
        for (String c : List.of("Louga", "Coki", "Pete Ouarack", "Thiamene", "Guet Ardo",
                "Ndiagne", "Keur Momar Sarr", "Nguer Malal", "Syer", "Kele Gueye",
                "Mbediene", "Nguidile", "Nomre", "Leona", "Ngueune Sarr", "Sakal"))
            addCommune(louga, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 13. TAMBACOUNDA
    // ═════════════════════════════════════════════════════════════════════════
    private void seedTambacounda(Pays sn) {
        Region r = findOrCreateRegion(sn, "TB", "Tambacounda");

        Departement bakel = findOrCreateDept(r, "TB-BAK", "Bakel");
        for (String c : List.of("Bakel", "Gathiary", "Madina Foulbe", "Sadatou", "Toumboura",
                "Bele", "Sinthiou Fissa", "Kidira", "Ballou", "Gabou", "Moudery", "Diawara"))
            addCommune(bakel, c);

        Departement tambacounda = findOrCreateDept(r, "TB-TAM", "Tambacounda");
        for (String c : List.of("Tambacounda", "Koussanar", "Sinthiou Maleme",
                "Makacolibantang", "Ndoga Babacar", "Niani Toucouleur",
                "Dialacoto", "Missirah", "Netteboulou"))
            addCommune(tambacounda, c);

        Departement goudiry = findOrCreateDept(r, "TB-GOU", "Goudiry");
        for (String c : List.of("Goudiry", "Bala", "Goumbayel", "Koar", "Dougue", "Koussan",
                "Sinthiou Mamadou Boubou", "Boynguel Bamba", "Bani Israel",
                "Boutoucoufara", "Dianke Makha", "Komoti", "Koulor",
                "Sinthiou Bocar Aly", "Kothiary"))
            addCommune(goudiry, c);

        Departement koumpentoum = findOrCreateDept(r, "TB-KPT", "Koumpentoum");
        for (String c : List.of("Koumpentoum", "Bamba Thialene", "Kahene", "Mereto", "Ndame",
                "Kouthia Guaydi", "Kouthiaba Wolof", "Pass Koto", "Payar", "Malem Niani"))
            addCommune(koumpentoum, c);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // 14. THIÈS
    // ═════════════════════════════════════════════════════════════════════════
    private void seedThies(Pays sn) {
        Region r = findOrCreateRegion(sn, "TH", "Thiès");

        Departement mbour = findOrCreateDept(r, "TH-MBO", "Mbour");
        for (String c : List.of("Joal Fadhiouth", "Mbour", "Fissel", "Ndiaganiao", "Ngueniene",
                "Sandiara", "Sessene", "Thiadiaye", "Malicounda", "Diass", "Sindia",
                "Nguekhokh", "Ngaparou", "Popenguine", "Saly Portudal", "Somone"))
            addCommune(mbour, c);

        Departement thies = findOrCreateDept(r, "TH-THI", "Thiès");
        for (String c : List.of("Khombole", "Pout", "Thiès Nord", "Thiès Est", "Thiès Ouest",
                "Notto", "Tassete", "Ndieyene Sirakh", "Ngoudiane", "Thienaba",
                "Touba Toul", "Diender", "Fandene", "Keur Moussa", "Kayar"))
            addCommune(thies, c);

        Departement tivaouane = findOrCreateDept(r, "TH-TIV", "Tivaouane");
        for (String c : List.of("Mekhe", "Tivaouane", "Meouane", "Taiba Ndiaye",
                "Darou Khoudoss", "Mboro", "Merina Dakhar", "Koul", "Pekesse",
                "Mbayene", "Ngandiouf", "Niakhene", "Thilmakha", "Cherif Lo",
                "Mont Rolland", "Notto Gouye Diama", "Pire Goureye", "Pambal"))
            addCommune(tivaouane, c);
    }
}

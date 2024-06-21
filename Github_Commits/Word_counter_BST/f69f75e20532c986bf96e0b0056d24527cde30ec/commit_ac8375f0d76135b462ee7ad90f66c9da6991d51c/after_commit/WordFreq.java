import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
public class WordFreq implements Comparator<WordFreq>{
    private int frequency;
    private String word, root, postfix, type[];
    private static final MyLinkedList <String> verbPostfix = new MyLinkedList<>("ζομαστε", "ζοταν", "ζεσαι", "ζεται", "ζεται", "ζομαι", "νομαστε", "ομαστε", "ομασταν", "ομασταν", "οσασταν", "ομουνα", "οσουνα", "ουσαμε", "ουσατε", "ουσαν",
                                                                                    "ηθηκαμε", "ωμασταν", "θηκαμε","νησα", "νησε", "νησαμε", "νησατε", "νησαν", "νουμε", "νετε", "νουν", "ασαμε", "ασατε", "ασουμε", "νουσαμε", "ναμε", "νας", "νησει",
                                                                                    "ησουμε", "ησετε" , "ησουν", "ηθηκαν", "ηθηκε", "ησει", "ησετε", "ησουν", "αγαμε", "σαστε", "ησεις", "ησει",
                                                                                    "ξουμε", "χνω", "χνεται", "νουν", "ουσα", "ουσε", "ουσες", "οτανε", "ουνταν","μασταν", "ομουν", "οσουν", "τομαι", "νομαι", "ομαι",
                                                                                    "εθηκες", "εθηκε", "εθηκαμε", "εθηκατε", "εθηκαν", "νθεις","νθω", "νομουν", "ζονται", "ονται",  "ωμαστε", "ωσαστε","μαστε", "σαστε", "ζεσαι",
                                                                                    "στηκε", "στει", "στηκα", "στηκη", "ισεις", "σισουν", "ασουμε", "αζουμε", "αζετε", "αζουν","ηθουμε", "θουμε", "ιουμαι", "ηκαμε",
                                                                                    "ιοαταν", "ιαιμαι", "ιεται", "ιετε", "εθηκα", "εσαι", "εται","οντε", "εσε", "χνω", "χνεις", "χνει", "ηθηκα",
                                                                                    "χνουμε", "χναμε", "χνατε", "χνετε", "χνεται", "χνουν", "χναν", "στει", "στηκε", "νοταν", "οταν",
                                                                                    "χτω", "χτηκα", "χτεις", "χτουμε", "χτειτε", "χτουν", "τομουν", "τει", "ηθει", "ηθεις",
                                                                                    "ψει", "ιζετε", "σετε", "αξουνε", "αχνω","αξεις", "αξει", "αξουμε", "αξετε", "αξουν", "αγμενα", "αχνω", "θεις", "κα", "κες", "κε", "ειται",
                                                                                    "ιεμαι", "ουμαι", "εμαι", "μαι", "σαι", "ναι", "νανε", "αζαν", "τσε", "ευω", "ευει", "θηκε", "αινω", "αινεις", "αινει", "αινουμε", "αινετε", "αινουν", "σουν",
                                                                                    "ζουνε", "ηκε", "ηκα", "αξα", "ζων", "ζας", "ζης", "χτει", "ναει", "εσε", "χνω", "ειτε", "ετε", "νεται",
                                                                                    "ασα", "αζεται", "ασω", "ασεις", "ασει", "ζουμε", "σουμε", "ζουν", "ουν", "ασετε", "ασουν", "ηνω", "ηνεις", "ηνει", "ηνουμε", "ηνετε", "ηνουν", "αγε", "αγες", "αγε",
                                                                                    "αγαμε", "αγατε", "αγαν", "αζω", "αζεις", "αζει",  "αξω", "αζα", "αζες", "αζε", "αζαμε", "αζατε", "αζαν", "ηθω", "ωμασταν",
                                                                                    "ησες", "ησαμε","ησατε","ησαν", "σαμε", "σατε", "σατε", "ησω", "ησα", "ησει", "ησε", "αγα", "ασε", "εψα", "αμε", "αω", "αει", "ησει",
                                                                                    "στω", "ανε", "θουν","ουμε", "αεις", "αξε", "ξεις", "ζει", "ουνε", "θει", "είς", "ατε", "γα", "με", "γε", "εσει","ησε", "σαν", "ξα", "ξω",
                                                                                    "ξουνε", "ξεις", "ξει", "ξουμε", "ξετε", "ξουν", "σω", "υω", "σω", "ζεις", "νεις", "νει", "ναμε", "νατε", "ναν",
                                                                                    "σει", "σεο", "σες", "σα", "σε", "αν", "ας", "αι", "ές", "θω", "εις", "ει", "νε", "ον",  "ζω", "ζα", "φα", "να", "σω", "νω", "με", "τω", "φω", "ψω", "ψα", "φε", "ψε", "ω", "ε");
    private static final MyLinkedList <String> nounPostfix = new MyLinkedList<>("σμενος", "σμενη", "σμενο", "σμενοι", "σμενα", "ημενος", "ημενη", "ημενο", "ημενοι", "ημενα", "ημενες", "μενος", "μενο", "μενα", "μενοι", "μενη", "μενες", "οτητα",
                                                                                    "οντας", "τσες", "οτα", "στης", "ουλα", "ατα", "ατη", "ατο", "ατου", "ατος", "ατες", "ατων", "κων", "ακι", "ωση", "τσα", "ακια", "τσικα","ηση", "σος", "σεων", "σεις", "ους", "κος", "κους",
                                                                                    "ην", "κο", "αση", "ση", "ου", "κης", "τι", "τια", "κη", "οι", "ης", "ων", "ος", "ες", "s", "η", "ς", "ν", "ο", "α");
    private static final MyLinkedList <String> adverbPostfix = new MyLinkedList<>("ως", "αια", "αιος", "ιαια");
    private static final MyLinkedList<String> prepositions = new MyLinkedList<>("αντί", "από", "για", "δίχως", "εναντίον", "εξαιτίας", "έως", "ίσαμε", "κατά", "με", "μετά", "μεταξύ", "μέχρι", "παρά", "πριν", "προς", "σαν",
                                                                                        "σε", "χωρίς", "ανά", "άνευ", "διά", "εις", "εκ", "εκτός", "εν", "ένεκα", "εντός", "επί", "κατόπιν", "λόγω", "μείον", "μέσω", "περί", "πλην", "προ", "συν", "υπέρ", "υπό", "χάριν");

    public static MyLinkedList<String> rootWords = new MyLinkedList<>();


    public WordFreq(){}

    public WordFreq(String word) {
        this.word = word;
        frequency = 1;
        findRootAndPostfix(this);
    }
    public WordFreq(WordFreq wf) {
        this.word = wf.word;
        this.frequency = wf.frequency;
        findRootAndPostfix(wf);
    }

    /** The word is normalized before compared so that we don't need to include diacritics in our Postfix Sets.
     *  As a result all roots and postfixes are without diacritics */
    public static void findRootAndPostfix(WordFreq wf) {
        String NormalizedWord = Normalizer.normalize(wf.word, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //removes diacritics

        for (String prep : prepositions) {
            if (NormalizedWord.equals(prep)) {
                wf.type = new String[]{"preposition"};
                return ;
            }
        }

        for (String p : verbPostfix) {
            if (NormalizedWord.endsWith(p) && (NormalizedWord.length() - p.length()) >= 1) { // second condition: is compound word (sintheti leksi)
                String stringRoot = NormalizedWord.substring(0, NormalizedWord.length() - p.length());
                if (stringRoot.length() >= 2) {
                    wf.root = stringRoot;
                    wf.postfix = p;
                    if (NormalizedWord.endsWith("εις"))
                        wf.type = "verb or noun or adverb".split(" or ");
                    else if (NormalizedWord.endsWith("κα") || (NormalizedWord.endsWith("ε") && p.length() == 1))
                        wf.type = "verb or noun".split(" or ");
                    else if (NormalizedWord.endsWith("σα") && NormalizedWord.length() < 4)
                        wf.type = "noun or adjective".split(" or ");
                    else if (NormalizedWord.endsWith("κες") || NormalizedWord.endsWith("ας")){
                        wf.type = "verb or noun or adjective".split(" or ");
                    } else
                        wf.type = new String[]{"verb"};
                    break;
                }
            }
        }

        for (String p : nounPostfix) {
            if (NormalizedWord.endsWith(p) && (NormalizedWord.length() - p.length()) >= 1) { // second condition: is compound word (sintheti leksi)
                String stringRoot = NormalizedWord.substring(0, NormalizedWord.length() - p.length());
                if (stringRoot.length() >= 2) {
                    if (wf.postfix == null || p.length() > wf.postfix.length()) {
                        wf.root = stringRoot;
                        wf.postfix = p;
                        if (NormalizedWord.endsWith("σεις"))
                            wf.type = "verb or noun or adjective".split(" or ");
                        else
                            wf.type = "noun or adjective".split(" or ");
                        break;
                    }
                }
            }
        }
    }

    public String key() {return word;}

    /** compares characters of a string ignoring diacritics and case */
    public int compareToIgnoreCaseWithoutDiacritics(WordFreq wf) {
        String normalizedWord1 = Normalizer.normalize(this.word, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String normalizedWord2 = Normalizer.normalize(wf.word, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalizedWord1.compareToIgnoreCase(normalizedWord2);
    }

    /** Compares frequency */
    @Override
    public int compare(WordFreq o1, WordFreq o2) {
        return Integer.compare(o1.frequency, o2.frequency);
    }

    public boolean containsType(String[] typeArr){
        if (this.type == null || typeArr == null)
            return false;
        else {
            for (String thisType : type)
                for (String t : typeArr)
                    if (thisType.equals(t))
                        return true;
        }
        return false;
    }

    public static String forToString(String s, int chars){
        int i = 0; StringBuilder result = new StringBuilder();
        if (s == null)
            s = "    ";
        while (++i <= chars - s.length())
            result.append(" ");
        return result.toString();
    }


    public String toString() {
        return word + "  " + forToString(word,19) + root + " + " + postfix + forToString(root + postfix,20) + " type: " + Arrays.toString(type) + forToString(Arrays.toString(type),25) + "Repetitions: " + frequency ;
    }

    public void increaseFrequency(){ ++frequency; }
    public String getWord() { return word; }
    public String getRoot() { return root; }
    public int getFrequency() { return frequency; }
    public String getPostfix() { return postfix; }
    public String[] getType() { return type; }

}


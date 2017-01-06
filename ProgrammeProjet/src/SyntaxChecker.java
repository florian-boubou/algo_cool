/**
 * Created by Terawa on 2016/12/28.
 */
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import java.util.ArrayList;
import java.util.HashMap;

public class SyntaxChecker {
    private String[] header;
    private ArrayList<String> data;
    private ArrayList<String> body;
    private HashMapConfig reserved;
    private HashMap<String, String> hConstant;
    private HashMap<String, String> hVariable;

    public SyntaxChecker(ArrayList<String> algo) throws SyntaxException{
        int delimiter = -1;
        data = new ArrayList<String>();
        body = new ArrayList<String>();
        reserved = new HashMapConfig();

        //Remplissage de l'en-tête avec la première ligne du document
        this.header = algo.get(0).split(" ");
        //Recherche de la balise "DEBUT" pour délimiter la partie données du corps de l'arlgorithme
        for(int i = 1; i < algo.size(); i++) {
            if(algo.get(i).contains("DEBUT")) {
                delimiter = i;
                break;
            }
        }
        //Si la balise "DEBUT" n'a pas été trouvée, on lance une exception
        if(delimiter == -1) {
            throw new SyntaxException("L'algorithme doit contenir une balise \"DEBUT\"");
        }
        //Remplissage de la partie données contenue entre l'en-tête et le corps de l'algorithme
        for(int i = 0; i < delimiter; i++) {
            this.data.add(algo.get(i));
        }
        //Remplissage du corps de l'algorithme contenu entre la balise "DEBUT" et la fin du document
        for(int i = delimiter; i < algo.size(); i++) {
            this.body.add(algo.get(i));
        }
    }
    //Prendre en charge les paramètres /!\
    public boolean headerCheck() {
        String tag, name;

        tag = header[0];
        name = header[1];

        //Retourne si le 1er mot de la ligne est "ALGORITHME" et si le 2ème respecte la syntaxe suivante :
        //  commence par une majuscule suivie d'un nombre variable de caractères majuscules, minucules ou de chiffres
        //on vérifie également que le nom attribué à l'algorithme n'est pas déjà réservé par l'interpréteur

        return tag.equals("ALGORITHME") && name.matches("^[A-Z][0-9A-Za-z]*") && !reserved.getHashMapConfig().get("tag").contains(name);
    }

    public boolean dataCheck() {
        //Indice de la balise "constante:"
        int constant = -1;
        //Indice de la balise "variable:"
        int variable = -1;
        String testLine = null;
        String testName = null;
        String testType = null;
        //HashMaps contenant les constantes et les variables
        hConstant = new HashMap<String, String>();
        hVariable = new HashMap<String, String>();

        //On cherche les balises "constante:" et "variable:"
        for (int i = 0; i < data.size(); i++) {
            //On enlève les espaces et les tabulations
            testLine = data.get(i).replaceAll("\\t| ", "");
            //Si on trouve une seule balise variable, variable prend la valeur de l'indice i
            //Sinon on retourne false
            if (testLine.equals("variable:")) {
                if (variable == -1) {
                    variable = i;
                } else {
                    return false;
                }
            }
            //Si on trouve une seule balise constante, constante prend la valeur de l'indice i
            //Sinon on retourne false
            if (testLine.equals("constante:")) {
                if (constant == -1) {
                    constant = i;
                } else {
                    return false;
                }
            }
        }

        //Si il y a une balise "constante:" dans la partie données de l'algorithme
        if (constant != -1) {
            for (int i = constant + 1; i < (variable == -1 ? data.size() : variable); i++) {
                //On vérifie si la ligne contient le symbole d'affectation ◄— et qu'il n'y en a qu'un seul
                if ((testLine = data.get(i)).contains("◄—") && testLine.split("◄—").length == 2) {
                    //On supprime les espaces en queue et en tête
                    testName = testLine.split("◄—")[0].trim();

                    //Le nom donné à la constante doit être sous la forme :
                    //  commence par une majuscule suivi par un nombre indéterminé de chiffres, de majuscules
                    //  ou d'un underscore suivi de chiffres ou de majuscules.
                    //On vérifie également que le nom donné à la constante ne fait pas partie des noms réservés
                    if (!testName.matches("^[A-Z][0-9A-Z]*(_[0-9A-Z]*)*$") || reserved.getHashMapConfig().get("tag").contains(testName)) {
                        return false;
                    }
                    else {
                        //Si la valeur attribué à la constante de respecte pas la syntaxe suivante, on retourne false:
                        //  Texte compris entre guillemets
                        //  Suite de chiffres suivis ou non d'une virgule et d'une suite de chiffres
                        //  Caractère compris entre simples guillemets
                        if(!testLine.split("◄—")[1].trim().matches("^\"([^\"]|\\\\\")*\"$|[0-9]*(,[0-9]*)?$|^'[^']'$")) { return false ;}
                        //On vérifie que la constante n'a pas déjà été déclarée
                        for (String k : hConstant.keySet()) {
                            if (k.equals(testName)) {
                                return false;
                            }
                        }

                        //On place dans la HasMap de constantes la constante vérifiée en clé et sa valeur attribuée en valeur
                        hConstant.put(testName, testLine.split("◄—")[1].trim());
                    }
                }
            }
            //Si la balise "constante:" existe mais qu'aucune constante n'est déclarée, on retourne false
            if (hConstant.isEmpty()) {
                return false;
            }
        }

        //Si il y a une balise "variable:" dans la partie données de l'algorithme
        if (variable != -1) {
            //Si la balise "variable:" se trouve avant la balise "constante:", on retourne false
            if (!(variable > constant)) {
                return false;
            }

            for (int i = variable + 1; i < data.size(); i++) {
                //Si la ligne contient le symbole ":" et qu'il ny en a qu'un seul
                if (!(testLine = data.get(i)).contains(":") && !(testLine.split(":").length == 2)) {
                    return false;
                }
                //On cherche à obtenir le type de la variable déclarée
                testType = testLine.split(":")[1].trim();
                //Si le type ne la variable déclarée ne correspond pas aux types définis pour l'interpréteur, on renvoie false
                if (!reserved.getHashMapConfig().get("type").contains(testType)) {
                    return false;
                }
                //On cherche à obtenir le nom de la variable déclarée
                testName = testLine.split(":")[0].trim();
                //On veut savoir si plusieurs variables on été déclarées sur une ligne. Pour cela, on cherche le caractère ","
                if (testName.contains(",")) {
                    for (int j = 0; j < testName.split(",").length; j++) {
                        //On vérifie la syntaxe de chaque nom de variable trouvé
                        if (!testName.split(",")[j].trim().matches("^[a-z][0-9A-Za-z]*((-|_)[0-9A-Za-z]*)*$")) {
                            return false;
                        }
                        //On vérifie si le nom de la variable n'est pas déjà réservé par l'interpréteur
                        for (String k : reserved.getHashMapConfig().keySet()) {
                            if (reserved.getHashMapConfig().get(k).contains(testName.split(",")[j].trim())) {
                                return false;
                            }
                        }
                        //On vérifie que la variable n'a pas déjà été déclarée
                        for (String k : hVariable.keySet()) {
                            if (k.equals(testName.split(",")[j].trim())) {
                                return false;
                            }
                        }
                        hVariable.put(testName.split(",")[j].trim(), testType);
                    }
                } else {
                    if (!testName.matches("^[a-z][0-9A-Za-z]*((-|_)[0-9a-zA-Z]*)*$")) {
                        return false;
                    }
                    hVariable.put(testName, testType);
                }
            }
            if (hVariable.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public boolean bodyCheck() {
        boolean found = false;
        if (!body.get(body.size() - 1).equals("FIN")) {
            return false;
        }
        for (String s : body) {
            if (s.contains("◄—")) {
                for (String keyC : hConstant.keySet()) {
                    if(s.split("◄—")[0].trim().equals(keyC)) {
                        found = true;
                        break;
                    }
                }
                for (String keyV : hVariable.keySet()) {
                    if(s.split("◄—")[0].trim().equals(keyV)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }
}

package common;

import java.util.ArrayList;

public class Configuration {
//    public static String OBSERVATION = "";
//    public static String INPUT_ONT_FILE = "";
//    public static String INPUT_FILE_NAME = "";
//    public static ReasonerType REASONER;
//    public static Integer DEPTH;
//    public static Long TIMEOUT;
//    public static boolean NEGATION_ALLOWED = true;
//    public static boolean LOOPING_ALLOWED = true;
//    public static boolean MHS_MODE = false;
    //default true, ale zatial iba false
//    public static boolean ROLES_IN_EXPLANATIONS_ALLOWED = false;
    public static ArrayList<String> ABDUCIBLES_CONCEPTS = new ArrayList<>();
    public static ArrayList<String> ABDUCIBLES_INDIVIDUALS = new ArrayList<>();
    public static ArrayList<String> ABDUCIBLES_ROLES = new ArrayList<>();
    public static ArrayList<String> PREFIXES = new ArrayList<>();

    //constants set before run program
    public static boolean REUSE_OF_MODELS = true;
    public static boolean GET_MODELS_BY_REASONER = false;
    public static boolean CHECKING_MINIMALITY_BY_QXP = false;
    public static boolean CACHED_CONFLICTS_LONGEST_CONFLICT = false;
    public static boolean CACHED_CONFLICTS_TABLE_OF_OCCURRENCE = false;
    public static String version = "_v6";
}

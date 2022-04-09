package parser;

import abduction.api.implementation.AbducibleContainerImpl;
import abduction.api.implementation.AbductionManagerImpl;
import application.Application;
import application.ExitCode;
import common.Configuration;
import common.DLSyntax;
import reasoner.ReasonerType;

import java.io.*;
import java.util.ArrayList;


public class ArgumentParser {

    public void parse(String[] args, AbducibleContainerImpl abducibleContainer, AbductionManagerImpl abductionManager) {

        if (args.length != 1){
            System.err.println("Wrong number of argument for main function: Run program with one configuration input file as argument");
            Application.finish(ExitCode.ERROR);
        }
        abductionManager.INPUT_FILE_NAME = new File(args[0]).getName().split("\\.")[0];
        ArrayList<String[]> lines = read_input_file(args[0]);

        boolean read_concepts = false;
        boolean read_individuals = false;
        boolean read_prefixes= false;
        boolean read_roles = false;

        for (String[] line: lines){
            String new_line = line[0].trim();
            if (read_concepts || read_individuals || read_prefixes || read_roles){
                if (new_line.equals("}")){
                    read_prefixes = false;
                    read_concepts = false;
                    read_individuals = false;
                    read_roles = false;
                } else if (read_concepts) {
                    add_abd(new_line, true, false);
                } else if (read_individuals) {
                    add_abd(new_line, false, false);
                } else if (read_roles) {
                    add_abd(new_line, false, true);
                } else{
                    String last = (line.length == 2) ? line[1] : "";
                    add_prefix(new_line + " " + last);
                }
                continue;
            }
            String next = line[1];
            switch(new_line) {
                case "-f:":
                    if (!(new File(next).exists())){
                        System.err.println("Could not open -f file " + next);
                        Application.finish(ExitCode.ERROR);
                    }
                    abductionManager.setAdditionalSolverSettings("INPUT_ONT_FILE:"+next);
                    break;
                case "-o:":
                    String observation = String.join(" ", line).replace("-o: ", "");
                    abductionManager.setAdditionalSolverSettings("OBSERVATION:"+observation);
                    break;
                case "-r:":
                    try {
                        abductionManager.setAdditionalSolverSettings("REASONER:"+next.toUpperCase());
                    }
                    catch (IllegalArgumentException e){
                        System.err.println("Reasoner type -r " + next + " is unknown, the only allowed reasoners are hermit|pellet|jfact");
                        Application.finish(ExitCode.ERROR);
                    }
                    break;
                case "-d:":
                    try {
                        abductionManager.setAdditionalSolverSettings("DEPTH:"+next);
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Wrong tree depth -d " + next + ", choose a whole number value");
                        Application.finish(ExitCode.ERROR);
                    }
                    break;
                case "-t:":
                    try {
                        abductionManager.setAdditionalSolverSettings("TIMEOUT:"+next);
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Wrong timeout value -t " + next + ", choose a whole number value");
                        Application.finish(ExitCode.ERROR);
                    }
                    break;
                case "-aI:":
                    if (next.equals("{")){
                        read_individuals = true;
                    } else {
                        add_abd(next, false, false);
                    }
                    break;
                case "-aC:":
                    if (next.equals("{")){
                        read_concepts = true;
                    } else {
                        add_abd(next, true, false);
                    }
                    break;
                case "-aR:":
                    if (next.equals("{")){
                        read_roles = true;
                    } else {
                        add_abd(next, false, true);
                    }
                    break;
                case "-mhs:":
                    if (next.equals("true")) {
                        abductionManager.setAdditionalSolverSettings("MHS_MODE:true");
                    } else if (!next.equals("false")) {
                        System.err.println("Wrong MHS mode value -mhs" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-l:":
                    if (next.equals("false")) {
                        abducibleContainer.allowLoops(false);
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong looping allowed value -l" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-eR:":
                    if (next.equals("false")) {
                        abducibleContainer.allowRoleAssertions(false);
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong roles in explanations allowed value -eR" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                case "-n:":
                    if (next.equals("false")) {
                        abducibleContainer.allowConceptComplement(false);
                    } else if (!next.equals("true")) {
                        System.err.println("Wrong negation allowed value -n" + next + ", allowed values are 'true' and 'false'");
                    }
                    break;
                default:
                    System.err.println("Unknown option " + line[0] + " in input file");
                    Application.finish(ExitCode.ERROR);
            }
        }
        if (abductionManager.INPUT_ONT_FILE.equals("") || abductionManager.OBSERVATION.equals("")){
            System.err.println("Input file -f and observation -o are both required argument");
            Application.finish(ExitCode.ERROR);
        }
        if (abductionManager.REASONER == null) {
            abductionManager.REASONER = ReasonerType.HERMIT;
        }
        if (abductionManager.DEPTH == null){
            abductionManager.DEPTH = Integer.MAX_VALUE;
        }
    }

    private void add_prefix(String prefix){
        if (!prefix.matches("[a-zA-Z0-9]+: " + DLSyntax.IRI_REGEX)){
            System.err.println("Prefix '" + prefix + "' does not match the form 'prefix_shortcut: prefix'");
            Application.finish(ExitCode.ERROR);
        }
        Configuration.PREFIXES.add(prefix);
    }

    private void add_abd(String abd, boolean isConcept, boolean isRole){
        if (isConcept)
            Configuration.ABDUCIBLES_CONCEPTS.add(abd);
        else if (isRole)
            Configuration.ABDUCIBLES_ROLES.add(abd);
        else
            Configuration.ABDUCIBLES_INDIVIDUALS.add(abd);
    }

    private ArrayList<String[]> read_input_file(String input_file_path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(input_file_path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String strLine;
        ArrayList<String[]> lines = new ArrayList<>();
        try {
            while ((strLine = reader.readLine()) != null) {
                if (strLine.equals("")){
                    continue;
                }
                String[] words = strLine.split("\\s+");
                lines.add(words);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

}

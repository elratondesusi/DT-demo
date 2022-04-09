package abduction.api.implementation;

import abductionapi.exception.AxiomAbducibleAssertionException;
import abductionapi.exception.AxiomAbducibleSymbolException;
import abductionapi.container.AbducibleContainer;
import abductionapi.exception.CommonException;
import org.apache.commons.lang3.NotImplementedException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import parser.ArgumentParser;
import reasoner.ILoader;
import reasoner.Loader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbducibleContainerImpl implements AbducibleContainer {

    private boolean loops = true;
    //default true, ale zatial iba false
    private boolean roleAssertions = false;
    private boolean conceptComplements = true;

    private Set<OWLClass> abduciblesConcepts = new HashSet<>();
    private Set<OWLNamedIndividual> abduciblesIndividuals = new HashSet<>();
    private Set<OWLObjectProperty> abduciblesRoles = new HashSet<>();

    private Set<OWLAxiom> assertions = new HashSet<>();

    private ILoader loader;

    public AbducibleContainerImpl(AbductionManagerImpl abductionManager) throws Exception {
        String[] x = new String[1];
//        x[0] = "/home/iveta/Plocha/skola/diplomovka/testingFilesDummyClass/testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3_noNeg.in";
//        x[0] = "/home/iveta/Plocha/skola/diplomovka/MHS-MXP-algorithm/in/input_fam_abd.txt";

        // 1. examples - asi su ok - treba este debuggnut lebo idu trochu aj bez api
        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3.in";
        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3_noNeg.in";
//        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/in/input_fam_abd.txt";
//        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/in/mhs_mod/family.in";
////        C:\Users\zuz\Documents\UNI\Praca\DEMO\DT-demo\DT-demo\in\mhs_mod\family.in
//        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/in/input_fam.txt";
        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/in/input_fam_2.txt";
        x[0] = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/DT-demo/in/divideSets.in";


        // 2. example

        ArgumentParser argumentParser = new ArgumentParser();
        //argumentParser.parse(args);
        argumentParser.parse(x, this, abductionManager);
        loader = new Loader();
        loader.initialize(abductionManager);
    }

    public ILoader getLoader() {
        return loader;
    }

    @Override
    public void allowLoops() {
        loops = true;
    }

    @Override
    public void allowLoops(Boolean allow) {
        loops = allow;
    }

    @Override
    public void allowRoleAssertions() {
        roleAssertions = true;
    }

    @Override
    public void allowRoleAssertions(Boolean allow) {
        roleAssertions = allow;
    }

    @Override
    public void allowConceptComplements() {
        conceptComplements = true;
    }

    @Override
    public void allowConceptComplements(Boolean allow) {
        conceptComplements = allow;
    }

    @Override
    public void addSymbol(Object o) throws AxiomAbducibleSymbolException {
        if (o instanceof OWLClass) {
            if (abduciblesConcepts == null) {
                abduciblesConcepts =  new HashSet<>();
            }
//            abduciblesConcepts.add((OWLClass)o)
        } else if (o instanceof OWLNamedIndividual) {
            if (abduciblesIndividuals == null) {
                abduciblesIndividuals =  new HashSet<>();
            }
//            abduciblesIndividuals
        } else if (o instanceof OWLObjectProperty) {
            if (abduciblesRoles == null) {
                abduciblesRoles =  new HashSet<>();
            }
//            abduciblesRoles
        }

    }

    @Override
    public void addSymbols(Set set) throws AxiomAbducibleSymbolException {
        set.stream().forEach(e -> {
            if (e instanceof OWLClass) {
                abduciblesConcepts.add((OWLClass) e);
            } else if (e instanceof OWLNamedIndividual) {
                abduciblesIndividuals.add((OWLNamedIndividual) e);
            } else if (e instanceof OWLObjectProperty) {
                abduciblesRoles.add((OWLObjectProperty) e);
            }
        });

        System.out.println(set);
    }

    @Override
    public void addSymbols(List list) throws AxiomAbducibleSymbolException {
        throw new CommonException("This method should not be used.", new NotImplementedException("This method should not be used."));
    }

    @Override
    public void addSymbols(Object o) throws AxiomAbducibleSymbolException {
        throw new CommonException("This method should not be used.", new NotImplementedException("This method should not be used."));
    }

    @Override
    public void addAssertion(Object o) throws AxiomAbducibleAssertionException {
        throw new CommonException("This method should not be used.", new NotImplementedException("This method should not be used."));
    }

    @Override
    public void addAssertions(Set set) throws AxiomAbducibleAssertionException {
        set.stream().forEach(e -> {
            assertions.add((OWLAxiom) e);
//            if (e instanceof OWLAxiom) {
//                assertions.add((OWLAxiom) e);
//            } else {
//                throw new AxiomAbducibleAssertionException();
//            }
        });
    }

    @Override
    public void addAssertions(List list) throws AxiomAbducibleAssertionException {
        throw new CommonException("This method should not be used.", new NotImplementedException("This method should not be used."));
    }

    @Override
    public void addAssertions(Object o) throws AxiomAbducibleAssertionException {
        throw new CommonException("This method should not be used.", new NotImplementedException("This method should not be used."));
    }

    @Override
    public boolean areLoopsEnabled() {
        return loops;
    }

    @Override
    public boolean areRoleAssertionsEnabled() {
        return roleAssertions;
    }

    @Override
    public boolean areConceptComplementsEnabled() {
        return conceptComplements;
    }

    public Set<OWLClass> getAbduciblesConcepts() {
        return abduciblesConcepts;
    }

    public Set<OWLNamedIndividual> getAbduciblesIndividuals() {
        return abduciblesIndividuals;
    }

    public Set<OWLObjectProperty> getAbduciblesRoles() {
        return abduciblesRoles;
    }

    public Set<OWLAxiom> getAssertions() {
        return assertions;
    }
}

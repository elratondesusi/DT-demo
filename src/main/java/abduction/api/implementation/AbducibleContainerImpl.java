package abduction.api.implementation;

import abductionapi.exception.AxiomAbducibleAssertionException;
import abductionapi.exception.AxiomAbducibleSymbolException;
import abductionapi.container.AbducibleContainer;
import common.Configuration;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import parser.ArgumentParser;
import reasoner.ILoader;
import reasoner.Loader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbducibleContainerImpl implements AbducibleContainer {

    private boolean loops = false;
    private boolean roleAssertions = false;
    private boolean conceptAssertions = false;
    private boolean complexConcepts = false;
    private boolean conceptComplement = false;

    private Set<OWLClass> abduciblesConcepts = new HashSet<>();
    private Set<OWLNamedIndividual> abduciblesIndividuals = new HashSet<>();
    private Set<OWLObjectProperty> abduciblesRoles = new HashSet<>();

    private Set<OWLAxiom> assertions = new HashSet<>();

    private ILoader loader;

    public AbducibleContainerImpl() throws Exception {
        String[] x = new String[1];
//        x[0] = "/home/iveta/Plocha/skola/diplomovka/testingFilesDummyClass/testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3_noNeg.in";
//        x[0] = "/home/iveta/Plocha/skola/diplomovka/MHS-MXP-algorithm/in/input_fam_abd.txt";
        x[0] = "C:/Users/zuz/Documents/UNI/Praca/Mhs-MXP-Algo/testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3_noNeg.in";
        x[0] = "C:/Users/zuz/Documents/UNI/Praca/Mhs-MXP-Algo/in/input_fam_abd.txt";

        ArgumentParser argumentParser = new ArgumentParser();
        //argumentParser.parse(args);
        argumentParser.parse(x);
        loader = new Loader();
        loader.initialize(Configuration.REASONER);
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
    public void allowConceptAssertions() {
        conceptAssertions = true;
    }

    @Override
    public void allowConceptAssertions(Boolean allow) {
        conceptAssertions = allow;
    }

    @Override
    public void allowComplexConcepts() {
        complexConcepts = true;
    }

    @Override
    public void allowComplexConcepts(Boolean allow) {
        complexConcepts = allow;
    }

    @Override
    public void allowConceptComplement() {
        conceptComplement = true;
    }

    @Override
    public void allowConceptComplement(Boolean allow) {
        conceptComplement = allow;
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

    }

    @Override
    public void addSymbols(Object o) throws AxiomAbducibleSymbolException {

    }

    @Override
    public void addAssertion(Object o) throws AxiomAbducibleAssertionException {

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

    }

    @Override
    public void addAssertions(Object o) throws AxiomAbducibleAssertionException {

    }

    public boolean isLoops() {
        return loops;
    }

    public boolean isRoleAssertions() {
        return roleAssertions;
    }

    public boolean isConceptAssertions() {
        return conceptAssertions;
    }

    public boolean isComplexConcepts() {
        return complexConcepts;
    }

    public boolean isConceptComplement() {
        return conceptComplement;
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

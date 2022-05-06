package abduction.api.implementation;

import abductionapi.exception.AxiomAbducibleAssertionException;
import abductionapi.exception.AxiomAbducibleException;
import abductionapi.exception.AxiomAbducibleSymbolException;
import abductionapi.container.AbducibleContainer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AbducibleContainerImpl implements AbducibleContainer<OWLEntity , OWLAxiom>{

    private boolean loops = true;
    //by default should be true, but for now just false (comment from MHS-MXP implementation)
    private boolean roleAssertions = false;
    private boolean conceptComplements = true;

    private Set<OWLClass> abduciblesConcepts = new HashSet<>();
    private Set<OWLNamedIndividual> abduciblesIndividuals = new HashSet<>();
    private Set<OWLObjectProperty> abduciblesRoles = new HashSet<>();

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
    public void addSymbol(OWLEntity o) throws AxiomAbducibleSymbolException {
        if (o instanceof OWLClass) {
            abduciblesConcepts.add((OWLClass) o);
        } else if (o instanceof OWLNamedIndividual) {
            abduciblesIndividuals.add((OWLNamedIndividual) o);
        } else if (o instanceof OWLObjectProperty) {
            abduciblesRoles.add((OWLObjectProperty) o);
        } else {
            throw new AxiomAbducibleSymbolException(o.toString());
        }
    }

    @Override
    public void addSymbols(Set set) throws AxiomAbducibleSymbolException {
        set.stream().forEach(e -> {
            addSymbol((OWLEntity) e);
        });
    }

    @Override
    public void addSymbols(List list) throws AxiomAbducibleSymbolException {
        throw new AxiomAbducibleException("adding symbols as list");
    }

    @Override
    public void addAssertion(OWLAxiom o) throws AxiomAbducibleAssertionException {
        throw new AxiomAbducibleException("assertion abducibles");
    }

    @Override
    public void addAssertions(Set set) throws AxiomAbducibleAssertionException {
        throw new AxiomAbducibleException("assertion abducibles");
    }

    @Override
    public void addAssertions(List list) throws AxiomAbducibleAssertionException {
        throw new AxiomAbducibleException("assertion abducibles.");
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
}

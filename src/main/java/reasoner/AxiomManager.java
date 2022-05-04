package reasoner;

import abduction.api.implementation.AbductionManagerImpl;
import common.DLSyntax;
import org.semanticweb.owlapi.model.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AxiomManager {

    public static List<OWLAxiom> createClassAssertionAxiom(ILoader loader, OWLAxiom axiom) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (OWLDeclarationAxiom.class.isAssignableFrom(axiom.getClass()) && OWLClass.class.isAssignableFrom(((OWLDeclarationAxiom) axiom).getEntity().getClass())) {
            OWLClass owlClass = loader.getDataFactory().getOWLClass(((OWLDeclarationAxiom) axiom).getEntity().getIRI());
            for (OWLNamedIndividual namedIndividual : loader.getAbducibles().getIndividuals()) {
                if(!loader.isMultipleObservationOnInput() || loader.getObservation().getReductionIndividual() != namedIndividual){
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
                }
            }
        }
        return owlAxioms;
    }

    public static List<OWLAxiom> createClassAssertionAxiom(ILoader loader, OWLClass owlClass) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (owlClass != null) {
            for (OWLNamedIndividual namedIndividual : loader.getAbducibles().getIndividuals()) {
                if(!loader.isMultipleObservationOnInput() || loader.getObservation().getReductionIndividual() != namedIndividual){
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, namedIndividual));
                    owlAxioms.add(loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), namedIndividual));
                }
            }
        }
        return owlAxioms;
    }

    public static List<OWLAxiom> createObjectPropertyAssertionAxiom(AbductionManagerImpl abductionManager, OWLAxiom axiom) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();
        if (OWLDeclarationAxiom.class.isAssignableFrom(axiom.getClass()) && OWLObjectProperty.class.isAssignableFrom(((OWLDeclarationAxiom) axiom).getEntity().getClass())) {
            OWLObjectProperty objectProperty = abductionManager.getAbducibles().getLoader().getDataFactory().getOWLObjectProperty(((OWLDeclarationAxiom) axiom).getEntity().getIRI());
            for (OWLNamedIndividual subject : abductionManager.getAbducibles().getAbduciblesIndividuals()) {
                if(!abductionManager.isMultipleObservationOnInput() || subject != abductionManager.getObservation().getReductionIndividual()){
                    for (OWLNamedIndividual object : abductionManager.getAbducibles().getAbduciblesIndividuals()) {
                        if (abductionManager.getAbducibles().areLoopsEnabled() || !subject.equals(object)) {
                            if(!abductionManager.isMultipleObservationOnInput() || object != abductionManager.getObservation().getReductionIndividual()){
                                owlAxioms.add(abductionManager.getAbducibles().getLoader().getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
                                owlAxioms.add(abductionManager.getAbducibles().getLoader().getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
                            }
                        }
                    }
                }
            }
        }
        return owlAxioms;
    }

    public static List<OWLAxiom> createObjectPropertyAssertionAxiom(AbductionManagerImpl abductionManager, OWLObjectProperty objectProperty) {
        List<OWLAxiom> owlAxioms = new LinkedList<>();

        if (objectProperty != null) {
            for (OWLNamedIndividual subject : abductionManager.getAbducibles().getAbduciblesIndividuals()) {
                if(!abductionManager.getAbducibles().getLoader().isMultipleObservationOnInput() || subject != abductionManager.getObservation().getReductionIndividual()){
                    for (OWLNamedIndividual object : abductionManager.getAbducibles().getAbduciblesIndividuals()) {
                        if (abductionManager.getAbducibles().areLoopsEnabled() || !subject.equals(object)) {
                            if(!abductionManager.isMultipleObservationOnInput() || object != abductionManager.getObservation().getReductionIndividual()){
                                owlAxioms.add(abductionManager.getAbducibles().getLoader().getDataFactory().getOWLObjectPropertyAssertionAxiom(objectProperty, subject, object));
                                owlAxioms.add(abductionManager.getAbducibles().getLoader().getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(objectProperty, subject, object));
                            }
                        }
                    }
                }
            }
        }
        return owlAxioms;
    }

    /**UPRAVENA FUNKCIA - vrati skutocny komplement**/
    public static OWLAxiom getComplementOfOWLAxiom(ILoader loader, OWLAxiom owlAxiom) {
        OWLAxiom complement = null;
        if(owlAxiom.getAxiomType() == AxiomType.CLASS_ASSERTION){
            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) owlAxiom).getClassExpression();
            complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClassExpression.getComplementNNF(), ((OWLClassAssertionAxiom) owlAxiom).getIndividual());
        } else if (owlAxiom.getAxiomType() == AxiomType.OBJECT_PROPERTY_ASSERTION){
            OWLObjectPropertyExpression owlObjectProperty = ((OWLObjectPropertyAssertionAxiom) owlAxiom).getProperty();
            complement = loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLObjectPropertyAssertionAxiom) owlAxiom).getObject());
        } else if (owlAxiom.getAxiomType() == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION){
            OWLObjectPropertyExpression owlObjectProperty = ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getProperty();
            complement = loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getObject());
        }
        return complement;
    }

    //stara funkcia - vracala komplement v pripade, ak v owlAxiom bola trieda, ale ak tam bol OWLObjectComplementOf, tak to vratilo to iste
    public static OWLAxiom getComplementOfOWLAxiom2(ILoader loader, OWLAxiom owlAxiom) {
        Set<OWLClass> names = owlAxiom.classesInSignature().collect(Collectors.toSet());
        String name = "";
        OWLAxiom complement = null;

        if (names.size() == 1) {
            name = names.iterator().next().getIRI().getFragment();
            OWLClass owlClass = loader.getDataFactory().getOWLClass(IRI.create(loader.getOntologyIRI().concat(DLSyntax.DELIMITER_ONTOLOGY).concat(name)));
            OWLClassExpression owlClassExpression = ((OWLClassAssertionAxiom) owlAxiom).getClassExpression();

            if (OWLObjectComplementOf.class.isAssignableFrom(owlClassExpression.getClass())) {
                complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass, ((OWLClassAssertionAxiom) owlAxiom).getIndividual());
            } else {
                complement = loader.getDataFactory().getOWLClassAssertionAxiom(owlClass.getComplementNNF(), ((OWLClassAssertionAxiom) owlAxiom).getIndividual());
            }

        } else {
            if (OWLObjectPropertyAssertionAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
                OWLObjectPropertyExpression owlObjectProperty = ((OWLObjectPropertyAssertionAxiom) owlAxiom).getProperty();
                complement = loader.getDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLObjectPropertyAssertionAxiom) owlAxiom).getObject());

            } else if (OWLNegativeObjectPropertyAssertionAxiom.class.isAssignableFrom(owlAxiom.getClass())) {
                OWLObjectPropertyExpression owlObjectProperty = ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getProperty();
                complement = loader.getDataFactory().getOWLObjectPropertyAssertionAxiom(owlObjectProperty, ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getSubject(), ((OWLNegativeObjectPropertyAssertionAxiom) owlAxiom).getObject());
            }
        }
        return complement;
    }




}

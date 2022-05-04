package algorithms.hybrid;

import abduction.api.implementation.AbductionManagerImpl;
import models.Explanation;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import reasoner.AxiomManager;

import java.util.List;

public class CheckRules implements ICheckRules {

    private AbductionManagerImpl abductionManager;

    CheckRules(AbductionManagerImpl abductionManager) {
        this.abductionManager = abductionManager;
    }

    @Override
    public boolean isConsistent(Explanation explanation) {
        abductionManager.getReasonerManager().removeAxiomFromOntology(abductionManager.getAbducibles().getLoader().getNegObservation().getOwlAxiom());
        abductionManager.getReasonerManager().addAxiomsToOntology(explanation.getOwlAxioms());
        boolean isConsistent = abductionManager.getReasonerManager().isOntologyConsistent();
        abductionManager.getReasonerManager().resetOntology(abductionManager.getBackgroundKnowledgeOriginal().axioms());
        abductionManager.getReasonerManager().addAxiomToOntology(abductionManager.getAbducibles().getLoader().getNegObservation().getOwlAxiom());
        return isConsistent;
    }

    @Override
    public boolean isExplanation(Explanation explanation) {
        abductionManager.getReasonerManager().addAxiomsToOntology(explanation.getOwlAxioms());
        boolean isConsistent = abductionManager.getReasonerManager().isOntologyConsistent();
        abductionManager.getReasonerManager().resetOntology(abductionManager.getBackgroundKnowledgeOriginal().axioms());
        return !isConsistent;
    }

    @Override
    public boolean isMinimal(List<Explanation> explanationList, Explanation explanation) {
        if (explanation == null || !(explanation.getOwlAxioms() instanceof List)) {
            return false;
        }

        for (Explanation minimalExplanation : explanationList) {
            if (explanation.getOwlAxioms().containsAll(minimalExplanation.getOwlAxioms())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRelevant(Explanation explanation) throws OWLOntologyCreationException {
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = ontologyManager.createOntology(explanation.getOwlAxioms());

        OWLReasoner reasoner = new OpenlletReasonerFactory().createNonBufferingReasoner(ontology);

        if(abductionManager.getAbducibles().getLoader().isMultipleObservationOnInput()){
            for(OWLAxiom obs : abductionManager.getAbducibles().getLoader().getObservation().getAxiomsInMultipleObservations()){
                OWLAxiom negObs = AxiomManager.getComplementOfOWLAxiom(abductionManager.getAbducibles().getLoader(), obs);
                ontologyManager.addAxiom(ontology, negObs);
                if(!reasoner.isConsistent()){
                    return false;
                }
                ontologyManager.removeAxiom(ontology, negObs);
            }
            return true;
        } else {
            ontologyManager.addAxiom(ontology, abductionManager.getAbducibles().getLoader().getNegObservation().getOwlAxiom());
            return reasoner.isConsistent();
        }
    }
}
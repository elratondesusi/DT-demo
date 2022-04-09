package algorithms;

import abduction.api.implementation.AbductionManagerImpl;
import abductionapi.Monitor;
import models.Explanation;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.util.List;


public interface ISolver {

    List<Explanation> solve(AbductionManagerImpl abductionManager) throws OWLOntologyStorageException, OWLOntologyCreationException;
}

package algorithms;

import models.Explanation;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.util.List;


public interface ISolver {

    List<Explanation> solve() throws OWLOntologyStorageException, OWLOntologyCreationException;
}

package reasoner;

import abduction.api.implementation.AbductionManagerImpl;
import application.Application;
import application.ExitCode;
import common.LogMessage;
import models.Abducibles;
import models.Individuals;
import models.Observation;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import parser.IObservationParser;
import parser.ObservationParser;
import parser.PrefixesParser;
import parser.AbduciblesParser;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Loader implements ILoader {

    private Logger logger = Logger.getLogger(Loader.class.getSimpleName());

    private OWLOntologyManager ontologyManager;
    private OWLReasonerFactory reasonerFactory;
    private OWLOntology ontology;
    private OWLReasoner reasoner;

    private Observation observation;
    private Observation negObservation;
    private String ontologyIRI;
    private Individuals namedIndividuals;
    private OWLOntology originalOntology;
    private Abducibles abducibles;

    private OWLDocumentFormat observationOntologyFormat;
    private boolean isMultipleObservationOnInput = false;

    @Override
    public void initialize(AbductionManagerImpl abductionManager) throws Exception {
        loadReasoner(abductionManager);
        loadObservation(abductionManager);
        loadPrefixes();
        loadAbductibles();
    }

    private void loadReasoner(AbductionManagerImpl abductionManager) {
        try {
            ontologyManager = OWLManager.createOWLOntologyManager();
            ontology = ontologyManager.loadOntologyFromOntologyDocument(new File(abductionManager.INPUT_ONT_FILE));
            originalOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(abductionManager.INPUT_ONT_FILE));

            changeReasoner(abductionManager.REASONER);
            initializeReasoner();

            if (reasoner.isConsistent()) {
                logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_CONSISTENCY);
            } else {
                logger.log(Level.WARNING, LogMessage.ERROR_ONTOLOGY_CONSISTENCY);
                reasoner.dispose();

                Application.finish(ExitCode.ERROR);
            }

        } catch (OWLOntologyCreationException exception) {
            logger.log(Level.WARNING, LogMessage.ERROR_CREATING_ONTOLOGY, exception);
            Application.finish(ExitCode.ERROR);
        }
    }

    @Override
    public void changeReasoner(ReasonerType reasonerType) {
        switch (reasonerType) {
            case PELLET:
                setOWLReasonerFactory(new OpenlletReasonerFactory());
                break;

            case HERMIT:
                setOWLReasonerFactory(new ReasonerFactory());
                break;

            case JFACT:
                setOWLReasonerFactory(new JFactFactory());
                break;
        }
        reasoner = reasonerFactory.createReasoner(ontology);
        logger.log(Level.INFO, LogMessage.INFO_ONTOLOGY_LOADED);
    }

    @Override
    public void initializeReasoner() {
        reasoner.flush();
    }

    private void loadObservation(AbductionManagerImpl abductionManager) throws Exception {
        namedIndividuals = new Individuals();

        IObservationParser observationParser = new ObservationParser(this);
        observationParser.parse(abductionManager);
    }

    private void loadPrefixes(){
        PrefixesParser prefixesParser = new PrefixesParser(observationOntologyFormat);
        prefixesParser.parse();
    }

    private void loadAbductibles(){
        AbduciblesParser abduciblesParser = new AbduciblesParser(this);
        abducibles = abduciblesParser.parse();
    }

    public Abducibles getAbducibles(){
        return abducibles;
    }

    @Override
    public Observation getObservation() {
        return observation;
    }

    @Override
    public void setObservation(OWLAxiom observation) {
        this.observation = new Observation(observation);
    }

    @Override
    public void setObservation(OWLAxiom observation, List<OWLAxiom> axiomsInMultipleObservations, OWLNamedIndividual reductionIndividual){
        this.observation = new Observation(observation, axiomsInMultipleObservations, reductionIndividual);
    }

    @Override
    public Observation getNegObservation() {
        return negObservation;
    }

    @Override
    public void setNegObservation(OWLAxiom negObservation) {
        this.negObservation = new Observation(negObservation);
    }

    @Override
    public OWLOntologyManager getOntologyManager() {
        return ontologyManager;
    }

    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public OWLReasoner getReasoner() {
        return reasoner;
    }

    @Override
    public void setOWLReasonerFactory(OWLReasonerFactory reasonerFactory) {
        this.reasonerFactory = reasonerFactory;
    }

    @Override
    public String getOntologyIRI() {
        if (ontologyIRI == null) {
            ontologyIRI = ontology.getOntologyID().getOntologyIRI().get().toString();
        }
        return ontologyIRI;
    }

    @Override
    public OWLDataFactory getDataFactory() {
        return ontologyManager.getOWLDataFactory();
    }

    @Override
    public Individuals getIndividuals() {
        return namedIndividuals;
    }

    @Override
    public void addNamedIndividual(OWLNamedIndividual namedIndividual) {
        namedIndividuals.addNamedIndividual(namedIndividual);
    }

    @Override
    public OWLOntology getOriginalOntology() {
        return originalOntology;
    }


    public void setObservationOntologyFormat(OWLDocumentFormat observationOntologyFormat) {
        this.observationOntologyFormat = observationOntologyFormat;
    }

    public boolean isMultipleObservationOnInput() {
        return isMultipleObservationOnInput;
    }

    public void setMultipleObservationOnInput(boolean multipleObservationOnInput) {
        isMultipleObservationOnInput = multipleObservationOnInput;
    }
}

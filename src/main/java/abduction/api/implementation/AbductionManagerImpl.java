package abduction.api.implementation;

import abductionapi.exception.AxiomObservationException;
import abductionapi.exception.CommonException;
import abductionapi.exception.MultiObservationException;
import abductionapi.manager.AbductionManager;
import algorithms.ISolver;
import algorithms.hybrid.HybridSolver;
import models.Explanation;
import models.Observation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import parser.ArgumentParser;
import reasoner.ILoader;
import reasoner.IReasonerManager;
import reasoner.Loader;
import reasoner.ReasonerManager;
import reasoner.ReasonerType;
import timer.ThreadTimes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AbductionManagerImpl implements AbductionManager<Explanation, Observation, OWLEntity, OWLAxiom, AbducibleContainerImpl> {

    private OWLOntology backgroundKnowledge;
    private OWLOntology backgroundKnowledgeOriginal;
    private Observation observation;
    private AbducibleContainerImpl abducibleContainer;
    private boolean isMultipleObservationOnInput;
    private IReasonerManager reasonerManager;

    public static boolean MHS_MODE = false;
    public static Integer DEPTH;
    public static Long TIMEOUT;
    public static String OBSERVATION = "";
    public static String INPUT_ONT_FILE = "";
    public static String INPUT_FILE_NAME = "";
    public static ReasonerType REASONER;

    private ILoader loader;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                     SOLVER METHODS                                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public ILoader getLoader() {
        return loader;
    }

    public void initializeSolverLoaderAndParser() {
        String[] x = new String[1];

        String actualPath = "C:/Users/zuz/Documents/UNI/Praca/DEMO/DT-demo/";
        // examples
//        x[0] = actualPath + "testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3.in";
//        x[0] = actualPath + "testingFiles/testingFiles0/mhs-mxp/lubm-0_2_3_noNeg.in";
//        x[0] = actualPath + "in/input_fam_abd.txt";
//        x[0] = actualPath + "in/mhs_mod/family.in";
//        x[0] = "actualPath + in/input_fam.txt";
//        x[0] = actualPath + "in/input_fam_2.txt";
        x[0] = actualPath + "in/divideSets.in";

        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parse(x, abducibleContainer, this);
        loader = new Loader();
        try {
            loader.initialize(this);
        } catch (Exception e) {
            throw new CommonException("Initialization of solver's loader gone wrong.");
        }
    }

    public IReasonerManager getReasonerManager() {
        return reasonerManager;
    }

    private void setReasonerManager() {
        this.reasonerManager = new ReasonerManager(loader);
    }

    private ISolver createSolver(ThreadTimes threadTimes) {
        long currentTimeMillis = System.currentTimeMillis();
        return new HybridSolver(threadTimes, currentTimeMillis, this);
    }

    public void setMultipleObservationOnInput(boolean multipleObservationOnInput) {
        isMultipleObservationOnInput = multipleObservationOnInput;
    }

    public boolean isMultipleObservationOnInput() {
        return isMultipleObservationOnInput;
    }

    public <T> void show(T explanations) {
        System.out.println("\n\n************************************************");
        System.out.println("* Explanations are:                            *");
        System.out.println("************************************************");
        ((Set)explanations).stream().forEach(e -> System.out.println(e));
        System.out.println("************************************************");
    }

    public void setBackgroundKnowledgeOriginal() {
        backgroundKnowledgeOriginal = loader.getOriginalOntology();
    }

    public OWLOntology getBackgroundKnowledgeOriginal() {
        return backgroundKnowledgeOriginal;
    }

    public void solve() {
        ThreadTimes threadTimes = new ThreadTimes(100);
        threadTimes.start();
        this.setReasonerManager();
        ISolver solver = createSolver(threadTimes);
        List<Explanation> expl = null;
        if (solver != null) {
            try {
                synchronized(monitor) {
                    expl = solver.solve();
                    System.out.println();
                    System.out.println("je ich:" + String.valueOf(expl.size()));
                    System.out.println();
                    show(new HashSet<Explanation>(expl));

                    sendExplanation(null);
                }
            } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                e.printStackTrace();
            }
        }
        threadTimes.interrupt();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                     OVERRIDEN METHODS                                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setBackgroundKnowledge(OWLOntology owlOntology) {
        backgroundKnowledge = owlOntology;
    }

    @Override
    public OWLOntology getBackgroundKnowledge() {
        return backgroundKnowledge;
    }

    @Override
    public Observation getObservation() {
        return observation;
    }

    @Override
    public void setObservation(Observation observation) {
        if (!isMultipleObservationOnInput && observation.getAxiomsInMultipleObservations() != null && !observation.getAxiomsInMultipleObservations().isEmpty()) {
            throw new MultiObservationException();
        }
        this.observation = observation;
    }

    @Override
    public void setObservation(Set<Observation> set) throws MultiObservationException, AxiomObservationException {
        throw new CommonException("This method should not be used.");
    }

    @Override
    public void setAbducibles(AbducibleContainerImpl abducibleContainer) {
        this.abducibleContainer = abducibleContainer;
    }

    @Override
    public AbducibleContainerImpl getAbducibles() {
        return abducibleContainer;
    }

    @Override
    public void setAdditionalSolverSettings(String s) {
        String[] tuple = s.split(":");
        if (tuple.length < 2) {
            throw new CommonException("Solver does not support this setting: " + s, null);
        }
        switch (tuple[0]) {
            case "MHS_MODE":
                MHS_MODE = tuple[1].equals("true") ? true : false;
                break;
            case "TIMEOUT":
                TIMEOUT = Long.valueOf(tuple[1]);
                break;
            case "DEPTH":
                DEPTH = Integer.valueOf(tuple[1]);
                break;
            case "REASONER":
                REASONER = ReasonerType.valueOf(tuple[1]);
                break;
            case "OBSERVATION":
                OBSERVATION = s.substring(12);
                break;
            case "INPUT_ONT_FILE":
                INPUT_ONT_FILE = tuple[1];
                break;
            case "BACKGROUND_KNOWLEDGE_ORIGINAL":
                setBackgroundKnowledgeOriginal();
                break;
            case "INITIALIZE_LOADER":
                initializeSolverLoaderAndParser();
                break;
            default:
                throw new CommonException("Solver does not support this setting: " + s, null);
        }
    }

    @Override
    public String getOutputAdditionalInfo() {
        return "No output information.";
    }

    @Override
    public Set<Explanation> getExplanations() {
        ThreadTimes threadTimes = new ThreadTimes(100);
        threadTimes.start();
        this.setReasonerManager();
        ISolver solver = createSolver(threadTimes);
        List<Explanation> expl = null;
        if (solver != null) {
            try {
                expl = solver.solve();
            } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                e.printStackTrace();
            }
        }
        threadTimes.interrupt();
        return new HashSet<Explanation>(expl);
    }

    // for thread version uncomment block below

    @Override
    public void run() {
        synchronized (this) {
            solve();
        }
    }

    @Override
    public void getExplanationsIncrementally() {
        new Thread(this, "abductionManager").start() ;
    }


//    @Override
//    public  void sendExplanation(Explanation explanation) {
//
//    }
}

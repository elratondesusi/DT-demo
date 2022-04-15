package abduction.api.implementation;

import abductionapi.Monitor;
import abductionapi.exception.AxiomObservationException;
import abductionapi.exception.CommonException;
import abductionapi.exception.MultiObservationException;
import abductionapi.manager.AbductionManager;
import algorithms.ISolver;
import algorithms.hybrid.HybridSolver;
import models.Explanation;
import models.Observation;
import org.apache.commons.lang3.NotImplementedException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import reasoner.IReasonerManager;
import reasoner.ReasonerManager;
import reasoner.ReasonerType;
import timer.ThreadTimes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbductionManagerImpl implements AbductionManager {

    private OWLOntology backgroundKnowledge;
    private OWLOntology backgroundKnowledgeOriginal;
    private Observation observation;
    private AbducibleContainerImpl abducibleContainer;
    private boolean isMultipleObservationOnInput;
    private IReasonerManager reasonerManager;
    private Monitor monitor;

    public static boolean MHS_MODE = false;
    public static Integer DEPTH;
    public static Long TIMEOUT;
    public static String OBSERVATION = "";
    public static String INPUT_ONT_FILE = "";
    public static String INPUT_FILE_NAME = "";
    public static ReasonerType REASONER;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                     SOLVER METHODS                                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public AbducibleContainerImpl getAbducibleContainer() {
        return abducibleContainer;
    }

    public IReasonerManager getReasonerManager() {
        return reasonerManager;
    }

    private void setReasonerManager() {
        this.reasonerManager = new ReasonerManager(abducibleContainer.getLoader());
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

    public <T> void setBackgroundKnowledgeOriginal(T owlOntologyOriginal) {
        backgroundKnowledgeOriginal = (OWLOntology)owlOntologyOriginal;
    }

    public OWLOntology getBackgroundKnowledgeOriginal() {
        return backgroundKnowledgeOriginal;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                     OVERRIDED METHODS                                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//    @Override
//    public <T> void setBackgroundKnowledge(T t) {
//        if (t instanceof File) {
//            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//            try {
//                backgroundKnowledge = manager.loadOntologyFromOntologyDocument((File)t);
//            } catch (OWLOntologyCreationException e) {
//                e.printStackTrace();
//            }
//        } else if (t instanceof OWLOntology) {
//            backgroundKnowledge = (OWLOntology) t;
//        }
//    }

    @Override
    public <T> void setBackgroundKnowledge(T owlOntology) {
        backgroundKnowledge = (OWLOntology)owlOntology;
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
    public <T> void setObservation(T observation) {
        if (!isMultipleObservationOnInput && ((Observation)observation).getAxiomsInMultipleObservations() != null && !((Observation)observation).getAxiomsInMultipleObservations().isEmpty()) {
            throw new MultiObservationException();
        }
        this.observation = (Observation)observation;
    }

    @Override
    public <T> void setObservation(Set<T> set) throws MultiObservationException, AxiomObservationException {
        throw new CommonException("This method should not be used.", new NotImplementedException("This method should not be used."));
    }

    @Override
    public <T> void setAbducibles(T t) {
        this.abducibleContainer = (AbducibleContainerImpl)t;
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
            default:
                throw new CommonException("Solver does not support this setting: " + s, null);
        }
    }

    @Override
    public String getOutputAdditionalInfo() {
        throw new NotImplementedException("Not needed to be implemented.");
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

    @Override
    public <T> T getExplanation() {
        return null;
    }

    // for thread version uncomment block below

    @Override
    public void run() {
        synchronized (this) {
            getExplanationsIncrementally();
        }
    }
    @Override
    public void getExplanationsIncrementally() {
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

    @Override
    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public <T> void sendExplanation(T explanation) {
        if (monitor != null){
            monitor.addNewExplanation(explanation);
            monitor.notifyAll();
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}

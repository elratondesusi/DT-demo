package abduction.api.implementation;

import abductionapi.Monitor;
import abductionapi.exception.AxiomObservationException;
import abductionapi.exception.MultiObservationException;
import abductionapi.manager.AbductionManager;
import algorithms.ISolver;
import algorithms.hybrid.HybridSolver;
import models.Explanation;
import models.Observation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import reasoner.IReasonerManager;
import reasoner.ReasonerManager;
import timer.ThreadTimes;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbductionManagerImpl implements AbductionManager {

    private OWLOntology backgroundKnowledge;
    private Observation observation;
    private AbducibleContainerImpl abducibleContainer;
    private boolean isMultipleObservationOnInput;
    private IReasonerManager reasonerManager;
    Monitor monitor;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public IReasonerManager getReasonerManager() {
        return reasonerManager;
    }

    public AbducibleContainerImpl getAbducibleContainer() {
        return abducibleContainer;
    }

    public Observation getObservation() {
        return observation;
    }

    public void setMultipleObservationOnInput(boolean multipleObservationOnInput) {
        isMultipleObservationOnInput = multipleObservationOnInput;
    }

    public OWLOntology getBackgroundKnowledge() {
        return backgroundKnowledge;
    }

    public boolean isMultipleObservationOnInput() {
        return isMultipleObservationOnInput;
    }

    @Override
    public <T> void setBackgroundKnowledge(T t) {
        if (t instanceof File) {
            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            try {
                backgroundKnowledge = manager.loadOntologyFromOntologyDocument((File)t);
            } catch (OWLOntologyCreationException e) {
                e.printStackTrace();
            }
        } else if (t instanceof OWLOntology) {
            backgroundKnowledge = (OWLOntology) t;
        }
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
                expl = solver.solve(this);
            } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                e.printStackTrace();
            }
        }
        threadTimes.interrupt();
        return new HashSet<Explanation>(expl);
    }

    @Override
    public void run() {
        synchronized (this) {
            ThreadTimes threadTimes = new ThreadTimes(100);
            threadTimes.start();
            this.setReasonerManager();
            ISolver solver = createSolver(threadTimes);
            List<Explanation> expl = null;
            if (solver != null) {
                try {
                    synchronized(monitor) {
                        expl = solver.solve(this);
                        show(new HashSet<Explanation>(expl));
                        sendExplanation(null);
                    }
                } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                    e.printStackTrace();
                }
            }
            threadTimes.interrupt();
        }
    }

    @Override
    public <T> void setObservation(T observation) {
        if (!isMultipleObservationOnInput && ((Observation)observation).getAxiomsInMultipleObservations() != null&& !((Observation)observation).getAxiomsInMultipleObservations().isEmpty()) {
            throw new MultiObservationException(null);
        }
        this.observation = (Observation)observation;
    }

    @Override
    public <T> void setAbducibles(T t) {
        this.abducibleContainer = (AbducibleContainerImpl)t;
    }

    public <T> void show(T explanations) {
        System.out.println("\n\n************************************************");
        System.out.println("* Explanations are:                            *");
        System.out.println("************************************************");
        ((Set)explanations).stream().forEach(e -> System.out.println(e));
        System.out.println("************************************************");
    }

    private static ISolver createSolver(ThreadTimes threadTimes) {
        long currentTimeMillis = System.currentTimeMillis();
        return new HybridSolver(threadTimes, currentTimeMillis);
    }

    public void setReasonerManager() {
        this.reasonerManager = new ReasonerManager(abducibleContainer.getLoader());
    }

    @Override
    public void setAdditionalSolverSettings(String s) {

    }

    @Override
    public String getOutputAdditionalInfo() {
        return null;
    }

    @Override
    public <T> T getExplanation() {
        return null;
    }

    @Override
    public <T> void setObservation(Set<T> set) throws MultiObservationException, AxiomObservationException {

    }

    @Override
    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }
}

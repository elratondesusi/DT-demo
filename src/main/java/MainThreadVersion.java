import abduction.api.implementation.AbducibleContainerImpl;
import abduction.api.implementation.AbductionManagerAndAbducibleContainerFactoryImpl;
import abduction.api.implementation.AbductionManagerImpl;
import abductionapi.monitor.Monitor;
import abductionapi.exception.CommonException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.List;
import java.util.stream.Collectors;


public class MainThreadVersion {

    private static Monitor monitor;

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        // DL Abduction API - abductionFactory, abductionManager and abducibleContainer
        AbductionManagerAndAbducibleContainerFactoryImpl abductionFactory = new AbductionManagerAndAbducibleContainerFactoryImpl();
        AbductionManagerImpl abductionManager = abductionFactory.createAbductionManager();
        AbducibleContainerImpl abducibleContainer = abductionFactory.createAbducibleContainer();
        abductionManager.setAbducibles(abducibleContainer);

        abductionManager.setAdditionalSolverSettings("INITIALIZE_LOADER:yes");

        // backgroundKnowledge
        abductionManager.setBackgroundKnowledge(abductionManager.getLoader().getOntology());

        // observation/s
        abductionManager.setMultipleObservationOnInput(abductionManager.getLoader().isMultipleObservationOnInput());
        try {
            abductionManager.setObservation(abductionManager.getLoader().getObservation());
        } catch (CommonException ex) {
            //process Exception somehow
            System.out.println("Solver exception: " + ex);
            throw new CommonException("Solver exception: ", ex);
        }

        List<OWLClass> cc = abductionManager.getLoader().getAbducibles().getClasses().stream().collect(Collectors.toList());
        abducibleContainer.addSymbol(cc.get(0));
        abducibleContainer.addSymbols(abductionManager.getLoader().getAbducibles().getClasses());
        abducibleContainer.addSymbols(abductionManager.getLoader().getAbducibles().getIndividuals());
        abducibleContainer.addSymbols(abductionManager.getLoader().getAbducibles().getRoles());

        abductionManager.setAdditionalSolverSettings("BACKGROUND_KNOWLEDGE_ORIGINAL:yes" );

        // At first the monitor is set to AbductionManager.
        monitor = abductionManager.getMonitor();

        Thread ct = new Thread() {
            public void run()
            {
                while (true) {
                    synchronized(monitor) {
                        try {
                            monitor.wait();
                            Object explanation = monitor.getNextExplanation();
                            if (explanation == null) {
                                monitor.notify();
                                break;
                            }
                            System.out.println("************************************************");
                            System.out.println("New explanation is computed:");
                            System.out.println(explanation);
                            System.out.println("************************************************");
                            monitor.notify();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        ct.start();
        abductionManager.getExplanationsIncrementally();
        // Then a new thread with target of AbductionManager instance is created at the application.
        // Then method run in AbductionManager is executed and new explanations are computed.
//        new Thread(abductionManager, "abductionManager").start() ;
        // If any new explanation is computed BY a solver (overriding AbductionManager.run), it will send a notification on a monitor.
        // Meanwhile, the application monitor is waiting for a new explanation to be shown.
    }
}

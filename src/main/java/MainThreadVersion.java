import abduction.api.implementation.AbducibleContainerImpl;
import abduction.api.implementation.AbductionManagerAndAbducibleContainerFactoryImpl;
import abduction.api.implementation.AbductionManagerImpl;
import abductionapi.Monitor;
import abductionapi.exception.CommonException;
import common.Configuration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class MainThreadVersion {

    private static Monitor monitor = new Monitor();

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        // Abduction API - abductionFactory, abductionManager and abducibleContainer
        AbductionManagerAndAbducibleContainerFactoryImpl abductionFactory = new AbductionManagerAndAbducibleContainerFactoryImpl();
        AbductionManagerImpl abductionManager = abductionFactory.createAbductionManager();
        AbducibleContainerImpl abducibleContainer = abductionFactory.createAbducibleContainer();

        // backgroundKnowledge
        abductionManager.setBackgroundKnowledge(abducibleContainer.getLoader().getOriginalOntology());
        // observation/s
        abductionManager.setMultipleObservationOnInput(abducibleContainer.getLoader().isMultipleObservationOnInput());
        try {
            abductionManager.setObservation(abducibleContainer.getLoader().getObservation());
        } catch (CommonException ex) {
            throw new CommonException("Solver exception: ", ex);
        }

        abducibleContainer.allowLoops(Configuration.LOOPING_ALLOWED);
        abducibleContainer.allowRoleAssertions(Configuration.ROLES_IN_EXPLANATIONS_ALLOWED);

        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getClasses());
        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getIndividuals());
        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getRoles());

        abducibleContainer.addAssertions(abducibleContainer.getLoader().getAbducibles().getAxioms());

        abductionManager.setAbducibles(abducibleContainer);
        abductionManager.setAdditionalSolverSettings("TODO");


        // a non-thread version

//        Set<Explanation> explanations = abductionManager.getExplanations();
//        abductionManager.show(explanations);

        // a thread version

        // At first monitor is set to AbductionManager.
        abductionManager.setMonitor(monitor);
        // Then a new thread with target of AbductionManager instance is created at the application.
        new Thread(abductionManager, "abductionManager").start();
        // Then method run in AbductionManager is executed and new explanations are computed.
        // If any new explanation is computed BY a solver (overriding AbductionManager.run), it will send a notification on a monitor.
        // Meanwhile, application monitor is waiting for a new explanation to be showed.

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
    }
}

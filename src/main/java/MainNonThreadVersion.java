import abduction.api.implementation.AbducibleContainerImpl;
import abduction.api.implementation.AbductionManagerAndAbducibleContainerFactoryImpl;
import abduction.api.implementation.AbductionManagerImpl;
import abductionapi.exception.CommonException;
import models.Explanation;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Set;


public class MainNonThreadVersion {

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        // Abduction API - abductionFactory, abductionManager and abducibleContainer
        AbductionManagerAndAbducibleContainerFactoryImpl abductionFactory = new AbductionManagerAndAbducibleContainerFactoryImpl();
        AbductionManagerImpl abductionManager = abductionFactory.createAbductionManager();
        AbducibleContainerImpl abducibleContainer = abductionFactory.createAbducibleContainer(abductionManager);

        // backgroundKnowledge
        abductionManager.setBackgroundKnowledge(abducibleContainer.getLoader().getOntology());
//        abducibleContainer.getLoader().getOntology().getOWLOntologyManager().getOWLDataFactory().getname
        // observation/s
        abductionManager.setMultipleObservationOnInput(abducibleContainer.getLoader().isMultipleObservationOnInput());
        try {
            abductionManager.setObservation(abducibleContainer.getLoader().getObservation());
        } catch (CommonException ex) {
            throw new CommonException("Solver exception: ", ex);
        }

        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getClasses());
        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getIndividuals());
        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getRoles());

        // how does getAxioms work?
//        abducibleContainer.addAssertions(abducibleContainer.getLoader().getAbducibles().getAxioms(abducibleContainer));

        abductionManager.setAbducibles(abducibleContainer);

        abductionManager.setAdditionalSolverSettings("BACKGROUND_KNOWLEDGE_ORIGINAL:yes" );

        Set<Explanation> explanations = abductionManager.getExplanations();
        // just for debugging and verifying nothing changes to original version od solver
        System.out.println();
        System.out.println("Count:" + String.valueOf(explanations.size()));
        System.out.println();
        abductionManager.show(explanations);
    }
}

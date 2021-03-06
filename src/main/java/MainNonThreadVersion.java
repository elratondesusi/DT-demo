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
            throw new CommonException("Solver exception: ", ex);
        }

        abducibleContainer.addSymbols(abductionManager.getLoader().getAbducibles().getClasses());
        abducibleContainer.addSymbols(abductionManager.getLoader().getAbducibles().getIndividuals());
        abducibleContainer.addSymbols(abductionManager.getLoader().getAbducibles().getRoles());

        abductionManager.setAdditionalSolverSettings("BACKGROUND_KNOWLEDGE_ORIGINAL:yes");

        Set<Explanation> explanations = abductionManager.getExplanations();
        // info just for debugging
        System.out.println();
        System.out.println("Count:" + String.valueOf(explanations.size()));
        System.out.println();
        abductionManager.show(explanations);
    }
}

import abduction.api.implementation.AbducibleContainerImpl;
import abduction.api.implementation.AbductionManagerAndAbducibleContainerFactoryImpl;
import abduction.api.implementation.AbductionManagerImpl;
import abductionapi.exception.CommonException;
import common.Configuration;
import models.Explanation;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Set;


public class MainNonThreadVersion {

    public static void main(String[] args) throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        BasicConfigurator.configure();

        // Abduction API - abductionFactory, abductionManager and abducibleContainer
        AbductionManagerAndAbducibleContainerFactoryImpl abductionFactory = new AbductionManagerAndAbducibleContainerFactoryImpl();
        AbductionManagerImpl abductionManager = abductionFactory.createAbductionManager();
        AbducibleContainerImpl abducibleContainer = abductionFactory.createAbducibleContainer(abductionManager);

        // backgroundKnowledge
        abductionManager.setBackgroundKnowledge(abducibleContainer.getLoader().getOriginalOntology());
        // observation/s
        abductionManager.setMultipleObservationOnInput(abducibleContainer.getLoader().isMultipleObservationOnInput());
        try {
            abductionManager.setObservation(abducibleContainer.getLoader().getObservation());
        } catch (CommonException ex) {
            throw new CommonException("Solver exception: ", ex);
        }

//        abducibleContainer.allowLoops(Configuration.LOOPING_ALLOWED); //teraz je to v parseri
//        abducibleContainer.allowRoleAssertions(Configuration.ROLES_IN_EXPLANATIONS_ALLOWED);
//        abducibleContainer.allowConceptComplement(Configuration.NEGATION_ALLOWED);
        abducibleContainer.allowComplexConcepts(false);
//        abducibleContainer.allowConceptAssertions(..);


        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getClasses());
        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getIndividuals());
        abducibleContainer.addSymbols(abducibleContainer.getLoader().getAbducibles().getRoles());

        abducibleContainer.addAssertions(abducibleContainer.getLoader().getAbducibles().getAxioms(abducibleContainer));

        abductionManager.setAbducibles(abducibleContainer);
//        abductionManager.setAdditionalSolverSettings("TODO");


        Set<Explanation> explanations = abductionManager.getExplanations();
        abductionManager.show(explanations);
    }
}

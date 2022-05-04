package abduction.api.implementation;

import abductionapi.container.AbducibleContainer;
import abductionapi.exception.CommonException;
import abductionapi.factory.AbductionManagerAndAbducibleContainerFactory;
import abductionapi.manager.AbductionManager;
import models.Explanation;
import models.Observation;
import org.semanticweb.owlapi.model.OWLAxiom;

public class AbductionManagerAndAbducibleContainerFactoryImpl implements AbductionManagerAndAbducibleContainerFactory {

    //    public AbducibleContainerImpl createAbducibleContainer(AbductionManagerImpl abductionManager) {
//        try {
//            return new AbducibleContainerImpl(abductionManager);
//        } catch (Exception e) {
//            throw new CommonException("File initialize problem.", e);
//        }
//    }
    public AbducibleContainerImpl createAbducibleContainer(Object ... objects) {
        try {
            return new AbducibleContainerImpl((AbductionManagerImpl)objects[0]);
        } catch (Exception e) {
            throw new CommonException("File initialize problem.", e);
        }
    }

    @Override
    public AbducibleContainer createAbducibleContainer() {
        return null;
    }

    @Override
    public AbductionManagerImpl createAbductionManager() {
        return new AbductionManagerImpl();
    }
}

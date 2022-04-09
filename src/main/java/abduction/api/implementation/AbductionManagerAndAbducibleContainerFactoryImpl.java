package abduction.api.implementation;

import abductionapi.container.AbducibleContainer;
import abductionapi.exception.CommonException;
import abductionapi.factory.AbductionManagerAndAbducibleContainerFactory;

public class AbductionManagerAndAbducibleContainerFactoryImpl implements AbductionManagerAndAbducibleContainerFactory {

    public AbducibleContainerImpl createAbducibleContainer(AbductionManagerImpl abductionManager) {
        try {
            return new AbducibleContainerImpl(abductionManager);
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

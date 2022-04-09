package abduction.api.implementation;

import abductionapi.exception.CommonException;
import abductionapi.factory.AbductionManagerAndAbducibleContainerFactory;

public class AbductionManagerAndAbducibleContainerFactoryImpl implements AbductionManagerAndAbducibleContainerFactory { @Override
    public AbducibleContainerImpl createAbducibleContainer() {
        try {
            return new AbducibleContainerImpl();
        } catch (Exception e) {
            throw new CommonException("File initialize problem.", e);
        }
    }

    @Override
    public AbductionManagerImpl createAbductionManager() {
        return new AbductionManagerImpl();
    }
}

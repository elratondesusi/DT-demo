package abduction.api.implementation;

import abductionapi.factory.AbductionManagerAndAbducibleContainerFactory;

public class AbductionManagerAndAbducibleContainerFactoryImpl implements AbductionManagerAndAbducibleContainerFactory<AbductionManagerImpl, AbducibleContainerImpl> {

    @Override
    public AbducibleContainerImpl createAbducibleContainer() {
        return new AbducibleContainerImpl();
    }

    @Override
    public AbductionManagerImpl createAbductionManager() {
        return new AbductionManagerImpl();
    }
}

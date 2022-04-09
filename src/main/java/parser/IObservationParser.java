package parser;

import abduction.api.implementation.AbductionManagerImpl;

public interface IObservationParser {
    void parse(AbductionManagerImpl abductionManager) throws Exception;
}

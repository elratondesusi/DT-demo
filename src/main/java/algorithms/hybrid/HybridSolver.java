package algorithms.hybrid;

import abduction.api.implementation.AbductionManagerImpl;
import algorithms.ISolver;
import com.google.common.collect.Iterables;
import common.Configuration;
import common.Printer;
import models.Abducibles;
import models.Explanation;
import models.Literals;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.*;
import reasoner.AxiomManager;
import timer.ThreadTimes;

import java.util.*;

/**
 * Base = knowledgeBase + negObservation
 * Literals = set of all literals / concepts with named individual except observation
 */

public class HybridSolver implements ISolver {

    private AbductionManagerImpl abductionManager;
    private Literals abd_literals;
    private ModelExtractor modelExtractor;
    private ExplanationsFilter explanationsFilter;
    private SetDivider setDivider;
    private List<OWLAxiom> lenghtOneExplanations = new ArrayList<>();
    private Set<Set<OWLAxiom>> pathsInCertainDepth = new HashSet<>();

    public OWLOntology ontology;
    public List<ModelNode> models;
    public List<ModelNode> negModels;
    public List<OWLAxiom> assertionsAxioms;
    public List<OWLAxiom> negAssertionsAxioms;
    public List<Explanation> explanations;
    public Set<OWLAxiom> path;
    //public List<OWLAxiom> path;
    public Set<OWLAxiom> pathDuringCheckingMinimality;
    public Abducibles abducibles;
    public int lastUsableModelIndex;
    public OWLAxiom negObservation;
    public ThreadTimes threadTimes;
    public long currentTimeMillis;
    public Map<Integer, Double> level_times = new HashMap<>();
    public boolean checkingMinimalityWithQXP = false;
    private ICheckRules checkRules;
    private Integer currentDepth;

    public HybridSolver(ThreadTimes threadTimes, long currentTimeMillis, AbductionManagerImpl abductionManager) {
        this.abductionManager = abductionManager;
        System.out.println("Optimalizacia QXP " + Configuration.CHECKING_MINIMALITY_BY_QXP);
        System.out.println("Optimalizacia Cached Conflicts - The Longest Conf " + Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT);
        System.out.println("Optimalizacia Cached Conflicts - Median " + Configuration.CACHED_CONFLICTS_TABLE_OF_OCCURRENCE);
        System.out.println("Roly " + abductionManager.getAbducibles().areRoleAssertionsEnabled());
        System.out.println("Negation " + abductionManager.getAbducibles().areConceptComplementsEnabled());
        System.out.println("MHS MODE " + abductionManager.MHS_MODE);
        System.out.println();
        this.threadTimes = threadTimes;
        this.currentTimeMillis = currentTimeMillis;
    }

    @Override
    public List<Explanation> solve() throws OWLOntologyStorageException, OWLOntologyCreationException {
        this.ontology = this.abductionManager.getBackgroundKnowledgeOriginal();
        this.modelExtractor = new ModelExtractor(abductionManager.getAbducibles().getLoader(), abductionManager.getReasonerManager(), this);
        this.explanationsFilter = new ExplanationsFilter(abductionManager, this);
        this.setDivider = new SetDivider(this);
        this.checkRules = new CheckRules(abductionManager);

        negObservation = abductionManager.getAbducibles().getLoader().getNegObservation().getOwlAxiom();

        this.abducibles = new Abducibles(abductionManager.getAbducibles().getLoader());
        this.abducibles.setAxioms(abductionManager.getAbducibles().getAssertions());
        this.abducibles.setClasses(abductionManager.getAbducibles().getAbduciblesConcepts());
        this.abducibles.setIndividuals(abductionManager.getAbducibles().getAbduciblesIndividuals());
        this.abducibles.setRoles(abductionManager.getAbducibles().getAbduciblesRoles());

        if (!abductionManager.getReasonerManager().isOntologyConsistent())
            return null;
        initialize();
        if (abductionManager.getReasonerManager().isOntologyWithLiteralsConsistent(abd_literals.getOwlAxioms(), ontology))
            return null;
        startSolving();
        return explanationsFilter.showExplanations();
    }

    private void initialize() {
        models = new ArrayList<>();
        negModels = new ArrayList<>();

        assertionsAxioms = new ArrayList<>();
        negAssertionsAxioms = new ArrayList<>();

        abductionManager.getAbducibles().getLoader().getOntologyManager().addAxiom(ontology, abductionManager.getAbducibles().getLoader().getNegObservation().getOwlAxiom());
        abductionManager.getReasonerManager().addAxiomToOntology(abductionManager.getAbducibles().getLoader().getNegObservation().getOwlAxiom());

        for(OWLClass owlClass : abductionManager.getAbducibles().getAbduciblesConcepts()){
            List<OWLAxiom> classAssertionAxiom = AxiomManager.createClassAssertionAxiom(abductionManager.getAbducibles().getLoader(), owlClass);
            for (int i = 0; i < classAssertionAxiom.size(); i++) {
                if (i % 2 == 0) {
                    assertionsAxioms.add(classAssertionAxiom.get(i));
                } else {
                    negAssertionsAxioms.add(classAssertionAxiom.get(i));
                }
            }
        }

        if(abductionManager.getAbducibles().areRoleAssertionsEnabled()){
            for(OWLObjectProperty objectProperty : abductionManager.getAbducibles().getAbduciblesRoles()){
                List<OWLAxiom> objectPropertyAssertionAxiom = AxiomManager.createObjectPropertyAssertionAxiom(abductionManager, objectProperty);
                for (int i = 0; i < objectPropertyAssertionAxiom.size(); i++) {
                    if (i % 2 == 0) {
                        assertionsAxioms.add(objectPropertyAssertionAxiom.get(i));
                    } else {
                        negAssertionsAxioms.add(objectPropertyAssertionAxiom.get(i));
                    }
                }
            }
        }

        if (abductionManager.isMultipleObservationOnInput()){
            assertionsAxioms.removeAll(abductionManager.getObservation().getAxiomsInMultipleObservations());
            negAssertionsAxioms.removeAll(abductionManager.getObservation().getAxiomsInMultipleObservations());
        } else {
            assertionsAxioms.remove(abductionManager.getObservation().getOwlAxiom());
            negAssertionsAxioms.remove(abductionManager.getObservation().getOwlAxiom());
        }

        Set<OWLAxiom> to_abd = new HashSet<>();

        if(abductionManager.getAbducibles().areConceptComplementsEnabled()){
            to_abd.addAll(assertionsAxioms);
            to_abd.addAll(negAssertionsAxioms);
        } else {
            to_abd.addAll(assertionsAxioms);
        }

        abd_literals = new Literals(to_abd);
    }

    private void startSolving() throws OWLOntologyStorageException, OWLOntologyCreationException {
        explanations = new LinkedList<>();
        path = new HashSet<>();
        currentDepth = 0;

        Queue<TreeNode> queue = new LinkedList<>();
        initializeTree(queue);

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();

            if(increaseDepth(node)){
                currentDepth++;
            }
            if(isTimeout() || !ModelNode.class.isAssignableFrom(node.getClass())){
                break;
            }

            ModelNode model = (ModelNode) node;
            if (model.depth.equals(abductionManager.DEPTH)) {
                break;
            }

            for (OWLAxiom child : model.data){

                if(isTimeout()){
                    return;
                }

                if(isIncorrectPath(model, child)){
                    continue;
                }

                Explanation explanation = new Explanation();
                explanation.addAxioms(model.label);
                explanation.addAxiom(child);
                explanation.setAcquireTime(threadTimes.getTotalUserTimeInSec());
                explanation.setLevel(currentDepth);

                path = new HashSet<>(explanation.getOwlAxioms());

                if(canBePruned(explanation)){
                    path.clear();
                    continue;
                }

                if (!Configuration.REUSE_OF_MODELS || !usableModelInModels()) {
                    if(isTimeout()){
                        return;
                    }
                    if(abductionManager.MHS_MODE){
                        if(!isOntologyConsistent()){
                            explanation.setDepth(explanation.getOwlAxioms().size());

                            abductionManager.sendExplanation(explanation);

                            explanations.add(explanation);
                            path.clear();
                            continue;
                        }
                    } else {
                        if (!addNewExplanations()){
                            path.clear();
                            continue;
                        }
                    }
                }
                else{
                    lenghtOneExplanations = new ArrayList<>();
                }
                addNodeToTree(queue, explanation, model);
            }
        }
        path.clear();
        if(!level_times.containsKey(currentDepth)){
            level_times.put(currentDepth, threadTimes.getTotalUserTimeInSec());
        }
        currentDepth = 0;
    }

    private void initializeTree(Queue<TreeNode> queue) throws OWLOntologyStorageException {
        if(abductionManager.MHS_MODE){
            if(!isOntologyConsistent()){
                return;
            }
        } else {
            Conflict conflict = getMergeConflict();
            for (Explanation e: conflict.getExplanations()){
                e.setDepth(e.getOwlAxioms().size());
            }
            explanations = conflict.getExplanations();
        }

        ModelNode root = createModelNodeFromExistingModel(true, null, null);
        if(root == null){
            return;
        }
        queue.add(root);
    }

    private void addToExplanations(Explanation explanation){
        explanation.setDepth(explanation.getOwlAxioms().size());
        if(Configuration.CHECKING_MINIMALITY_BY_QXP){
            Explanation newExplanation = getMinimalExplanationByCallingQXP(explanation);

            abductionManager.sendExplanation(newExplanation);

            explanations.add(newExplanation);
            if(Configuration.CACHED_CONFLICTS_TABLE_OF_OCCURRENCE){
                setDivider.addPairsOfLiteralsToTable(newExplanation);
            }
        } else {
            abductionManager.sendExplanation(explanation);

            explanations.add(explanation);
            if(Configuration.CACHED_CONFLICTS_TABLE_OF_OCCURRENCE){
                setDivider.addPairsOfLiteralsToTable(explanation);
            }
        }
    }

    private void addNodeToTree(Queue<TreeNode> queue, Explanation explanation, ModelNode model){
        ModelNode modelNode = createModelNodeFromExistingModel(false, explanation, model.depth + 1);
        if(modelNode == null){
            path.clear();
            return;
        }
        if(!abductionManager.MHS_MODE){
            modelNode.add_node_explanations(model);
            modelNode.add_to_explanations(lenghtOneExplanations);
        }
        queue.add(modelNode);
        path.clear();
    }

    private ModelNode createModelNodeFromExistingModel(boolean isRoot, Explanation explanation, Integer depth){
        ModelNode modelNode = new ModelNode();
        if (usableModelInModels()){
            if(isRoot){
                modelNode.data = negModels.get(lastUsableModelIndex).data;
                modelNode.label = new LinkedList<>();
                modelNode.depth = 0;
            } else {
                modelNode.label = explanation.getOwlAxioms();
                modelNode.data = negModels.get(lastUsableModelIndex).data;
                modelNode.data.removeAll(path);
                modelNode.depth = depth;
            }
        }
        if(modelNode.data == null || modelNode.data.isEmpty()){
            return null;
        }
        return modelNode;
    }

    private boolean increaseDepth(TreeNode node){
        if (node.depth > currentDepth){
            Double time = threadTimes.getTotalUserTimeInSec();
            level_times.put(currentDepth, time);
            explanationsFilter.showExplanationsWithDepth(currentDepth, false, time);
            explanationsFilter.showExplanationsWithLevel(currentDepth, false, time);
            pathsInCertainDepth = new HashSet<>();
            return true;
        }
        return false;
    }

    private boolean isTimeout(){
        if (abductionManager.TIMEOUT != null && threadTimes.getTotalUserTimeInSec() > abductionManager.TIMEOUT) {
            System.out.println("timeout");
            Double time = threadTimes.getTotalUserTimeInSec();
            level_times.put(currentDepth, time);
            explanationsFilter.showExplanationsWithDepth(currentDepth + 1, true, time);
            explanationsFilter.showExplanationsWithLevel(currentDepth, true, time);
            return true;
        }
        return false;
    }

    private boolean canBePruned(Explanation explanation) throws OWLOntologyCreationException {
        if (!checkRules.isMinimal(explanations, explanation)){
            return true;
        }
        if(pathsInCertainDepth.contains(path)){
            return true;
        }
        pathsInCertainDepth.add(new HashSet<>(path));

        if(abductionManager.MHS_MODE){
            if(!checkRules.isRelevant(explanation)){
                return true;
            }
            if(!checkRules.isConsistent(explanation)){
                return true;
            }
        }

        if(!abductionManager.MHS_MODE){
            if (checkRules.isExplanation(explanation)){
                addToExplanations(explanation);
                return true;
            }
        }
        return false;
    }

    private boolean isIncorrectPath(ModelNode model, OWLAxiom child){
        if (model.label.contains(AxiomManager.getComplementOfOWLAxiom(abductionManager.getAbducibles().getLoader(), child)) ||
                child.equals(abductionManager.getObservation().getOwlAxiom()) ||
                (abductionManager.isMultipleObservationOnInput() && abductionManager.getObservation().getAxiomsInMultipleObservations().contains(child))){
            return true;
        }

        if (!abd_literals.contains(child)){
            return true;
        }

        return false;
    }

    private Conflict getMergeConflict() throws OWLOntologyStorageException {
        if (!abductionManager.getReasonerManager().isOntologyConsistent()) {
            return new Conflict();
        }

        if (abductionManager.getReasonerManager().isOntologyWithLiteralsConsistent(abd_literals.getOwlAxioms(), ontology)) {
            return new Conflict();
        }
        return findConflicts(abd_literals);
    }

    private List<Explanation> findExplanations(){
        abd_literals.removeLiterals(path);
        abd_literals.removeLiterals(lenghtOneExplanations);
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.setIndexesOfExplanations(explanations.size());
        }
        Conflict conflict = findConflicts(abd_literals);
        abd_literals.addLiterals(path);
        abd_literals.addLiterals(lenghtOneExplanations);
        return conflict.getExplanations();
    }

    private Conflict findConflicts(Literals literals) {
        path.remove(negObservation);
        abductionManager.getReasonerManager().addAxiomsToOntology(path);
        if (isOntologyWithLiteralsConsistent(literals.getOwlAxioms())) {
            return new Conflict(literals, new LinkedList<>());
        }
        removeAxiomsFromOntology(path);
        if (literals.getOwlAxioms().size() == 1) {
            List<Explanation> explanations = new LinkedList<>();
            Explanation newExplanation = new Explanation(literals.getOwlAxioms(), literals.getOwlAxioms().size(), currentDepth, threadTimes.getTotalUserTimeInSec());

            abductionManager.sendExplanation(newExplanation);

            explanations.add(newExplanation);
            return new Conflict(new Literals(), explanations);
        }

        int indexOfExplanation = -1;
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            indexOfExplanation = setDivider.getIndexOfTheLongestAndNotUsedConflict();
        }
        List<Literals> sets = setDivider.divideIntoSets(literals);

        Conflict conflictC1 = findConflicts(sets.get(0));
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        }

        Conflict conflictC2 = findConflicts(sets.get(1));
        if(Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        }

        List<Explanation> explanations = new LinkedList<>();
        explanations.addAll(conflictC1.getExplanations());
        explanations.addAll(conflictC2.getExplanations());

        Literals conflictLiterals = new Literals();
        conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());
        conflictLiterals.getOwlAxioms().addAll(conflictC2.getLiterals().getOwlAxioms());

        while (!isOntologyWithLiteralsConsistent(conflictLiterals.getOwlAxioms())) {
            path.addAll(conflictC2.getLiterals().getOwlAxioms());
            Explanation X = getConflict(conflictC2.getLiterals().getOwlAxioms(), conflictC1.getLiterals(), path);
            path.removeAll(conflictC2.getLiterals().getOwlAxioms());

            path.addAll(X.getOwlAxioms());
            Explanation CS = getConflict(X.getOwlAxioms(), conflictC2.getLiterals(), path);
            path.removeAll(X.getOwlAxioms());

            CS.getOwlAxioms().addAll(X.getOwlAxioms());

            conflictLiterals.getOwlAxioms().removeAll(conflictC1.getLiterals().getOwlAxioms());
            X.getOwlAxioms().stream().findFirst().ifPresent(axiom -> conflictC1.getLiterals().getOwlAxioms().remove(axiom));
            conflictLiterals.getOwlAxioms().addAll(conflictC1.getLiterals().getOwlAxioms());

            if (explanations.contains(CS)) {
                break;
            }

            Explanation newExplanation = CS;
            if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                newExplanation = getMinimalExplanationByCallingQXP(CS);
            }

            abductionManager.sendExplanation(newExplanation);

            explanations.add(newExplanation);
            if(Configuration.CACHED_CONFLICTS_TABLE_OF_OCCURRENCE){
                setDivider.addPairsOfLiteralsToTable(newExplanation);
            }
        }

        return new Conflict(conflictLiterals, explanations);
    }

    private Explanation getConflict(Collection<OWLAxiom> axioms, Literals literals, Set<OWLAxiom> actualPath) {
        if (!axioms.isEmpty() && !isOntologyConsistent()) {
            return new Explanation();
        }

        if (literals.getOwlAxioms().size() == 1) {
            return new Explanation(literals.getOwlAxioms(), 1, currentDepth, threadTimes.getTotalUserTimeInSec());
        }

        int indexOfExplanation = -1;
        if(!checkingMinimalityWithQXP && Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            indexOfExplanation = setDivider.getIndexOfTheLongestAndNotUsedConflict();
        }
        List<Literals> sets = setDivider.divideIntoSets(literals);

        actualPath.addAll(sets.get(0).getOwlAxioms());
        Explanation D2 = getConflict(sets.get(0).getOwlAxioms(), sets.get(1), actualPath);
        actualPath.removeAll(sets.get(0).getOwlAxioms());
        if(!checkingMinimalityWithQXP && Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        }

        actualPath.addAll(D2.getOwlAxioms());
        Explanation D1 = getConflict(D2.getOwlAxioms(), sets.get(0), actualPath);
        actualPath.removeAll(D2.getOwlAxioms());
        if(!checkingMinimalityWithQXP && Configuration.CACHED_CONFLICTS_LONGEST_CONFLICT){
            setDivider.addIndexToIndexesOfExplanations(indexOfExplanation);
        }

        Set<OWLAxiom> conflicts = new HashSet<>();
        conflicts.addAll(D1.getOwlAxioms());
        conflicts.addAll(D2.getOwlAxioms());

        return new Explanation(conflicts, conflicts.size(), currentDepth, threadTimes.getTotalUserTimeInSec());
    }

    private boolean usableModelInModels(){
        for (int i = models.size()-1; i >= 0 ; i--){
            if (models.get(i).data.containsAll(path)){
                lastUsableModelIndex = i;
                return true;
            }
        }
        return false;
    }

    private boolean addNewExplanations(){
        List<Explanation> newExplanations = findExplanations();
        lenghtOneExplanations = new ArrayList<>();
        for (Explanation conflict : newExplanations){
            if (conflict.getOwlAxioms().size() == 1){
                lenghtOneExplanations.add(Iterables.get(conflict.getOwlAxioms(), 0));
            }
            conflict.addAxioms(path);
            if (checkRules.isMinimal(explanations, conflict)){
                Explanation newExplanation = conflict;
                if(Configuration.CHECKING_MINIMALITY_BY_QXP){
                    newExplanation = getMinimalExplanationByCallingQXP(conflict);
                }
                newExplanation.setDepth(newExplanation.getOwlAxioms().size());

                abductionManager.sendExplanation(newExplanation);

                explanations.add(newExplanation);
                if(Configuration.CACHED_CONFLICTS_TABLE_OF_OCCURRENCE){
                    setDivider.addPairsOfLiteralsToTable(newExplanation);
                }
            }
        }
        if (newExplanations.size() == this.lenghtOneExplanations.size()){
            return false;
        }
        return !newExplanations.isEmpty();
    }

    private boolean isOntologyWithLiteralsConsistent(Collection<OWLAxiom> axioms){
        path.addAll(axioms);
        boolean isConsistent = isOntologyConsistent();
        path.removeAll(axioms);
        return isConsistent;
    }

    private boolean isOntologyConsistent(){
        if (Configuration.GET_MODELS_BY_REASONER){
            return (!modelExtractor.getNegModelByReasoner().data.isEmpty());
        }
        return (!modelExtractor.getNegModelByOntology().data.isEmpty());
    }

    public void printAxioms(List<OWLAxiom> axioms){
        List<String> result = new ArrayList<>();
        for (OWLAxiom owlAxiom : axioms) {
            result.add(Printer.print(owlAxiom));
        }
        System.out.println("{" + StringUtils.join(result, ",") + "}");
    }

    public Explanation getMinimalExplanationByCallingQXP(Explanation explanation){
        Set<OWLAxiom> temp = new HashSet<>();
        temp.addAll(explanation.getOwlAxioms());
        if(path != null){
            temp.addAll(path);
        }
        Literals potentialExplanations = new Literals(temp);

        checkingMinimalityWithQXP = true;
        pathDuringCheckingMinimality = new HashSet<>();
        Explanation newExplanation = getConflict(new ArrayList<>(), potentialExplanations, pathDuringCheckingMinimality);
        checkingMinimalityWithQXP = false;
        pathDuringCheckingMinimality = new HashSet<>();

        return newExplanation;
    }

    public void removeAxiomsFromOntology(Set<OWLAxiom> axioms){
//        abductionManager.getReasonerManager().resetOntology(abductionManager.getAbducibleContainer().getLoader().getOriginalOntology().axioms());
        abductionManager.getReasonerManager().resetOntology(abductionManager.getBackgroundKnowledgeOriginal().axioms());
    }

}
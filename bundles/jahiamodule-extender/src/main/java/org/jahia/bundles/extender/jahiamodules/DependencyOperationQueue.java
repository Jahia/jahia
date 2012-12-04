package org.jahia.bundles.extender.jahiamodules;

import java.util.*;

/**
 * A queue that order tasks depending on their dependencies to other tasks
 */
public class DependencyOperationQueue {

    List<DependencyOperation> sortedOperations = new ArrayList<DependencyOperation>();

    public abstract class DependencyOperation implements Comparable<DependencyOperation> {

        private String operationId;
        private long insertionTime;
        private Set<String> dependencies;
        private Set<String> unsatisfiedDependencies;

        public DependencyOperation(String operationId, Set<String> dependencies) {
            this.operationId = operationId;
            this.dependencies = dependencies;
            this.unsatisfiedDependencies = new HashSet<String>(dependencies);
            this.insertionTime = System.currentTimeMillis();
        }

        public abstract Object execute();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DependencyOperation that = (DependencyOperation) o;

            if (!operationId.equals(that.operationId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return operationId.hashCode();
        }

        @Override
        public int compareTo(DependencyOperation dependencyOperation) {
            if (unsatisfiedDependencies.size() < dependencyOperation.unsatisfiedDependencies.size()) {
                return -1;
            }
            if (unsatisfiedDependencies.size() == dependencyOperation.unsatisfiedDependencies.size()) {
                return new Long(insertionTime).compareTo(new Long(dependencyOperation.insertionTime));
            }
            return 1;
        }

        public void addOperation(DependencyOperation newOperation) {
            Set<DependencyOperation> operationsExecuted = new TreeSet<DependencyOperation>();
            for (DependencyOperation dependencyOperation : sortedOperations) {
                if (dependencyOperation.unsatisfiedDependencies.contains(newOperation.operationId)) {
                    dependencyOperation.unsatisfiedDependencies.remove(operationId);
                    if (dependencyOperation.unsatisfiedDependencies.size() == 0) {
                        dependencyOperation.execute();
                        operationsExecuted.add(dependencyOperation);
                    }
                }
            }
            for (DependencyOperation dependencyOperation : operationsExecuted) {
                sortedOperations.remove(dependencyOperation);
            }
            if (newOperation.unsatisfiedDependencies.size() == 0) {
                newOperation.execute();
            } else {
                sortedOperations.add(newOperation);
            }
            Collections.sort(sortedOperations);
        }

    }


}

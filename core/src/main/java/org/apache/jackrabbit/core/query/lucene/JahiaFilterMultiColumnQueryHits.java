//package org.apache.jackrabbit.core.query.lucene;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
//import org.apache.jackrabbit.core.query.lucene.constraint.EvaluationContext;
//import org.slf4j.Logger;
//import org.apache.lucene.index.IndexReader;
//
//public class JahiaFilterMultiColumnQueryHits extends FilterMultiColumnQueryHits {
//    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JahiaFilterMultiColumnQueryHits.class);    
//
//    private Constraint constraint = null;
//    private EvaluationContext context = null;
//
//    public JahiaFilterMultiColumnQueryHits(MultiColumnQueryHits hits, Constraint constraint, EvaluationContext context) {
//        super(hits);
//        this.context = context;
//        this.constraint = constraint;
//    }
//
//    public IndexReader getReader() {
//        return context.getIndexReader();
//    }
//
//
//    public ScoreNode[] nextScoreNodes() throws IOException {
//        ScoreNode[] next;
//        do {
//            next = super.nextScoreNodes();
//            if (log.isDebugEnabled()) {
//                if (next != null) {
//                    log.debug(Arrays.asList(next).toString());
//                }
//            }
//        } while (next != null && constraint != null && !constraint.evaluate(next, getSelectorNames(), context));
//        return next;
//    }
//
//    public int getSize() {
//        return -1;
//    }
//
//    public void skip(int n) throws IOException {
//        while (n-- > 0) {
//            nextScoreNodes();
//        }
//    }
//
//}

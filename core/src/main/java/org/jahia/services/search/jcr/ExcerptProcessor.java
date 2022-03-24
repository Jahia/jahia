package org.jahia.services.search.jcr;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositionVector;

import java.io.IOException;
import java.util.Set;

public interface ExcerptProcessor {
    String handleText(TermPositionVector tpv, String text, Set<Term[]> terms, int maxFragments, int maxFragmentSize) throws IOException;
}

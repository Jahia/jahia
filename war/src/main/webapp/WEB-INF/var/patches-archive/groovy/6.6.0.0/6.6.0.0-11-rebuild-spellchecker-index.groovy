import org.jahia.services.search.spell.CompositeSpellChecker;

def log = log;

log.info("Force rebuilding spellchecker indexes...");

CompositeSpellChecker.updateSpellCheckerIndex();

log.info("... done rebuilding spellchecker indexes.");
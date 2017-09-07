package com.graphaware.nlp.persistence;

import com.graphaware.nlp.NLPIntegrationTest;
import com.graphaware.nlp.domain.AnnotatedText;
import com.graphaware.nlp.domain.Sentence;
import com.graphaware.nlp.domain.Tag;
import com.graphaware.nlp.util.TestNLPGraph;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AnnotatedTextPersistenceTest extends NLPIntegrationTest {

    @Test
    public void testTagsHavingTwoDifferentPOSInDifferentSentencesShouldReflectBothPOS() {
        String text = "The discipline of preparing and peer reviewing formal engineering reports leads to a high degree of accuracy and technical rigor.";
        String text2 = "During this effort to establish accurate crack information, it was discovered that several cracks were kinked rather than extending in a self-similar crack growth direction as was implied by the sketches and analyses reports in the briefing charts.";
        TestNLPGraph test = new TestNLPGraph(getDatabase());
        AnnotatedText at1 = createAnnotatedTextFor(text, "reports", "VGB");
        try (Transaction tx = getDatabase().beginTx()) {
            getNLPManager().getPersister(AnnotatedText.class).persist(at1, "test-a", "1");
            tx.success();
        }
        test.assertTagWithValueHasPos("reports", "VGB");
        AnnotatedText at2 = createAnnotatedTextFor(text2, "reports", "NNS");
        try (Transaction tx = getDatabase().beginTx()) {
            getNLPManager().getPersister(AnnotatedText.class).persist(at2, "test-b", String.valueOf(System.currentTimeMillis()));
            tx.success();
        }
        test.assertTagWithValueHasPos("reports", "VGB");
        test.assertTagWithValueHasPos("reports", "NNS");
    }

    private AnnotatedText createAnnotatedTextFor(String text, String expectedTokenForPOS, String expectedPOS) {
        AnnotatedText annotatedText = new AnnotatedText();
        annotatedText.setText(text);
        AtomicInteger inc = new AtomicInteger();
        for (String s : text.split("\\.")) {
            Sentence sentence = new Sentence(s, inc.get());
            for (String token : s.split(" ")) {
                Tag tag = new Tag(token, "en");
                if (token.equals(expectedTokenForPOS)) {
                    tag.setPos(Collections.singletonList(expectedPOS));
                }
                sentence.addTagOccurrence(0, 20, sentence.addTag(tag));
            }
            inc.incrementAndGet();
            annotatedText.addSentence(sentence);
        }

        return annotatedText;
    }
}

package nl.hsac.fitnesse.junit.allure;

import ru.yandex.qatools.allure.events.TestCaseEvent;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.TestCaseResult;

import java.util.List;

/**
 * Created by Tom on 4-7-2016.
 */

public class AllureSetLabelsEvent implements TestCaseEvent {

    private List<Label> labels;

    public AllureSetLabelsEvent(List<Label> labels) {
        this.labels = labels;
    }

    @Override
    public void process(TestCaseResult context) {
        context.setLabels(labels);
    }
}

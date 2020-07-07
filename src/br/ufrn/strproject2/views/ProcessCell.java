package br.ufrn.strproject2.views;

import br.ufrn.strproject2.models.HostProcess;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author dhiogoboza
 */
public class ProcessCell extends ListCell<HostProcess> {

    private final HBox myView;
    private final Label processName;
    private final Label cpu;

    public ProcessCell() {
        processName = new Label();
        processName.setPrefWidth(Metrics.PROCESS_NAME_W);

        cpu = new Label();
        cpu.setPrefWidth(Metrics.PROCESS_CPU_W);

        myView = new HBox(processName, cpu);
    }

    @Override
    protected void updateItem(HostProcess item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            processName.setText(item.getReadableName());
            cpu.setText(item.getCPU());

            setGraphic(myView);
        }

    }

}

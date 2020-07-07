/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrn.strproject2;

import br.ufrn.strproject2.views.MainStage;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author dhiogoboza
 */
public class Main extends Application {
    private MainStage mainStage;

    @Override
    public void start(Stage primaryStage) {
        mainStage = new MainStage(primaryStage);

        mainStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

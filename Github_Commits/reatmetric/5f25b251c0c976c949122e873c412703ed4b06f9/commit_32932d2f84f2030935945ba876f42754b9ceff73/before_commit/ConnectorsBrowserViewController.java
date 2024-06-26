/*
 * Copyright (c)  2020 Dario Lucia (https://www.dariolucia.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package eu.dariolucia.reatmetric.ui.controller;

import eu.dariolucia.reatmetric.api.IReatmetricSystem;
import eu.dariolucia.reatmetric.api.common.exceptions.ReatmetricException;
import eu.dariolucia.reatmetric.api.transport.ITransportConnector;
import eu.dariolucia.reatmetric.api.transport.ITransportSubscriber;
import eu.dariolucia.reatmetric.api.transport.TransportStatus;
import eu.dariolucia.reatmetric.ui.ReatmetricUI;
import eu.dariolucia.reatmetric.ui.utils.FxUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author dario
 */
public class ConnectorsBrowserViewController extends AbstractDisplayController implements ITransportSubscriber {

    private static final Logger LOG = Logger.getLogger(ConnectorsBrowserViewController.class.getName());

    // Pane control
    @FXML
    protected AnchorPane displayTitledPane;

    @FXML
    private VBox vbox;
    @FXML
    private ScrollPane scrollpane;

    private final Map<String, ConnectorStatusWidgetController> connector2controller = new ConcurrentHashMap<>();
    private final Map<String, ITransportConnector> name2connector = new ConcurrentHashMap<>();

    @Override
    protected Window retrieveWindow() {
        return displayTitledPane.getScene().getWindow();
    }

    @Override
    protected void doInitialize(URL url, ResourceBundle rb) {
        // Nothing
    }

    @Override
    protected Control doBuildNodeForPrinting() {
        // Print function not available in this view
        return null;
    }

    @Override
    protected void doSystemDisconnected(IReatmetricSystem system, boolean oldStatus) {
        this.displayTitledPane.setDisable(true);
        // Clear the list view
        try {
            clearConnectorsModel();
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE, "Remote exception when clearing connectors", e);
        }
    }

    @Override
    protected void doSystemConnected(IReatmetricSystem system, boolean oldStatus) {
        this.displayTitledPane.setDisable(false);
        startSubscription();
    }

    private void startSubscription() {
        try {
            clearConnectorsModel();
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE, "Remote exception when clearing connectors", e);
        }
        ReatmetricUI.threadPool(getClass()).execute(() -> {
            try {
                buildConnectorsModel();
            } catch (ReatmetricException | RemoteException e) {
                LOG.log(Level.SEVERE, "Cannot retrieve connectors: " + e.getMessage(), e);
            }
        });
    }

    private void clearConnectorsModel() throws RemoteException {
        // First lock
        for(Map.Entry<String, ITransportConnector> entry : this.name2connector.entrySet()) {
            entry.getValue().deregister(this);
        }
        this.name2connector.clear();
        this.connector2controller.clear();
        this.vbox.getChildren().removeAll(this.vbox.getChildren());
        this.vbox.getChildren().clear();
        this.vbox.layout();
    }

    private void buildConnectorsModel() throws ReatmetricException, RemoteException {
        final List<ITransportConnector> connectors = ReatmetricUI.selectedSystem().getSystem().getTransportConnectors();
        FxUtils.runLater(() -> {
            for(ITransportConnector tc : connectors) {
                ConnectorStatusWidgetController controller = buildConnectorController();
                if(controller != null) {
                    try {
                        connector2controller.put(tc.getName(), controller);
                        name2connector.put(tc.getName(), tc);
                        controller.setConnector(tc);
                        tc.register(this);
                        status(tc.getLastTransportStatus());
                    } catch (RemoteException e) {
                        LOG.log(Level.SEVERE, "Remote exception when registering to connector", e);
                    }
                }
            }
            vbox.layout();
        });
    }

    private ConnectorStatusWidgetController buildConnectorController() {
        try {
            URL paneUrl = getClass().getResource("/eu/dariolucia/reatmetric/ui/fxml/ConnectorStatusWidget.fxml");
            FXMLLoader loader = new FXMLLoader(paneUrl);
            VBox pp = loader.load();
            HBox.setHgrow(pp, Priority.ALWAYS);
            vbox.getChildren().add(pp);
            return loader.getController();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Cannot load FXML file", e);
            return null;
        }
    }

    @Override
    public void status(TransportStatus status) {
        FxUtils.runLater(() -> {
            ConnectorStatusWidgetController controller = connector2controller.get(status.getName());
            if(controller != null) {
                controller.updateStatus(status);
            }
        });
    }
}

package hm.edu.controller;

import com.thoughtworks.xstream.XStream;
import hm.edu.model.Connection;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.CheckBox;
import hm.edu.model.Song;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * FXML Controller class for task manager main view
 *
 * @author Ulrike Hammerschall
 * @version 1.0
 */
public class SmartCastFXMLController implements Initializable {

    @FXML
    private TableView<Song> tableView;
    @FXML
    private TableColumn<Song, String> titleColumn;
    @FXML
    private TableColumn<Song, String> urlColumn;
    @FXML
    private Button connectButton;
    @FXML
    private Button addSongButton;
    @FXML
    private Button removeSongButton;
    @FXML
    private Button playButton;
    @FXML
    private Button repeatButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private CheckBox setVideoCheckBox;
    @FXML
    private TextField titleField;
    @FXML
    private TextField urlField;
    @FXML
    private TextField ipField;
    @FXML
    private Label wallLabel;

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    ObservableList<Song> songs = FXCollections.observableArrayList();
    Container container = new Container();

    Connection connection;
    boolean connected;

    boolean video;

    boolean playState;

    boolean playThreadUp;
    long nextTimer;

    boolean repeatOne;
    boolean repeat;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(final URL url, final ResourceBundle rb) {

        load();

        titleColumn.setCellValueFactory(
                new PropertyValueFactory<Song, String>("title"));
        urlColumn.setCellValueFactory(
                new PropertyValueFactory<Song, String>("url"));

        tableView.setRowFactory(tv -> {
            TableRow<Song> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    Song draggedSong = tableView.getItems().remove(draggedIndex);

                    int dropIndex;

                    if (row.isEmpty()) {
                        dropIndex = tableView.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }

                    tableView.getItems().add(dropIndex, draggedSong);

                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row;
        });

        video = false;

        repeat = true;
        repeatOne = false;

        tableView.setItems(songs);
        playThreadUp = false;
        setConnectionState(false);
    }

    @FXML
    private void play() {
        if (!playThreadUp) {
            playThreadUp = true;
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    int index = tableView.getSelectionModel().getSelectedIndex();
                    if (index != -1) {
                        --index;
                    }

                    while (true) {
                        if (playState && nextTimer < System.currentTimeMillis() && nextTimer != -1) {
                            if (!repeatOne) {
                                ++index;
                            }
                            if (index >= songs.size()) {
                                index = 0;
                                if (!repeat) {
                                    playThreadUp = false;
                                    setPlayState(false);
                                    break;
                                }
                            }
                            tableView.getSelectionModel().clearAndSelect(index);
                            Song song = tableView.getSelectionModel().getSelectedItem();
                            String audioOnly = "";
                            if (!video) {
                                audioOnly = "--extract-audio ";
                            }

                            connection.write(audioOnly + song.getUrl() + "\n");
                            checkConnection();
                            nextTimer = connection.getDuration() + System.currentTimeMillis();
                        } else if (!playState || isCancelled() || !connection.up || !playThreadUp) {
                            playThreadUp = false;
                            break;
                        } else {
                            try {
                                Thread.currentThread().sleep(200);
                            } catch (InterruptedException ex) {
                                playThreadUp = false;
                                break;
                            }
                        }
                    }
                    return null;
                }
            };
            new Thread(task)
                    .start();
        }
        setPlayState(true);
        nextTimer = 0;
    }

    @FXML
    private void repeat() {
        if (repeat && repeatOne) {
            repeat = true;
            repeatOne = false;
            repeatButton.setText("⟳All");
        } else if (repeat && !repeatOne) {
            repeat = false;
            repeatOne = false;
            repeatButton.setText("⟳Off");
        } else {
            repeat = true;
            repeatOne = true;
            repeatButton.setText("⟳One");
        }
    }

    @FXML
    private void stop() {
        connection.write("q");
        setPlayState(false);
        nextTimer = 0;
        checkConnection();
    }

    @FXML
    private void next() {
        stop();
        tableView.getSelectionModel().selectNext();
        try {
            Thread.currentThread().sleep(200);
        } catch (InterruptedException ex) {
        }
        play();
    }

    @FXML
    private void previous() {
        stop();
        tableView.getSelectionModel().selectPrevious();
        try {
            Thread.currentThread().sleep(200);
        } catch (InterruptedException ex) {
        }
        play();
    }

    @FXML
    private void addSong() {
        if (inputFieldsNotEmpty()) {
            songs.add(new Song(titleField.getText(), urlField.getText()));
            clearInputFields();
            save();
        }
    }

    @FXML
    private void removeSong() {
        Song song = tableView.getSelectionModel().getSelectedItem();
        if (song != null) {
            songs.remove(song);
            tableView.getColumns().get(0).setVisible(false);
            tableView.getColumns().get(0).setVisible(true);
            save();
        }
    }

    @FXML
    private void connect() {
        if (!connected) {
            String[] address = ipField.getText().split(":");
            connection.setIp(address[0]);
            connection.setPort(Integer.parseInt(address[1]));
            connection.initiate();
            setConnectionState(true);
            save();
        } else {
            stop();
            connection.disconnect();
            setConnectionState(false);
        }
    }

    @FXML
    private void setVideo() {
        video = !video;
    }

    public void setConnection(Connection connection) throws IOException {
        this.connection = connection;
    }

    private void checkConnection() {
        if (!connection.up) {
            setConnectionState(false);
        }
    }

    private void setPlayState(boolean state) {
        playState = state;
        playButton.setDisable(state);
        repeatButton.setDisable(!state);
        stopButton.setDisable(!state);
        previousButton.setDisable(!state);
        nextButton.setDisable(!state);
    }

    private void setConnectionState(boolean state) {
        if (state) {
            connected = true;
            setPlayState(false);
            ipField.setDisable(true);
            connectButton.setText("disconnect");
        } else {
            connected = false;
            ipField.setDisable(false);
            connectButton.setText("connect");
            playButton.setDisable(!state);
            repeatButton.setDisable(!state);
            stopButton.setDisable(!state);
            previousButton.setDisable(!state);
            nextButton.setDisable(!state);
        }
    }

    private boolean inputFieldsNotEmpty() {
        return !titleField.getText().trim().isEmpty() && !urlField.getText().trim().isEmpty();
    }

    private void clearInputFields() {
        titleField.clear();
        urlField.clear();
    }

    private void save() {
        buildContainer();
        saveContainer();
    }

    private void buildContainer() {
        container.songs.clear();
        for (Song song : songs) {
            container.songs.add(song);
        }
        container.ip = ipField.getText();
    }

    private void saveContainer() {
        FileOutputStream fos = null;
        try {
            XStream xstream = new XStream();
            String xml = xstream.toXML(container);
            fos = new FileOutputStream("songContainer.xml");
            fos.write("<?xml version=\"1.0\"?>".getBytes("UTF-8"));
            byte[] bytes = xml.getBytes("UTF-8");
            fos.write(bytes);
        } catch (Exception e) {
            System.err.println("Error in XML Write: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.out.println("IOException on Close of XML-File");
                }
            }
        }
    }

    private void load() {
        File saveFile = new File("songContainer.xml");
        if (saveFile.exists() && !saveFile.isDirectory()) {
            FileReader fileReader;
            try {
                // File exists
                fileReader = new FileReader(saveFile); // load our xml file
                BufferedReader br = new BufferedReader(fileReader);
                if (br.readLine() != null) {                                    // File not empty
                    XStream xstream = new XStream();                            // init XStream
                    xstream.alias("container", Container.class);
                    container = (Container) xstream.fromXML(saveFile);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SmartCastFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SmartCastFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (Song song : container.songs) {
                songs.add(song);
            }
            ipField.setText(container.ip);
        }
    }

    public void setWall(String message) {
        wallLabel.setText(message);
    }
}

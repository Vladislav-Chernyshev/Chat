package ru.gb.java2.chat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


public class ViewController {

    @FXML
    public ListView<String> usersList;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextArea messageTextArea;

    private ClientChat application;

    @FXML
    public void initialize() {
        usersList.setItems(FXCollections.observableArrayList(ClientChat.USERS_TEST_DATA));
    }

    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText().trim();
        if (message.isEmpty()) {
            messageTextArea.clear();
            return;
        }

        String sender = null;

        if (!usersList.getSelectionModel().isEmpty()) {
            sender = usersList.getSelectionModel().getSelectedItem();

        }

        try {
            message = sender != null ? String.join(": ", sender, message) : message;
            Network.getInstance().sendMessage(message);
        } catch (IOException e) {
            application.showNetworkErrorDialog("Ошибка передачи данных по сети", "Не удалось отправить сообщение!");
        }
        appendMessageToChat("Я", message);
    }

    private void appendMessageToChat(String sender, String message) {

        chatHistory.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.appendText(System.lineSeparator());
        if (sender != null) {
            chatHistory.appendText(sender + ":");
            chatHistory.appendText(System.lineSeparator());
        }
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
        messageTextArea.clear();
    }


    @FXML
    public void sendTextAreaMessage(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            event.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
            if (event.isShiftDown()) {
                messageTextArea.appendText(System.lineSeparator());
            } else {
                sendMessage();
            }
        }
    }

    public void initMessageHandler() {
        Network.getInstance().waitMessages(message -> Platform.runLater(() -> {
            ViewController.this.appendMessageToChat("Server", message);
        }));
    }


    public void setApplication(ClientChat application) {
        this.application = application;
    }


}

package com.ivan.spring.bot.test.spring_bot_05_01_2023.service;

import com.ivan.spring.bot.test.spring_bot_05_01_2023.config.BotConfig;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.model.User;
import com.ivan.spring.bot.test.spring_bot_05_01_2023.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class CommandsBots extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;

    final BotConfig botConfig;
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String CREATE = "Создать пользователя";
    static final String DELETE = "Удалить пользователя";
    static final String READ = "Показать всех пользователей";
    static final String UPDATE = "Обновить пользователя";





    enum State {
        Start,
        Name,
        Country,
        ID_NEW,
        Name_New,
        Country_NEW,
        Start_Delete,
        Delete
    }

    public State state = State.Start;
    public User user = new User();


    public CommandsBots(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "запустить бота"));
        botCommandList.add(new BotCommand(CREATE, "Записать"));
        botCommandList.add(new BotCommand(READ, "показать базу данных"));
        botCommandList.add(new BotCommand(DELETE, "удалить сотрудника"));
        botCommandList.add(new BotCommand(UPDATE, "редактировать данные сотрудника"));
//        botCommandList.add(new BotCommand(ShowAllUsers, "delete my data"));

        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));

        } catch (TelegramApiException e) {

        }
    }


    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                startCommand(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.equals(READ)) {
                var users = userRepository.findAll();
                for (User userOne : users) {
                    int id = (int) userOne.getId();
                    String name = userOne.getName();
                    String country = userOne.getCountry();
                    sendMessage(chatId, "id: " + id + " Имя:" + name + ", страна: " + country);
                }

            } else if (messageText.equals(UPDATE)) {
                sendMessage(chatId, "Введите Id для изменения");
                state = State.ID_NEW;

        } else if (messageText.equals(DELETE)) {
                sendMessage(chatId, "введите Id для удаления");
                state = State.Start_Delete;

            } else if (messageText.equals(CREATE)) {
                sendMessage(chatId, "введите имя");
                state = State.Name;
            } else {
                switch (state) {
                    //Добавляем сотрудника
                    case Name:
                        user.setName(messageText);
                        sendMessage(chatId, "введите свой город");
                        state = State.Country;
                        break;
                    case Country:
                        user.setCountry(messageText);
                        sendMessage(chatId, "город сохранен и ты тоже");
                        userRepository.save(user);
                        state = State.Start;
                        break;
                        //удаляем сотрудника (Delete)
                    case Start_Delete:
                        user.setId(Integer.parseInt(messageText));
                        state = State.Delete;
                        break;
                    case Delete:
                        userRepository.deleteById(user.getId());
                        sendMessage(chatId, "Сотрудник удален");
                        state = State.Start;
                        var users = userRepository.findAll();
                        for (User userOne : users) {
                            int id = (int) userOne.getId();
                            String name = userOne.getName();
                            String country = userOne.getCountry();
                            sendMessage(chatId, "id: " + id + " Имя:" + name + ", страна: " + country);
                        }
                        break;
                        // Изменяем сотрудника
                    case ID_NEW:
                        if (!userRepository.findById(Long.valueOf(messageText)).isEmpty()){
                            user.setId(Integer.parseInt(messageText));
                            sendMessage(chatId, "Введите имя");
                            state = State.Name_New;
                        } else {
                            sendMessage(chatId, "нет такого пользователя");
                        }
                        break;

                        //Перезаписываем пользователя
                    case Name_New:
                        user.setName(messageText);
                        sendMessage(chatId, "Введите город");
                        state = State.Country_NEW;
                        break;
                    case Country_NEW:
                        user.setCountry(messageText);
                        userRepository.save(user);
                        sendMessage(chatId, "Данные изменены");
                        state = State.Start;
                        break;
                }
            }

        }

//        } else if (update.hasCallbackQuery()) {
//            Message msg = update.getCallbackQuery().getMessage();
//            String callBackData = update.getCallbackQuery().getData();
//            long messageId = update.getCallbackQuery().getMessage().getMessageId();
//            long chatId = update.getCallbackQuery().getMessage().getChatId();
//
//            if (callBackData.equals(YES_BUTTON)) {
//                if (msg != null) {
//                }
//                String text = "You pressed YES button";
//                executeEditMessageText(text, chatId, messageId);
//
//            } else if (callBackData.equals(NO_BUTTON)) {
//                String text = "You pressed NO button";
//                executeEditMessageText(text, chatId, messageId);
//            }
//        }


    }


    private void startCommand(long chatId, String firstName) {
        String answer = "Привет " + firstName + " этот бот умеет регистрировать пользователей в какой то базе";
        sendMessage(chatId, answer);
    }

    public void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        //Клавиатура снизу
//        InlineKeyboardButton buttonOne = new InlineKeyboardButton();
//        KeyboardButton button = new KeyboardButton();
//        button.setText("Создать юзера");


        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow rowOneLine = new KeyboardRow();
        var rowCreate = new ReplyKeyboardMarkup();


        rowOneLine.add(CREATE);

        rowOneLine.add(READ);
        keyboardRows.add(rowOneLine);

        rowOneLine = new KeyboardRow();
        rowOneLine.add(DELETE);
        rowOneLine.add(UPDATE);
        keyboardRows.add(rowOneLine);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void executeMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executeMessage(message);
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yesButon = new InlineKeyboardButton();
        yesButon.setText("Yes");
        yesButon.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInline.add(yesButon);
        rowInline.add(noButton);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }
}
